package nl.xillio.xill.plugins.mongodb.data;

import org.testng.annotations.Test;

import static org.testng.Assert.assertSame;

/**
 * Tests for {@link MongoObjectId}
 *
 * @author Geert Konijnendijk
 */
public class MongoObjectIdTest {

    /**
     * Test {@link MongoObjectId#copy()}
     */
    @Test
    public void testCopy() {
        MongoObjectId mongoObjectId = new MongoObjectId();

        // Run
        MongoObjectId copy = mongoObjectId.copy();

        // Assert

        // An ObjectID is immutable, so should not be cloned
        assertSame(mongoObjectId.getObjectId(), copy.getObjectId());
    }

}
