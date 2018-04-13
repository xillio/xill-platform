package nl.xillio.xill.plugins.mongodb.data;


import nl.xillio.xill.api.components.CopyableMetadataExpression;
import org.bson.types.Binary;

public class MongoBinary implements CopyableMetadataExpression<MongoBinary> {
    private final Binary binary;

    public MongoBinary(Binary binary) {
        this.binary = binary;
    }

    @Override
    public String toString() {
        return new String(binary.getData());
    }

    @Override
    public MongoBinary copy() {
        // TODO: Verify whether Binary is immutable or not and on how to copy this item
        return new MongoBinary(binary);
    }

    public Binary getBinary() {
        return binary;
    }
}
