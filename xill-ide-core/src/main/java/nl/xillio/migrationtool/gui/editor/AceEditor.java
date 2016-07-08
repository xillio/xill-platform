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
package nl.xillio.migrationtool.gui.editor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.biesaart.utils.FileUtils;
import me.biesaart.utils.Log;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import nl.xillio.migrationtool.BreakpointPool;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.migrationtool.gui.FileTab;
import nl.xillio.migrationtool.gui.ReplaceBar;
import nl.xillio.migrationtool.gui.RobotTab;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.preview.Replaceable;
import nl.xillio.xill.util.HotkeysHandler.Hotkeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class wraps around a webview object containing an ace editor in Xill mode.
 * It *should* extend {@link WebView}, but this class is final...
 */
public class AceEditor implements EventHandler<javafx.event.Event>, Replaceable, EventDispatcher {
    private static final Logger LOGGER = Log.get();
    private static final Clipboard clipboard = Clipboard.getSystemClipboard();
    private static String EDITOR_URL;

    private final StringProperty code = new SimpleStringProperty();
    private final WebView editor;
    private final SimpleBooleanProperty documentLoaded = new SimpleBooleanProperty(false);
    private final EventHost<Boolean> onDocumentLoaded = new EventHost<>();
    private final EventDispatcher parentEventDispatcher;

    private JSObject ace;
    private FileTab tab;
    private ContextMenu rightClickMenu;
    private JSObject undoManager;

    static {
        try {
            deployEditor();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Failed to deploy editor", e);
        }
    }

    /**
     * Deploy the editor file.
     * <p>
     * This is a workaround for a bug introduced in jdk1.8.0_60 where internal resources cannot reference other internal resources. This method should be removed as soon as this bug is fixed.
     * </p>
     *
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8134975?page=com.atlassian.streams.streams-jira-plugin:activity-stream-issue-tab">Bug Report</a>
     * @deprecated This is a workaround
     */
    @Deprecated
    private static void deployEditor() throws IOException, TemplateException {
        File editorFile = File.createTempFile("xill_editor", ".html");
        FileUtils.forceDeleteOnExit(editorFile);
        Configuration config = new Configuration(Configuration.VERSION_2_3_23);
        config.setClassForTemplateLoading(AceEditor.class, "/");
        Template template = config.getTemplate("editor.html");
        Map<String, Object> model = new HashMap<>();
        model.put("jarFile", AceEditor.class.getResource("/editor.html").toExternalForm().replaceAll("editor\\.html", ""));
        template.process(model, new FileWriter(editorFile));
        LOGGER.info("Deployed editor as JavaFX workaround");
        EDITOR_URL = editorFile.toURI().toURL().toExternalForm();
    }

    /**
     * Default constructor. Takes a {@link WebView} since we can't extend it.
     *
     * @param editor the {@link WebView} to wrap in
     */
    public AceEditor(final WebView editor) {
        this.editor = editor;
        this.parentEventDispatcher = editor.getEventDispatcher();

        // Add our own context menu.
        editor.setContextMenuEnabled(false);
        createContextMenu();

        // Add event handlers.
        editor.addEventHandler(KeyEvent.KEY_PRESSED, this);
        editor.addEventHandler(ScrollEvent.SCROLL, this);
        editor.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this);
        editor.addEventHandler(MouseEvent.MOUSE_PRESSED, this);

        // Replace the event dispatcher to make copy and cut work correctly
        editor.setEventDispatcher(this);

        documentLoaded.addListener(
                (obs, oldDoc, newDoc) -> {
                    if (newDoc != null) {
                        onDocumentLoad();
                        onDocumentLoaded.invoke(newDoc);
                    }
                });

        // Disable drag-and-drop, set the cursor graphic when dragging.
        editor.setOnDragDropped(null);
        editor.setOnDragOver(e -> editor.sceneProperty().get().setCursor(Cursor.DISAPPEAR));

