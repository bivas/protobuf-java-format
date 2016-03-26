package com.googlecode.protobuf.format;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.issue23.Issue23;
import com.googlecode.protobuf.format.issue23.Issue23.InnerTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.InnerTestMessage.InnerInnerTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.NewTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.NewTestMessage.UnknownGroup;
import com.googlecode.protobuf.format.issue23.Issue23.OldTestMessage;

import org.testng.Assert;
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

    @Test(description = "https://github.com/bivas/protobuf-java-format/issues/23")
    public void testIssue23Simple() throws Exception {
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
    
    @Test(description = "https://github.com/bivas/protobuf-java-format/issues/23")
    public void testIssue23Complex() throws Exception {
    	// Create version of message with "unknown" fields
        NewTestMessage expected = NewTestMessage.newBuilder()
            .setKnownfield("hello")
            .setUnknownfieldstring1("world")
            .addUnknownfieldstring2("I")
            .addUnknownfieldstring2("am")
            .setUnknownfieldMessage(InnerTestMessage.newBuilder()
                .setValue(30)
                .setInnerMessage(InnerInnerTestMessage.newBuilder().setValue(1.2f))
            )
            .setUnknownfieldInt64(51232271120233L)
            .setUnknownfieldInt32(6)
            .setUnknownfieldFloat(2.3f)
            .setUnknownfieldDouble(3.14d)
            .addUnknownGroup(UnknownGroup.newBuilder()
                .setName("hi")
                .setIntvalue(23)
                .setFloatvalue(5.2f)
                .setLongvalue(44232993922327L))
            .addUnknownfieldRepeatedMessage(InnerTestMessage.newBuilder()
                .setValue(6)
                .setInnerMessage(InnerInnerTestMessage.newBuilder().setValue(-1.3f))
            )
            .addUnknownfieldRepeatedMessage(InnerTestMessage.newBuilder()
                .setValue(-110)
                .setInnerMessage(InnerInnerTestMessage.newBuilder().setValue(0f))
            )
            .build();

        // Parse this message into the old version of the proto
        OldTestMessage parsedIntoOld = OldTestMessage.parseFrom(expected.toByteArray());

        NewTestMessage newRightBackFromOld = NewTestMessage.parseFrom(parsedIntoOld.toByteArray());
        Assert.assertEquals(newRightBackFromOld, expected);

        // Convert to json
        String oldJson = JSON_FORMATTER.printToString(parsedIntoOld);

        // Parse back from json into new and old protos
        OldTestMessage.Builder oldBuilder = OldTestMessage.newBuilder();
        JSON_FORMATTER.merge(
            oldJson,
            ExtensionRegistry.getEmptyRegistry(),
            oldBuilder);
        OldTestMessage oldParsedFromJson = oldBuilder.build();
        NewTestMessage.Builder newBuilder = NewTestMessage.newBuilder();
        JSON_FORMATTER.merge(
            oldJson,
            ExtensionRegistry.getEmptyRegistry(),
            newBuilder);
        NewTestMessage newParsedFromJson = newBuilder.build(); 

        NewTestMessage newParsedFromOld = NewTestMessage.parseFrom(oldParsedFromJson.toByteArray());
        NewTestMessage newParsedFromNew = NewTestMessage.parseFrom(newParsedFromJson.toByteArray());

        Assert.assertEquals(newParsedFromOld, expected);
        Assert.assertEquals(newParsedFromNew, expected);
        Assert.assertEquals(newParsedFromJson, expected);	
    }
    
    @Test(enabled = true, description = "https://github.com/bivas/protobuf-java-format/issues/24")
    public void testReverseEscapeBytes() throws Exception {
    	byte[] inBytes = {8, -110, -1, -1, -1, -1, -1, -1, -1, -1, 1};
    	ByteString inByteString = ByteString.copyFrom(inBytes);
    	String escaped = JsonFormat.escapeBytes(inByteString);
    	ByteString outByteString = JsonFormat.unescapeBytes(escaped);
    	byte[] outBytes = outByteString.toByteArray();
    	
    	Assert.assertTrue(Arrays.equals(inBytes, outBytes)); 
    }
}
