package com.googlecode.protobuf.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.issue23.Issue23;
import com.googlecode.protobuf.format.issue23.Issue23.InnerTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.NewTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.OldTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.InnerTestMessage.InnerInnerTestMessage;
import com.googlecode.protobuf.format.issue23.Issue23.NewTestMessage.UnknownGroup;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import protobuf_unittest.UnittestProto;

import java.io.StringWriter;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.*;

/**
 * @author scr on 10/13/15.
 */
@Test
public class JsonJacksonFormatTest {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    @Test
    public void testPrettyPrint() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(writer)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        JsonJacksonFormat jsonJacksonFormat =
                (JsonJacksonFormat) new FormatFactory().createFormatter(FormatFactory.Formatter.JSON_JACKSON);
        jsonJacksonFormat.print(UnittestProto.TestAllTypes.newBuilder()
                .setOptionalForeignEnum(UnittestProto.ForeignEnum.FOREIGN_BAR)
                .setOptionalFloat(0)
                .build(), jsonGenerator);
        jsonGenerator.close();
        assertThat(writer.toString(), is(Files.readFile(JsonJacksonFormatTest.class.getResourceAsStream(
                "/expectations/JsonJacksonFormatTest/prettyprint.json")).trim()));
    }

    @Test
    public void testUnsignedTypes() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(writer);
        JsonJacksonFormat jsonJacksonFormat =
                (JsonJacksonFormat) new FormatFactory().createFormatter(FormatFactory.Formatter.JSON_JACKSON);
        final long maxIntAsLong = ((long) Integer.MAX_VALUE) << 1;
        final BigInteger maxLongAsBigInt = BigInteger.valueOf(Long.MAX_VALUE).shiftLeft(1);
        jsonJacksonFormat.print(UnittestProto.TestAllTypes.newBuilder()
                .setOptionalUint32((int) maxIntAsLong)
                .setOptionalFixed32((int) maxIntAsLong)
                .setOptionalUint64(maxLongAsBigInt.longValue())
                .setOptionalFixed64(maxLongAsBigInt.longValue())
                .build(), jsonGenerator);
        jsonGenerator.close();
        assertThat(writer.toString(), is(Files.readFile(JsonJacksonFormatTest.class.getResourceAsStream(
                "/expectations/JsonJacksonFormatTest/unsigneds.json")).trim()));
        JsonParser jsonParser = JSON_FACTORY.createParser(writer.toString());

        UnittestProto.TestAllTypes.Builder testAllTypesBuilder = UnittestProto.TestAllTypes.newBuilder();
        ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        UnittestProto.registerAllExtensions(extensionRegistry);
        jsonJacksonFormat.merge(jsonParser, extensionRegistry, testAllTypesBuilder);
        assertThat(testAllTypesBuilder.getOptionalUint32(), is((int) maxIntAsLong));
        assertThat(testAllTypesBuilder.getOptionalFixed32(), is((int) maxIntAsLong));
        assertThat(testAllTypesBuilder.getOptionalUint64(), is(maxLongAsBigInt.longValue()));
        assertThat(testAllTypesBuilder.getOptionalFixed64(), is(maxLongAsBigInt.longValue()));
    }
    
    @Test(enabled=false, description = "https://github.com/bivas/protobuf-java-format/issues/23")
    public void testIssue23Simple() throws Exception {    	
    	JsonJacksonFormat jsonJacksonFormat =
                (JsonJacksonFormat) new FormatFactory().createFormatter(FormatFactory.Formatter.JSON_JACKSON);
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
        String message = jsonJacksonFormat.printToString(issue23Message);        

        Issue23.MsgWithUnknownFields.Builder issue23Builder = Issue23.MsgWithUnknownFields.newBuilder();
        ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
        Issue23.registerAllExtensions(extensionRegistry);
        JsonParser jsonParser = JSON_FACTORY.createParser(message);
        jsonJacksonFormat.merge(jsonParser, extensionRegistry, issue23Builder);
        assertThat("No unknown field 4", issue23Builder.getUnknownFields().hasField(4));
    }
    
    @Test(enabled=false, description = "https://github.com/bivas/protobuf-java-format/issues/23")
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
        
        JsonJacksonFormat jsonJacksonFormat =
                (JsonJacksonFormat) new FormatFactory().createFormatter(FormatFactory.Formatter.JSON_JACKSON);

        // Convert to json
        String oldJson = jsonJacksonFormat.printToString(parsedIntoOld);

        // Parse back from json into new and old protos
        OldTestMessage.Builder oldBuilder = OldTestMessage.newBuilder();
        jsonJacksonFormat.merge(
        	JSON_FACTORY.createParser(oldJson),
            ExtensionRegistry.getEmptyRegistry(),
            oldBuilder);
        OldTestMessage oldParsedFromJson = oldBuilder.build();
        NewTestMessage.Builder newBuilder = NewTestMessage.newBuilder();
        jsonJacksonFormat.merge(
            JSON_FACTORY.createParser(oldJson),
            ExtensionRegistry.getEmptyRegistry(),
            newBuilder);
        NewTestMessage newParsedFromJson = newBuilder.build(); 

        NewTestMessage newParsedFromOld = NewTestMessage.parseFrom(oldParsedFromJson.toByteArray());
        NewTestMessage newParsedFromNew = NewTestMessage.parseFrom(newParsedFromJson.toByteArray());

        Assert.assertEquals(newParsedFromOld, expected);
        Assert.assertEquals(newParsedFromNew, expected);
        Assert.assertEquals(newParsedFromJson, expected);	
    }
}
