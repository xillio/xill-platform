/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill;

import com.google.inject.Injector;
import me.biesaart.utils.Log;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.XillParsingException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue.IssueImpl;
import org.slf4j.Logger;
import xill.RobotLoader;
import xill.lang.XillStandaloneSetup;
import xill.lang.validation.XillValidator;
import xill.lang.xill.ConstructCall;
import xill.lang.xill.InstructionSet;
import xill.lang.xill.UseStatement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for processing a single Xill file
 */
public class XillProcessor implements nl.xillio.xill.api.XillProcessor {
    private static final Logger LOGGER = Log.get();
    /**
     * The supported file extension
     */
    private final XtextResourceSet resourceSet;

    private final IResourceValidator validator;
    private final RobotID robotID;
    private final Path workingDirectory;
    private final RobotLoader robotLoader;
    private final List<XillPlugin> plugins;
    private final Debugger debugger;
    private final Map<Construct, String> argumentSignatures = new HashMap<>();
    private Robot robot;
    private OutputHandler outputHandler = new DefaultOutputHandler();

    /**
     * Create a new processor that can run a file.
     *
     * @param workingDirectory   the project folder
     * @param fullyQualifiedName fully qualified name of the robot that should be executed
     * @param robotLoader        the robot loader
     * @param plugins            the plugins
     * @param debugger           the debugger
     * @throws IOException if thrown if a file(-related) operation fails.
     */
    public XillProcessor(final Path workingDirectory, final String fullyQualifiedName, final RobotLoader robotLoader, final List<XillPlugin> plugins,
                         final Debugger debugger) throws IOException {
        this.workingDirectory = workingDirectory;
        this.robotLoader = robotLoader;
        this.robotID = toRobotID(fullyQualifiedName);
        this.plugins = plugins;
        this.debugger = debugger;
        Injector injector = new XillStandaloneSetup(robotLoader).createInjectorAndDoEMFRegistration();
        injector.injectMembers(this);

        // obtain a resource set
        resourceSet = injector.getInstance(XtextResourceSet.class);
        validator = injector.getInstance(IResourceValidator.class);
    }

    @Override
    public List<Issue> compile() throws IOException, XillParsingException {
        return compileAsSubRobot(null);
    }

    @Override
    public List<Issue> compileAsSubRobot(final RobotID rootRobot) throws XillParsingException {
        debugger.reset();
        return compile(robotID, rootRobot);
    }

    @Override
    public void setOutputHandler(final OutputHandler outputHandler) {
        Objects.requireNonNull(outputHandler);
        this.outputHandler = outputHandler;
        this.debugger.setOutputHandler(outputHandler);
    }

    private List<Issue> compile(final RobotID robotID, RobotID rootRobot) throws XillParsingException {
        Resource resource = findResource(robotID);

        if (rootRobot == null) {
            rootRobot = robotID;
        }

        LanguageFactory<xill.lang.xill.Robot> factory = new XillProgramFactory(workingDirectory, plugins, getDebugger(), rootRobot, outputHandler, robotLoader);


        List<Issue> issues = validateAllResources();

        // Throw an exception when an error was found
        Optional<Issue> error = issues.stream().filter(issue -> issue.getSeverity() == Issue.Type.ERROR).findFirst();

        if (error.isPresent()) {
            throw new XillParsingException(error.get().getMessage(), error.get().getLine(), error.get().getRobot());
        }

        xill.lang.xill.Robot mainRobotToken = null;

        // Parse all resources
        for (Resource currentResource : resourceSet.getResources()) {
            for (EObject rootToken : currentResource.getContents()) {

                // Parse
                factory.parse(
                        (xill.lang.xill.Robot) rootToken,
                        toRobotID(currentResource)
                );

                // Check if is main robot token
                if (rootToken.eResource() == resource) {
                    mainRobotToken = (xill.lang.xill.Robot) rootToken;
                }
            }
        }

        factory.compile();

        robot = factory.getRobot(mainRobotToken);
        return issues;

    }