        editor.getEngine().getLoadWorker().exceptionProperty().addListener(
                (source, o, n) -> {
                    LOGGER.error("Javascript exception", n);
                }
        );
    }

    /**
     * Build the right-click context menu.
     */
    private void createContextMenu() {
        // Cut menu item.
        MenuItem cut = new MenuItem("Cut");
        cut.setOnAction(e -> cut());

        // Copy menu item.
        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(e -> copy());

        // Paste menu item.
        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(e -> paste());

        // Create the menu with all items.
        rightClickMenu = new ContextMenu(cut, copy, paste);
    }

    /**
     * Set the {@link RobotTab}
     *
     * @param tab the tab that should be used by this editor
     */
    public void setTab(final FileTab tab) {
        this.tab = tab;
    }

    private void onDocumentLoad() {
        // Set members
        bindToWindow();
        // Set code, if it was set before editor loads
        String codeString = code.get();
        if (codeString != null) {
            setCode(codeString);
        }
        // get focus
        callOnAce("focus");
    }

    /**
     * Clears all highlighted lines
     */
    public void clearHighlight() {
        callOnAce("clearHighlight");
    }

    /**
     * Clears the history by loading a new UndoManager
     */
    public void clearHistory() {
        executeJSBlocking("editor.session.setUndoManager(new UndoManager())");
    }

    /**
     * Cut content from the editor
     */
    private void cut() {
        copy();
        callOnAce("onCut");
    }

    /**
     * This method is called from javascript whenever cut or copy is performed, to copy the selected text to the clipboard.
     */
    public void copy() {
        // Get the copy text from ace.
        String text = (String) callOnAceBlocking("getSelectedText");

        // Replace double newlines and carriage returns by single newlines.
        String removedNewlines = text.replaceAll("\r\n?", "\n");

        final ClipboardContent content = new ClipboardContent();
        content.putString(removedNewlines);
        clipboard.setContent(content);
    }

    /**
     * @return The ace editor Javascript object
     */
    public JSObject getAce() {
        return ace;
    }

    /**
     * @param ace The ace editor Javascript object
     */
    public void setAce(JSObject ace) {
        this.ace = ace;
    }

    /**
     * Clears all breakpoints.
     */
    public void clearBreakpoints() {
        callOnAce(session -> ((JSObject) session).call("clearBreakpoints"), "getSession");
    }

    /**
     * Returns the current code property
     *
     * @return code property
     */
    public StringProperty getCodeProperty() {
        return code;
    }

    /**
     * Handles input events
     */
    @Override
    public void handle(final javafx.event.Event event) {
        // Hotkeys
        if (event instanceof KeyEvent) {
            KeyEvent ke = (KeyEvent) event;

            if (matchesHotkey(Hotkeys.DUPLICATELINES, ke)) {
                callOnAce("duplicateCurrentLines");
            }
        }

        // Context menu
        if (event instanceof ContextMenuEvent) {
            ContextMenuEvent ce = (ContextMenuEvent) event;
            rightClickMenu.show(editor, ce.getScreenX(), ce.getScreenY());
            event.consume();
        }

        // Mouse click, close context menu.
        if (event instanceof MouseEvent) {
            rightClickMenu.hide();
        }
    }

    /**
     * @param hotkey The hotkey to match
     * @param ke     The event to test for a match
     * @return True if the event matches the hotkey
     */
    private boolean matchesHotkey(Hotkeys hotkey, KeyEvent ke) {
        return KeyCombination.valueOf(FXController.hotkeys.getShortcut(hotkey)).match(ke);
    }


    @Override
    public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
        if (event instanceof KeyEvent && event.getEventType() == KeyEvent.KEY_PRESSED) {
            KeyEvent ke = (KeyEvent) event;

            if (matchesHotkey(Hotkeys.CUT, ke)) {
                cut();
                ke.consume();
            } else if (matchesHotkey(Hotkeys.COPY, ke)) {
                copy();
                ke.consume();
            } else if (matchesHotkey(Hotkeys.PASTE, ke)) {
                paste();
                ke.consume();
            }
        }
        return parentEventDispatcher.dispatchEvent(event, tail);
    }

    /**
     * Highlights a line.
     *
     * @param line the line to highlight
     * @param type type of highlighting to be used( "error" or "highlight" )
     */
    public void highlightLine(final int line, final String type) {
        callOnAce("highlight", (line > 0 ? line - 1 : 0), type);
    }

    /**
     * Pastes the current clipboard at the caret.
     */
    public void paste() {
        String clipboardContent = (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
        callOnAceBlocking("onPaste", clipboardContent);
    }

    /**
     * Steps back in edit history.
     */
    public void undo() {
        callOnAceBlocking("undo");
    }

    /**
     * Steps forward in the edit history.
     */
    public void redo() {
        callOnAceBlocking("redo");
    }

    /**
     * Clears the selected text.
     */
    public void clearSelection() {
        callOnAce("clearSelection");
    }

    /**
     * Wrapper around {@link WebView#requestFocus()}.
     *
     * @see WebView#requestFocus()
     */
    public void requestFocus() {
        Platform.runLater(editor::requestFocus);
    }

    /**
     * Sets the code.
     *
     * @param code the code to set
     */
    public void setCode(final String code) {
        if (documentLoaded.get()) {
            if (ace != null) {
                callOnAceBlocking("setCode", code);
                clearHistory();
            }
            this.code.setValue(code);
        } else {

            // Run this later
            documentLoaded.addListener(
                    (obs, oldDoc, newDoc) -> {
                        if (newDoc != null) {
                            setCode(code);
                        }
                    });
        }
    }

    public Boolean hasRedo() {
        return (Boolean) executeJSBlocking("editor.session.getUndoManager().hasRedo()");
    }

    public Boolean hasUndo() {
        return (Boolean) executeJSBlocking("editor.session.getUndoManager().hasUndo()");
    }


    public void snapshotUndoManager() {
        undoManager = (JSObject) executeJSBlocking("editor.session.getUndoManager()");
    }

    public void restoreUndoManager() {
        if (undoManager != null) {
            ((JSObject) ace.getMember("session")).call("setUndoManager", undoManager);
        }
    }

    /**
     * Load breakpoints from a breakpoint pool for a particular robot
     *
     * @param robot
     */
    public void refreshBreakpoints(final RobotID robot) {
        List<Integer> bps = BreakpointPool.INSTANCE.get(robot).stream().map(bp -> bp - 1).collect(Collectors.toList());
        clearBreakpoints();
        bps.forEach(br -> callOnAce(s -> ((JSObject) s).call("setBreakpoint", br), "getSession"));
    }

    /**
     * Sets whether to show invisible characters (line breaks, spaces, etc...).
     *
     * @param show whether to display invisible characters
     */
    public void setShowInvisibles(final boolean show) {
        callOnAce("setShowInvisibles", show);
    }


    /**
     * Perform Ace editor settings
     *
     * @param jsCode JavaScript code with settings to be executed
     */
    public void setOptions(final String jsCode) {
        // If the document has not been loaded yet load the robot later
        if (!documentLoaded.get()) {
            documentLoaded.addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            setOptions(jsCode);
                        }
                    });

            return;
        }
        executeJS(jsCode);
    }

    private void bindToWindow() {
        // Do not use executeJS here, it needs to be done immediately
        JSObject jsobj = (JSObject) executeJSBlocking("window");
        jsobj.setMember("javaEditor", this);

        // Check if the tab is a robot tab.
        if (tab instanceof RobotTab) {
            jsobj.setMember("xillCoreOverride", new XillJSObject(((RobotTab) tab).getProcessor()));
        }

        executeJSBlocking("init();");
    }

    /**
     * This method is called by the javascript editor whenever the code has been changed
     *
     * @param newCode the current code
     */
    public void codeChanged(final String newCode) {
        code.setValue(newCode);
    }

    /**
     * This method is called by the javascript editor whenever the list of breakpoints has changed
     *
     * @param jsBreakpoints the breakpoints to set
     */
    public void breakpointsChanged(JSObject jsBreakpoints) {
        // Check if the tab is a robot tab.
        if (!(tab instanceof RobotTab)) {
            return;
        }
        RobotTab robotTab = (RobotTab) tab;

        int length = ((Number) jsBreakpoints.getMember("length")).intValue();

        List<Integer> breakpoints = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            if (!"undefined".equals(jsBreakpoints.getSlot(i).toString())) {
                breakpoints.add(i + 1);
            }
        }

        BreakpointPool.INSTANCE.clear(robotTab.getCurrentRobot());

        breakpoints.forEach(bp -> BreakpointPool.INSTANCE.add(robotTab.getCurrentRobot(), bp));
    }

    /**
     * Fail-safe way to execute a javascript code on a document that may not have been fully loaded yet.
     * Wrapper around {@link WebEngine#executeScript(String)}, that returns null in case of an error not caused by the document not being loaded.
     * <p>
     * Defers execution of code using {@link Platform#runLater(Runnable)}.
     *
     * @param js the script to execute.
     * @see WebEngine#executeScript(String)
     */
    private void executeJS(final String js) {
        executeJS(js, r -> {
        });
    }

    private void executeJS(final String js, final Consumer<Object> callback) {
        Platform.runLater(() -> callback.accept(executeJSBlocking(js)));
    }

    /**
     * Fail-safe way to execute a javascript code on a document that may not have been fully loaded yet.
     * Wrapper around {@link WebEngine#executeScript(String)}, that returns null in case of an error not caused by the document not being loaded.
     * <p>
     * Runs code immediately.
     *
     * @param js the script to execute.
     * @see WebEngine#executeScript(String)
     */
    private Object executeJSBlocking(final String js) {
        if (!documentLoaded.get()) {
            throw new IllegalStateException("Cannot run javascript because the editor has not been loaded yet: " + js);
        }
        try {
            return editor.getEngine().executeScript(js);
        } catch (JSException e) {
            LOGGER.error("Failed to execute javascript [" + js + "]");
            throw e;
        }
    }

    /**
     * Call a method on the javascript ace editor object
     * <p>
     * Defers execution using {@link Platform#runLater(Runnable)}.
     *
     * @param method Method name
     * @param args   Arguments to pass to the method
     * @see JSObject#call(String, Object...)
     */
    private void callOnAce(String method, Object... args) {
        callOnAce(o -> {
        }, method, args);
    }

    /**
     * Call a method on the javascript ace editor object
     * <p>
     * Defers execution using {@link Platform#runLater(Runnable)}.
     *
     * @param callback Callback that is called with the return value of the call
     * @param method   Method name
     * @param args     Arguments to pass to the method
     * @see JSObject#call(String, Object...)
     */
    private void callOnAce(final Consumer<Object> callback, String method, Object... args) {
        Platform.runLater(() -> callback.accept(callOnAceBlocking(method, args)));
    }

    /**
     * Call a method on the javascript ace editor object
     *
     * @param method Method name
     * @param args   Arguments to pass to the method
     * @see JSObject#call(String, Object...)
     */
    private Object callOnAceBlocking(String method, Object... args) {
        try {
            return ace.call(method, args);
        } catch (JSException e) {
            LOGGER.error("Failed to call on ace [" + method + ": " + Arrays.toString(args) + "] ");
            throw e;
        }
    }

    /**
     * Load Ace editor in the {@link WebView}
     */
    public void loadEditor() {
        load(EDITOR_URL);
    }

    private void load(final String path) {
        editor.getEngine().load(path);
        editor.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> documentLoaded.setValue(true));
    }

    private static String escape(final String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }

    public String getSelectedText() {
        Object range = ((JSObject) ace.getMember("selection")).call("getRange");
        return ((JSObject) ((JSObject) callOnAceBlocking("getSession")).getMember("doc")).call("getTextRange", range).toString();
    }

    /**
     * Get the plugin and the construct at the cursor position
     *
     * @return the plugin and construct at cursor position
     * @throws ClassCastException which is most likely an out of bounds exception by retrieving tokens.
     */
    public String[] getPluginAndConstructAtCursor() throws ClassCastException {

        Object cursor = ((JSObject) ace.getMember("selection")).call("getCursor");
        Object column = ((JSObject) cursor).getMember("column");
        Object row = ((JSObject) cursor).getMember("row");
        Object currentToken = ((JSObject) callOnAceBlocking("getSession")).call("getTokenAt", row, column);
        String[] result = new String[2];

        // The line was empty thus return nulls
        if (currentToken == null) {
            return result;
        }

        Object tokens = ((JSObject) callOnAceBlocking("getSession")).call("getTokens", row);
        int tokenIndex = Integer.valueOf(((JSObject) currentToken).getMember("index").toString());
        String tokenType = ((JSObject) currentToken).getMember("type").toString();

        String plugin = null;
        String construct = null;

        switch (tokenType) {
            case "plugin":
                if (((JSObject) ((JSObject) tokens).getSlot(tokenIndex + 1)).getMember("type").toString().equals("construct")) {
                    construct = getTokenValue(tokens, tokenIndex + 1).replace(".", "");
                }
                plugin = getTokenValue(currentToken);
                break;
            case "construct":
                construct = getTokenValue(currentToken).replace(".", "");
                plugin = getTokenValue(tokens, tokenIndex - 1);
                break;
            default:
                break;
        }
        result[0] = (plugin);
        result[1] = (construct);
        return result;
    }

    private String getTokenValue(Object tokens, int index) throws ClassCastException {
        return getTokenValue(((JSObject) tokens).getSlot(index));
    }

    private String getTokenValue(Object token) throws ClassCastException {
        return ((JSObject) token).getMember("value").toString();
    }

    /**
     * Auto-detect and set the editor mode based on the path.
     *
     * @param path the path to detect the mode for
     */
    public void autoDetectMode(String path) {
        executeJS("editor.session.setMode(modelist.getModeForPath(\"" + path + "\").mode);");
    }

    ////////////////// SEARCH BAR //////////////////
    private ReplaceBar replaceBar;
    private int occurrences = 0;

    /**
     * Set the replace bar that the editor uses.
     *
     * @param bar The replace bar.
     */
    public void setReplaceBar(ReplaceBar bar) {
        replaceBar = bar;
    }

    @Override
    public void searchPattern(final String pattern, final boolean caseSensitive) {
        callOnAce(this::handleResult, "findOccurrences", pattern, true, caseSensitive);
    }

    @Override
    public void search(final String needle, final boolean caseSensitive) {
        callOnAce(this::handleResult, "findOccurrences", needle, false, caseSensitive);
    }

    @Override
    public int getOccurrences() {
        return occurrences;
    }

    @Override
    public void replaceAll(final String replacement) {
        callOnAce("replaceAll", replacement);
    }

    @Override
    public void replaceOne(final int occurrence, final String replacement) {
        callOnAce("replace", replacement);
    }

    private void handleResult(Object r) {
        // Get the amount of occurrences and current index.
        JSObject result = (JSObject) r;
        occurrences = (int) result.getMember("amount");
        if (replaceBar != null)
            replaceBar.setCurrentOccurrence((int) result.getMember("index"));

        // If there are no results, clear the search
        if (occurrences == 0)
            callOnAceBlocking("clearSearch");
    }

    @Override
    public void findNext(int next) {
        callOnAce(this::handleResult, "doFind", false, true);
    }

    @Override
    public void findPrevious(int previous) {
        callOnAce(this::handleResult, "doFind", true, true);
    }

    @Override
    public void clearSearch() {
        callOnAce("clearSearch");
    }

    /**
     * @return the onDocumentLoaded
     */
    public Event<Boolean> getOnDocumentLoaded() {
        return onDocumentLoaded.getEvent();
    }

    public void setEditable(final boolean editable) {
        callOnAce("setReadOnly", !editable);
    }

    public void annotate(List<Issue> issues) {
        List<String> annotations = issues.stream()
                .map(this::toJavaScript)
                .collect(Collectors.toList());
        String param = StringUtils.join(annotations, ",");
        executeJS("editor.session.setAnnotations([ " + param + "]);");
    }

    private String toJavaScript(Issue issue) {
        return String.format("{row:%d,column:0,text:\"%s\",type:\"%s\"}", (issue.getLine() > 0 ? issue.getLine() - 1 : 0),
                escape(issue.getMessage()), issue.getSeverity().toString().toLowerCase());
    }
}
