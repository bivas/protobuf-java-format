package com.google.protobuf;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.smile.Tool;

import protobuf_unittest.Bigint;
import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.TestAllTypes;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author jeffrey.damick@neustar.biz Jeffrey Damick
 *         Based on {@link TextFormat} originally written by:
 * @author eliran.bivas@gmail.com Eliran Bivas
 *         <p/>
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class SmileFormatTest extends TestCase {
    private static final String bogusJson = "{\"name\": \"!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">\"}";
    private static final String validJson = "{\"name\": \"!@##&*)&*(&*&*&*\\\"}{))_+__+$$(((((((((((((((()!?:\\\">\"}";
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private final Tool smileTool = new Tool();

    
    public void testStackOverflow() throws Exception {
        Bigint.BigData bd = Bigint.BigData.newBuilder().setD(ByteString.copyFrom(new byte[1024])).build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SmileFormat.print(bd, output);
        Bigint.BigData.Builder builder = Bigint.BigData.newBuilder();
        SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), builder);
        assertEquals(bd, builder.build());
    }

    public void testNonUtf8Bytes() throws Exception {
    	TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    	byte[] ff = {(byte) 0xFF}; // RFC 3629 - The octet values C0, C1, F5 to FF never appear.
    	byte[] testStrBytes = "\n\r\t\b\u0012".getBytes(CHARSET_UTF8);
    	byte[] testBytes = new byte[testStrBytes.length + 1];
    	System.arraycopy(testStrBytes, 0, testBytes, 0, testStrBytes.length);
    	System.arraycopy(ff, 0, testBytes, testStrBytes.length, ff.length);
    	
    	try {
    		ByteBuffer bb = ByteBuffer.wrap(testBytes);
    		CHARSET_UTF8.newDecoder().decode(bb);
    		fail("expected decode error, we want a non-utf8 ByteString");
    	} catch (MalformedInputException malformedException) {
    		// should catch
    	}
    	builder.setOptionalBytes(ByteString.copyFrom(testBytes));
    	TestAllTypes byteType = builder.build();
    	
    	// put it out
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SmileFormat.print(byteType, output);
        TestAllTypes.Builder roundTripBuilder = TestAllTypes.newBuilder();
        SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), roundTripBuilder);
        assertArrayEquals(byteType.getOptionalBytes().toByteArray(), roundTripBuilder.getOptionalBytes().toByteArray());
        // make we get back what we put in.
        assertArrayEquals(testBytes, roundTripBuilder.getOptionalBytes().toByteArray());
    }
    
    public void testExtensions() throws Exception {
    	UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	SmileFormat.print(TestUtil.getAllExtensionsSet(), output);
    	SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), TestUtil.getExtensionRegistry(), builder);
        assertEquals(TestUtil.getAllExtensionsSet(), builder.build());
    }
    
    public void testUnregisteredExtensions() throws Exception {
    	UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	SmileFormat.print(TestUtil.getAllExtensionsSet(), output);
    	try {
    		SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), builder);
    		fail("Should fail without the extension registered");
    	} catch (RuntimeException rte) {}
    }
    
    public void testUnknown() throws Exception {
    	Bigint.ThreeFields threeFields = Bigint.ThreeFields.newBuilder().setField1(123).addField2(456).addField2(789).
                setField3(Bigint.ThreeFields.Nested.newBuilder().setField1("foo").addField2("bar").addField2("blah").build()).
                build();
        Bigint.OneField oneField = Bigint.OneField.parseFrom(threeFields.toByteArray());
        
        // put it out
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SmileFormat.print(oneField, output);
        
        // and back in..
        Bigint.OneField.Builder oneFieldSmiled = Bigint.OneField.newBuilder();
        SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), oneFieldSmiled);
        	
        // make sure both the reference object and the one we built match (without unknowns)
        // we leave off unknowns, because without also encoding the field number in the json
        // there is no way to add them to the UnknownFieldSet
        Bigint.OneField.Builder oneFieldWithoutUnknowns = Bigint.OneField.newBuilder(oneField);
        oneFieldWithoutUnknowns.setUnknownFields(UnknownFieldSet.newBuilder().build()); // empty them
        assertEquals(oneFieldWithoutUnknowns.build(), oneFieldSmiled.build());
        				
        Bigint.ThreeFields.Builder builder = Bigint.ThreeFields.newBuilder();
        SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), builder);
        assertEquals(threeFields, builder.build());
    }

    // test a simple round trip
    public void testAllFieldsSet() throws Exception {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	TestAllTypes allTypes = TestUtil.getAllSet();
    	SmileFormat.print(allTypes, output);
    	// verify compatibility with JsonFormat
        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    	SmileFormat.merge(new ByteArrayInputStream(output.toByteArray()), builder);
    	assertEquals(allTypes, builder.build());
    }
    
    // test backward compatibility (except for byte arrays) with Json from Jackson and parsing with JsonFormat
    // @see testNonUtf8Bytes 
    public void testAllFieldsSetBackwardCompat() throws Exception {
    	StringWriter output = new StringWriter();

    	TestAllTypes.Builder allTypesBuilder = TestAllTypes.newBuilder();
        TestUtil.setAllFields(allTypesBuilder);
        // clear all byte fields since we are encoding bytes in Jackson's Base64 w/o linefeeds
        allTypesBuilder.clearOptionalBytes();
        allTypesBuilder.clearDefaultBytes();
        allTypesBuilder.clearRepeatedBytes();
    	
    	TestAllTypes allTypes = allTypesBuilder.build();

    	printAsText(allTypes, output);

    	// verify compatibility with JsonFormat
        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    	JsonFormat.merge(new StringReader(output.toString()), builder);
    	assertEquals(allTypes, builder.build());
    	
    	TestAllTypes.Builder builder2 = TestAllTypes.newBuilder();
    	JsonFactory jsonFactory = new JsonFactory();
    	JsonParser parser = jsonFactory.createJsonParser(
    			new ByteArrayInputStream(output.toString().getBytes(CHARSET_UTF8)));
    	SmileFormat.merge(parser, ExtensionRegistry.getEmptyRegistry(), builder2);
    	assertEquals(allTypes, builder2.build());
    }

    public void testInvalidJson() throws Exception {
        Bigint.TestItem msg = Bigint.TestItem.newBuilder().setName("!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">").build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SmileFormat.print(msg, smileTool.smileFactory.createJsonGenerator(output));
        
        ByteArrayOutputStream validSmile = new ByteArrayOutputStream();
        encode(validJson, validSmile);
        assertArrayEquals(validSmile.toByteArray(), output.toByteArray());

        try {
            ByteArrayOutputStream bogusSmile = new ByteArrayOutputStream();
            encode(bogusJson, bogusSmile);
            fail("Expect parsing error due to malformed JSON");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void printAsText(Message msg, Writer writer) throws IOException {
    	JsonGenerator generator = (new JsonFactory()).createJsonGenerator(writer);
    	generator.useDefaultPrettyPrinter();
    	SmileFormat.print(msg, generator);
    }
    
    // based on: org.codehaus.jackson.smile.Tool
    private void encode(String in, OutputStream out) throws IOException {
        JsonParser jp = smileTool.jsonFactory.createJsonParser(in);
        JsonGenerator jg = smileTool.smileFactory.createJsonGenerator(out, JsonEncoding.UTF8);
        while ((jp.nextToken()) != null) {
            jg.copyCurrentEvent(jp);
        }
        jp.close();
        jg.close();
    }
}
