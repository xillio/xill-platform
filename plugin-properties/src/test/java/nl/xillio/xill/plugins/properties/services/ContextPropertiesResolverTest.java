package nl.xillio.xill.plugins.properties.services;

import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;


public class ContextPropertiesResolverTest {
    @Test
    public void testDefaultXillResolver() throws Exception {
        ContextPropertiesResolver resolver = ContextPropertiesResolver.defaultXillResolver();
        ConstructContext context = new ConstructContext(
                RobotID.dummyRobot(),
                RobotID.getInstance(new File("I AM A UNIT TEST"), new File("I AM A PROJECT")),
                null,
                null,
                null,
                null,
                null
        );

        String projectPath = resolver.resolve("xill.projectPath", context).get();
        String robotPath = resolver.resolve("xill.robotPath", context).get();

        assertTrue(projectPath.contains("I AM A PROJECT"));
        assertTrue(robotPath.contains("I AM A UNIT TEST"));
    }

    @Test
    public void testResolve() throws Exception {
        ContextPropertiesResolver resolver = new ContextPropertiesResolver();
        resolver.register("test", constructContext -> "Hello World");

        assertEquals(resolver.resolve("test", null).get(), "Hello World");
        assertFalse(resolver.resolve("nope", null).isPresent());
    }

}
