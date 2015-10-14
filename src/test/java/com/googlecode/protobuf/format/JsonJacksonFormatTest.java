package com.googlecode.protobuf.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import protobuf_unittest.UnittestProto;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.*;

/**
 * @author scr on 10/13/15.
 */
@Test
public class JsonJacksonFormatTest {
    @Test
    public void testPrettyPrint() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        JsonJacksonFormat jsonJacksonFormat =
                (JsonJacksonFormat) new FormatFactory().createFormatter(FormatFactory.Formatter.JSON_JACKSON);
        jsonJacksonFormat.print(UnittestProto.TestAllTypes.newBuilder()
                .setOptionalForeignEnum(UnittestProto.ForeignEnum.FOREIGN_BAR)
                .build(), jsonGenerator);
        assertThat(writer.toString(), is(Files.readFile(JsonJacksonFormatTest.class.getResourceAsStream(
                "/expectations/JsonJacksonFormatTest/prettyprint.json")).trim()));
    }
}
