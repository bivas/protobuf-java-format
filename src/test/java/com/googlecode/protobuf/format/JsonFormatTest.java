package com.googlecode.protobuf.format;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.bits.Base64Serializer;
import com.googlecode.protobuf.format.bits.HexByteSerializer;
import com.googlecode.protobuf.format.issue23.Issue23;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import protobuf_unittest.UnittestProto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
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
    private static final JsonFormat JSON_HEX_FORMATTER = new JsonFormat(new HexByteSerializer());
    private static final JsonFormat JSON_BASE64_FORMATTER = new JsonFormat(new Base64Serializer());

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
                {"test2_hex_bytes.json",
                        UnittestProto.TestAllTypes.newBuilder()
                                .setOptionalBytes(ByteString.copyFromUtf8("Hello!"))
                                .build(),
                        JSON_HEX_FORMATTER,
                        getExpected("test2_hex_bytes.json")},
                {"test3_base64_bytes.json",
                        UnittestProto.TestAllTypes.newBuilder()
                                .setOptionalBytes(ByteString.copyFromUtf8("Hello!"))
                                .build(),
                        JSON_BASE64_FORMATTER,
                        getExpected("test3_base64_bytes.json")},
                {"test4_default_bytes.json",
                        UnittestProto.TestAllTypes.newBuilder()
                                .setOptionalBytes(ByteString.copyFromUtf8("Hello!"))
                                .build(),
                        JSON_FORMATTER,
                        getExpected("test4_default_bytes.json")},
        };
    }

    @Test(dataProvider = "data")
    public void testJsonFormat(
            String desc, Message input, ProtobufFormatter formatter, String expectedString) throws Exception {

        assertThat(formatter.printToString(input), is(expectedString));
    }

    @Test(dataProvider = "data")
    public void testJsonFormatParsing(
            String desc, Message input, ProtobufFormatter formatter, String expectedString) throws Exception {

        UnittestProto.TestAllTypes.Builder builder = UnittestProto.TestAllTypes.newBuilder();
        formatter.merge(new ByteArrayInputStream(expectedString.getBytes()), builder);
        assertThat(builder.build(), is(input));
    }

    // TODO(scr): Re-enable test when the code is fixed to enable slurping unknown fields into the message.
    @Test(enabled = false, description = "https://github.com/bivas/protobuf-java-format/issues/23")
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

    @Test
    public void testDeserializeNullFieldFromJson() throws Exception {
        UnittestProto.TestNullField.Builder builder = UnittestProto.TestNullField.newBuilder();
        new JsonJacksonFormat().merge(JsonFormatTest.class.getResourceAsStream("/json_format_null_field_data.txt"), builder);

        final UnittestProto.TestNullField actual = builder.build();
        System.out.println(actual);
        assertThat(actual, equalTo(UnittestProto.TestNullField.newBuilder().build()));
    }

    @Test
    public void testSkipUnknownFieldsFromJson() throws Exception {
        UnittestProto.TestEmptyMessage.Builder builder = UnittestProto.TestEmptyMessage.newBuilder();
        JSON_FORMATTER.merge(JsonFormatTest.class.getResourceAsStream("/json_format_unknown_fields_data.txt"), builder);
    }
}
