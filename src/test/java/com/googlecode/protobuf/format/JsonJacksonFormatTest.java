package com.googlecode.protobuf.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.protobuf.ExtensionRegistry;
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
public class JsonJacksonFormatTest extends ProtobufFormatterTest<JsonJacksonFormat> {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    @Override
    protected JsonJacksonFormat getFormatterUnderTest() {
        return new JsonJacksonFormat();
    }

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
}
