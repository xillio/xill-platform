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
package nl.xillio.xill.plugins.mongodb.services.serializers;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.bson.types.Binary;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static org.testng.Assert.*;

public class MongoBinarySerializerTest extends TestUtils {
    private MongoIdSerializer serializer = new MongoIdSerializer(new ObjectIdSerializer(), new UUIDSerializer(), new BinarySerializer());

    @Test
    public void testParseStringStream() throws IOException {
        MetaExpression result = serializer.parseObject(new Binary("test".getBytes()));
        assertTrue(result.getBinaryValue().hasInputStream());
        assertEquals(IOUtils.toString(result.getBinaryValue().getInputStream()), "test");
    }

    @Test
    public void testParseDataStream() throws IOException {
        MetaExpression result = serializer.parseObject(new Binary(getBase64File()));
        assertTrue(result.getBinaryValue().hasInputStream());
        assertEquals(IOUtils.toByteArray(result.getBinaryValue().getInputStream()), getBase64File());

    }

    @Test
    public void testExtractStringStream() {
        String input = "test";
        MetaExpression binaryStream = fromValue(new SimpleIOStream(IOUtils.toInputStream(input), ""));

        byte[] bytes = ((Binary) serializer.extractValue(binaryStream)).getData();

        assertEquals(bytes, input.getBytes());
    }

    @Test
    public void testExtractDataStream() {
        byte[] input = getBase64File();

        MetaExpression binaryStream = fromValue(new SimpleIOStream(new ByteArrayInputStream(input), ""));
        byte[] bytes = ((Binary) serializer.extractValue(binaryStream)).getData();
        assertEquals(bytes, input);
    }

    @Test
    public void testExtractNothing() {
        assertNull(serializer.extractValue(fromValue("not a Xill stream")));
    }

    @Test
    public void testParseNothing() {
        assertNull(serializer.parseObject("Not a Binary object"));
    }


