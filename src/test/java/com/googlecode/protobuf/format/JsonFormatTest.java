package com.googlecode.protobuf.format;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.issue23.Issue23;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import protobuf_unittest.UnittestProto;
import sun.nio.cs.StandardCharsets;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.nio.charset.Charset;
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
    private static final JsonFormat JSON_FORMATTER =
            (JsonFormat) FORMAT_FACTORY.createFormatter(FormatFactory.Formatter.JSON);

    private static String getExpected(String name) throws IOException {
        return Files.readFile(JsonFormatTest.class.getResourceAsStream(RESOURCE_PATH + name)).trim();
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

    @Test(enabled = true, description = "https://github.com/bivas/protobuf-java-format/issues/23")
    public void testIssue23() throws Exception {
        Issue23.MsgWithUnknownFields issue23Message = Issue23.MsgWithUnknownFields.newBuilder()
                .setLeaf1("Hello")
                .setLeaf2(23)
                .addLeaf3(41)
                .setUnknownFields(
                        UnknownFieldSet.newBuilder()
                                .addField(4, UnknownFieldSet.Field.newBuilder()
                                        .addLengthDelimited(ByteString.copyFromUtf8("world"))
                                        .build())
                                .build())
                .build();
        String message = JSON_FORMATTER.printToString(issue23Message);
        assertThat(message, is("{\"leaf1\": \"Hello\",\"leaf2\": 23,\"leaf3\": [41], \"4\": [\"world\"]}"));

        Issue23.MsgWithUnknownFields.Builder issue23Builder = Issue23.MsgWithUnknownFields.newBuilder();
        ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        Issue23.registerAllExtensions(extensionRegistry);
        JSON_FORMATTER.merge(message, extensionRegistry, issue23Builder);
        assertThat("No unknown field 4", issue23Builder.getUnknownFields().hasField(4));
    }
}