    private URL toURL(URI uri) {
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RobotID toRobotID(String fullyQualifiedName) throws NoSuchFileException {
        return RobotID.getInstance(robotLoader.getRobot(fullyQualifiedName))
                .orElseThrow(() -> new NoSuchFileException("No robot was found for '" + fullyQualifiedName + "'"));
    }

    private RobotID toRobotID(Resource resource) {
        return RobotID.getInstance(toURL(resource.getURI()))
                .orElse(null);
    }

    private Resource findResource(RobotID robotID) {
        return resourceSet.getResource(URI.createURI(robotID.getURL().toString()), true);
    }

    @Override
    public List<Issue> validate() {
        debugger.reset();
        return validateAllResources();
    }

    private List<Issue> validateAllResources() {
        // Validate all resources and concat issues
        List<Resource> resources = resourceSet.getResources();
        List<Issue> result = new ArrayList<>();

        // This cannot be replaced by a foreach since the resource set is
        // modified while validating (resources are added)
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            result.addAll(validate(
                    resource,
                    toRobotID(resource)
            ));
        }

        return result;
    }

    private List<Issue> validate(final Resource resource, final RobotID robotID) {
        try {
            return doValidate(resource, robotID);
        } catch (WrappedException e) {
            // In rare cases xText throws an exception. That means we have no information except the robot with which it happened.
            LOGGER.error("Exception during validation.", e);
            return Collections.singletonList(new Issue("An unexpected exception occurred during compilation." +
                    "\nThis can be caused by two reserved keywords incorrectly following each other somewhere in this robot.", -1, Issue.Type.ERROR, robotID));
        }
    }

    private List<Issue> doValidate(final Resource resource, final RobotID robotID) {
        // Validate
        List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl).stream()
                .map(issue -> {
                    IssueImpl impl = (IssueImpl) issue;
                    Issue.Type type;

                    switch (impl.getSeverity()) {
                        case ERROR:
                            type = Issue.Type.ERROR;
                            break;
                        case WARNING:
                            type = Issue.Type.WARNING;
                            break;
                        default:
                            type = Issue.Type.INFO;
                            break;
                    }

                    return new Issue(impl.getMessage(), impl.getLineNumber(), type, robotID);
                }).collect(Collectors.toList());

        // Add warnings to issues
        resource.getWarnings().forEach(warning -> issues.add(new Issue(warning.getMessage(), warning.getLine(), Issue.Type.WARNING, robotID)));

        // Check for the existence of the plugins
        for (EObject object : resource.getContents()) {
            xill.lang.xill.Robot bot = (xill.lang.xill.Robot) object;

            for (UseStatement useStatement : bot.getUses()) {
                String name = useStatement.getName();

                boolean found = plugins
                        .stream()
                        .anyMatch(plugin -> plugin.getName().equals(name));

                if (!found) {
                    INode node = NodeModelUtils.getNode(object);

                    issues.add(new Issue("No plugin with name " + name + " was found.", node.getStartLine(), Issue.Type.ERROR, robotID));
                }
            }
        }

