package com.googlecode.protobuf.format;

import com.google.protobuf.Message;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import protobuf_unittest.UnittestProto;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author scr on 10/13/15.
 */
@Test
public class JsonFormatTest {
    private static final String RESOURCE_PATH = "/expectations/JsonFormatTest/";
    private static final FormatFactory FORMAT_FACTORY = new FormatFactory();
    private static final ProtobufFormatter JSON_FORMATTER =
            FORMAT_FACTORY.createFormatter(FormatFactory.Formatter.JSON);

    private static String getExpected(String name) throws IOException {
        return Files.readFile(JsonFormatTest.class.getResourceAsStream(RESOURCE_PATH + "test1.json")).trim();
    }

    @DataProvider(name = "data")
    public static Object[][] data() throws IOException {
        return new Object[][]{
                {"test1.json",
                        UnittestProto.TestAllTypes.newBuilder()
                                .addAllRepeatedBool(Arrays.asList(true, false, true))
                                .setOptionalForeignEnum(UnittestProto.ForeignEnum.FOREIGN_FOO)
                                .build(),
                        JSON_FORMATTER,
                        getExpected("test1.json")},
        };
    }

    @Test(dataProvider = "data")
    public void testJsonFormat(
            String desc, Message input, ProtobufFormatter formatter, String expectedString) throws Exception {
        assertThat(formatter.printToString(input), is(expectedString));
    }
}
