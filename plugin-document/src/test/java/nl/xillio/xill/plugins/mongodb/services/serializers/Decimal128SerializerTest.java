package nl.xillio.xill.plugins.mongodb.services.serializers;

import org.bson.types.Decimal128;
import org.testng.annotations.Test;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class Decimal128SerializerTest {
    private final Decimal128Serializer serializer = new Decimal128Serializer();

    @Test
    public void testParseObject() {
        assertEquals(
                serializer.parseObject(new Decimal128(5430462)),
                fromValue(5430462)
        );
        assertNull(serializer.parseObject("Test String"));
    }
}