        // Check for the existence of the constructs
        if (issues.isEmpty()) {
            for (EObject object : resource.getContents()) {
                xill.lang.xill.Robot bot = (xill.lang.xill.Robot) object;
                List<Issue> constructIssues = checkConstructs(bot.getInstructionSet(), robotID);
                issues.addAll(constructIssues);
            }
        }
        return issues;
    }

    private List<Issue> checkConstructs(InstructionSet instructionSet, RobotID robotID) {
        TreeIterator<EObject> iterator = instructionSet.eAllContents();

        List<Issue> issues = new ArrayList<>();

        while (iterator.hasNext()) {
            EObject object = iterator.next();

            if (object instanceof ConstructCall) {
                ConstructCall call = (ConstructCall) object;
                String plugin = call.getPackage().getName();
                XillPlugin xillPlugin = plugins.stream()
                        .filter(p -> p.getName().equals(plugin))
                        .findAny()
                        .orElse(null);

                Construct construct = xillPlugin.getConstruct(call.getFunction());
                INode node = NodeModelUtils.getNode(object);
                if (construct == null) {
                    // Create an error if the construct does not exist
                    Issue issue = new Issue("No construct with name " + call.getFunction() + " was found in package " + plugin, node.getStartLine(), Issue.Type.ERROR, robotID);
                    issues.add(issue);
                } else if (construct.isDeprecated()) {
                    Issue issue = new Issue("Call to deprecated construct with name " + call.getFunction(), node.getStartLine(), Issue.Type.WARNING, robotID);
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    /**
     * @return The last compiled robot or null if compilation hasn't taken place
     * yet.
     */
    @Override
    public Robot getRobot() {
        return robot;
    }

    /**
     * @return the debugger
     */
    @Override
    public Debugger getDebugger() {
        return debugger;
    }

    @Override
    public RobotID getRobotID() {
        return robotID;
    }

    @Override
    public Collection<String> listPackages() {
        return plugins.stream()
                .map(XillPlugin::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String[] getReservedKeywords() {
        return XillValidator.RESERVED_KEYWORDS;
    }

    @Override
    public Map<String, List<String>> getCompletions(String currentLine, String prefix, int column, int row) {

        Map<String, List<String>> result = new HashMap<>();
        getConstructCompletions(result, currentLine, column, prefix);

        if (result.isEmpty()) {
            result.put("keyword", Arrays.asList(XillValidator.RESERVED_KEYWORDS));
            result.put("package", plugins.stream().map(XillPlugin::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                    .collect(Collectors.toList()));
        }


        return result;
    }

    /**
     * This method will fill the result map with construct advice based on the test in front of the cursor.
     *
     * @param result      the result map to fill
     * @param currentLine the current line
     * @param column      the cursor position
     * @param prefix      the piece of text in front of the cursor
     */
    private void getConstructCompletions(Map<String, List<String>> result, String currentLine, int column, String prefix) {
        if (column <= 0) {
            return;
        }

        int lastPeriod = currentLine.lastIndexOf(".");

        if (lastPeriod >= 0 && lastPeriod == column - prefix.length() - 1) {
            String tillColumn = currentLine.substring(0, lastPeriod);

            plugins.stream()
                    // Test if the plugin name is a match.
                    .filter(xillPlugin -> tillColumn.endsWith(xillPlugin.getName()))
                    .forEach(xillPlugin -> {
                        // Test all constructs.
                        List<String> constructs = xillPlugin.getConstructs().stream()
                                .filter(construct -> prefix.isEmpty() || construct.getName().startsWith(prefix))
                                .map(this::getSignature).collect(Collectors.toList());

                        // If there are results put them in the map.
                        if (!constructs.isEmpty()) {
                            result.put(xillPlugin.getName(), constructs);
                        }
                    });
        }

    }

    /**
     * This method will build a signature that matches the construct.
     *
     * @param construct the construct
     * @return the signature
     */
    private String getSignature(Construct construct) {
        if (!argumentSignatures.containsKey(construct)) {
            ConstructContext context = new ConstructContext(workingDirectory, getRobotID(), getRobotID(), construct, null, null, outputHandler, null, null);
            try (ConstructProcessor processor = construct.prepareProcess(context)) {

                List<String> args = new ArrayList<>();
                for (int i = 0; i < processor.getNumberOfArguments(); i++) {
                    String arg = processor.getArgumentName(i);
                    args.add("${" + (i + 1) + ":" + arg + "}");
                }

                String signature = construct.getName() + "(" + StringUtils.join(args, ", ") + ")";
                argumentSignatures.put(construct, signature);
            }
        }

        return argumentSignatures.get(construct);
    }
}