    private byte[] getBase64File() {
        return Base64.getDecoder().decode(
                "/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBw0QEhAQDxIPEBEQEA4SEA8QEBAPEA0QIBEiFiARHx8kKDQsJCYnJxUfIjsiMS" +
                        "k3LjouIys/QT84QzQ5LjcBCgoKDg0OGxAQGzIlHiY1OC43NzcyNzcwNDc3NTc0LTcrKzcyMTc3LS03Ny03LTExNzE4Ny0u" +
                        "LTIrNzc1NTUvLf/AABEIAGQAZAMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAAAAQIDBQYEB//EAD4QAAIBAwIDBg" +
                        "IEDAcBAAAAAAECAwAEEQUSBiExExRBUWFxByIyQoGhFSNSYnSCkaKxs8HRJDRDRFNyczP/xAAaAQEAAgMBAAAAAAAAAAAA" +
                        "AAAAAgMBBAUG/8QAIBEBAAICAQUBAQAAAAAAAAAAAAECAxEhBAUSMUGRUf/aAAwDAQACEQMRAD8A9mIpCKWkoEpcUVw63q" +
                        "0NnE00uSAQqIg3STSHksajxYnlisTMRG5Emqalb2sTT3EixRp1dvPyHmT0wOdV3C/FdpqPb937VXt5OzmimQxSoeeCR67T" +
                        "68qy47xcXUb3qJIyRPObb6cNhGcqiL4NKxBy55YUgdc1oeA9MRLdbs7WuNQSK5uJQANxZMrGPzVB2j9vjVGHPGW0+PqEpr" +
                        "po8UGlxS1sImGkp5ppoDFGKKWgMUUZooGK1OqGJs1KKBRWGu76Oe9aWU/ibSZbS0XBYPeMuXk5eIB2Z6DD1t5WwrHyBP3V" +
                        "iuCVzZW8h+lMHuGPiXkcyE/v1yu65ppj1H1Zjjl0cPHLXUzcy93Kv6keIgv7hPuTVlwG/wDgoYj9K1MtowPUGKQxfeEB9j" +
                        "VdoI2i5TxS7uM/rt2w+6QV0aBL2N7c255Ldot5D5GVQsMq+/KN/wBY1r9qyxGS1PxLJHG2poooruqSUhp1NNAlIxpTTHNA" +
                        "b6KgMlFAsfKpwagQZUH0pyk0Esi5Vh5gj7qxXBBxY26HrErQnPUGOQxlfs2YrarWOgQ2t5c22BsuW75Bk4BzhZUHqGw2Pz" +
                        "65Hd8U2xeUfFuOeSPcC3upd2dlzCJUAwS08fyMg8yVaMAehrk4kv5I+6XKRPvhdZohy7SQdmxlttv/AJBj/wBgtdvFGmd4" +
                        "gIVVeSJhLErcg7D/AE/TcCVz4ZzTtDtbRljuoe0cugKPNLNPJGp5lBvJ29MEDxFcfBmrjmuX7HCy0b4aZ3S6tyYJiFmiPZ" +
                        "3ERBK5XlIvt1qk+G0149hG92UZ3eZ0dCzdrG0hYSc/PJx+bt9qoLHh6zhne0Pa2q3Zke0uLWZ4CWIJe0cD5WI5su5T8vL6" +
                        "teg2lskUccUYwkSIiDyQLgD7q9ZjyVvWLV9S15jSWkzSmmGpsAmopW5UkhNRqCTzoKfV9djtnCMwBKBvvI/pS1iuMbY3N3" +
                        "KRzEWyIH2QMR+1zRQeoQjl9ppwpsZ6+9SUDhVLxZpMlxEskGBdWz9tbE8gzAYMR9HBKn3q0u5zHHI4UuUR2CDkXIUnb9uM" +
                        "VmIPiBaDTbfVJkkjjncRiJcSOsnaGMjwyBsJz5D7KhekXrNZZidOjSNRS5iWVMjOQ6NyeKQHDRsPMEYqumLWLu+C1nIzPI" +
                        "FBZrKQnLSY8UYnJ8QcnoTiv1a8uI9VmTTrWaYKkbakoKJCWMe5JEJP/wBMDGOh5eWa0OmanBcqWibJU7ZI2BWSF/yGU8wf" +
                        "Q15Tqelv01543WWxW3kLyGC5i2t+Mjfs2R4m+ZW3ZWZGHQr9IMKS04gltSItRK9mSFi1BRtic9Aso/029foH06VyNofZkv" +
                        "ZyG1ZjuaML2ltIfMx8sZ81Kn3qTvV2AVmtRLkEHu8sTo49VkK4z5c6t6PrJwcVndf56YtTbXKwIBBBBGQRzBFBrziPSTuK" +
                        "29ve6fGdxZor8QRryzkRI7L9wrn+El1qck14Zpbi5sukc1w5fE4bBSMknK9efTkK9Bg6qmadVUzWYeksKFFLSmtlFitCs+" +
                        "2SSZuZlnnfz5FuQoqy4MT/AAkPtRQXydTUwqGMczU4oCvItP4T1dpYNPuI4/wVBfyyRnA3LFHI8qsfSXt9nn8nh1PrtOFB" +
                        "57YW1/bavcztBfyJeXCKGiaHuKwCJVWZ+e7cuCMYH9K1es8MWl0wkYSRTAYFxbyNBMB+TuXqPQ8qwN3xk54hiiSSTusRWx" +
                        "kUbjC9w6M2T4bg4VfP5TT/AIa6xqk2oXqXV3vjiNyGt5Qik4nMayRY+qNjAnoOXXqI2pFo1JEtQdA1SIYhvYpgPoi7ttz4" +
                        "9WRlz74po0vWmO03GnR+q208jY9AZBUHwz1Ce6jmuZ5LxnmcssU6rHAkO9ijRADmpUjLedZmfiNW4ojQPhI4u5bRnDExNK" +
                        "T9j7Vx/atSe34N70n5y2cfCCyf564nvB/wnbBbH3RMbvZiRWhjjVFCIqqqgBVUBVUeQFZHhXiCRtOnvp5N5J1K4iDYytuk" +
                        "rbVA8htq04KuJXsbNriTtJ3toZJSxG/5l3AkfbitnHipjjVY0jMzK6oJoNNqxhQ8IZ7pCPJcUUvDB/EAeTyD96igvYvH3q" +
                        "UVFD4+5/jUwoErPWvHGlO5iFwokF4bMIwO55/ID8k9N3TNX1xv2tsxv2nbu+juxyz6V5rJ8Ll/B6iNYhqqxxt3okgd47wJ" +
                        "i2cdeq7sdKC/stR4be5ms4+6G5Wd7mWPsf8AcpkmUMRgsvM8jkc/Wo7S/wBAtjZXFrGsjajcyRW9xGpkftJH3OSzHKjd1H" +
                        "gT061mNE4TvwLO2ktezeynvp7i/wB8ZW9Z1cKqfWO7eucgYC1Bw5wLqMMWlTw7gsRe6ubK4+R0vBGyhlyMjd8qlc46H2DX" +
                        "8PapoEdxL3aKK1ne6nsQREsZupI8M2zb9UFhz5c6odJ0zSTd3uILxY7O/kvZ9SmkRIVuozzgB6lBvY4PPzrk+FXBeoW00N" +
                        "/OgDzd8W6juBiaHLZWWPl1YjByelW/ASXlvdXMFzDqB71NeTSF4o/wfCxlZsq+STvGOXn4UD9C4f4Zg7SG2KMb2ymkZjJJ" +
                        "Ie4H5Ww3RV5+/L0p/wAOJ+HUaa20l98uwPM7LMXkRW253MOYG7GBy5106Hwq9nNrE0MFqO87TZp9Vh2RzGw+qpfngf2rm+" +
                        "HdnqKSyGeyTT4BCEECyiVDP2hYvGOexCD9DOM8/Og3dMqQ03FBm9DYKkq/k3Fyv7JCKKg0lv8AMfpd3/NNJQaqLp9p/jUw" +
                        "oooCgUUUC0tFFAGkoooGNSClooA000UUGR0r/cfpl3/NNFFFB//Z");
    }
}
