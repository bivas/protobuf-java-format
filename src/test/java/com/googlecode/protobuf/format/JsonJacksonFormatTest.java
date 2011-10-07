package com.googlecode.protobuf.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.OneString;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestNestedExtension;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.FormatFactory.Formatter;
import com.googlecode.protobuf.format.test.UnittestImport.ImportMessage;
import com.googlecode.protobuf.format.util.TextUtils;

/**
 * Unit test for {@link JsonJacksonFormat}
 *
 * @author jeffrey.damick@neustar.biz Jeffrey Damick
 */
public class JsonJacksonFormatTest {
    // since jackson doesn't append spaces after the colon... 
    private static final String unknownFieldsText = 
            TestUtil.readTextFromFile("json_format_unknown_fields_data.txt").replaceAll(": ", ":");
    
    private static final String bogusJson = "{\"default_string\":\"!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">\"}";
    private static final String validJson = "{\"default_string\":\"!@##&*)&*(&*&*&*\\\"}{))_+__+$$(((((((((((((((()!?:\\\">\"}";
    private FormatFactory formatFactory = new FormatFactory();
    private ProtobufFormatter formatter = formatFactory.createFormatter(Formatter.JSON_JACKSON);

    @Before
    public void setup() {
        formatter.setDefaultCharset(Charset.forName("UTF-8"));
    }
    
    @Test
    public void testStackOverflow() throws Exception {
    	TestAllTypes bd = TestAllTypes.newBuilder().setDefaultBytes(ByteString.copyFrom(new byte[1024])).build();
        String jsonText = formatter.printToString(bd);
        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(jsonText), builder);
    }

    @Test
    public void testUnknown() throws Exception {
    	TestAllTypes allTypes = TestAllTypes.newBuilder().setDefaultInt32(123).setOptionalInt64(456l).setOptionalString("foo").setOptionalImportMessage(ImportMessage.newBuilder().setD(123)).build();
        String javaText = formatter.printToString(allTypes);
         // System.out.println(javaText);
        assertEquals("json doesn't match", unknownFieldsText, javaText);

        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(javaText), builder);
        assertEquals(allTypes, builder.build());
    }
    
    @Test
    public void testMoreUnknown() throws Exception {
        UnknownFieldSet unknownGroupLevel2 = UnknownFieldSet.newBuilder()
                .addField(16, UnknownFieldSet.Field.newBuilder().addVarint(566667).build()).build();
        
        UnknownFieldSet unknownGroup = UnknownFieldSet.newBuilder()
                .addField(11, UnknownFieldSet.Field.newBuilder().addVarint(566667).build())
                .addField(15, UnknownFieldSet.Field.newBuilder().addGroup(unknownGroupLevel2).build())
                .build();
                
        ByteString bs = ByteString.copyFromUtf8("testUnknown");
        OneString data = OneString.newBuilder().setUnknownFields(
                UnknownFieldSet.newBuilder()
                    .addField(5, UnknownFieldSet.Field.newBuilder().addFixed32(999).build())
                    .addField(6, UnknownFieldSet.Field.newBuilder().addGroup(unknownGroup).build())
                    .addField(7, UnknownFieldSet.Field.newBuilder().addLengthDelimited(bs).build()).build()
        ).setData("12345").build();
        
        String javaText = formatter.printToString(data);
//System.out.println(javaText);
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(javaText), builder);
        assertEquals(data.getData(), builder.build().getData());
    }
    

    @Test
    public void testInvalidJson() throws Exception {
    	TestAllTypes msg = TestAllTypes.newBuilder().setDefaultString("!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">").build();
        String javaText = formatter.printToString(msg);
        //System.out.println(javaText);
        assertEquals(javaText, validJson);

        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        try {
        	formatter.merge(TextUtils.toInputStream(bogusJson), builder);
            fail("Expect parsing error due to malformed JSON");
        } catch (Exception e) {
           // good
        }
    }
    
    @Test
    public void testStringValueContainsSurrogatePair() throws Exception {
        String testString = new String(Character.toChars(0x1D11E));
                
        OneString msg = OneString.newBuilder().setData(testString).build();
        String json = formatter.printToString(msg);
        // Assert that the surrogate pair was encoded

        assertEquals("{\"data\":\"\\uD834\\uDD1E\"}", json);
    
        // Assert that we can read the string back into a msg
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(json), builder);
        assertEquals(msg, builder.build());
    }
    
    @Test
    public void testStringValueContainsControlCharacters() throws Exception {
        char[] ctrlChars = new char[0x001F + 1];
        for(char c = 0; c < 0x001F+1; c++) {
          ctrlChars[c] = c;
        }
        String testString = new String(ctrlChars);
        OneString msg = OneString.newBuilder().setData(testString).build();
        String json = formatter.printToString(msg);
    
        // Assert that we can read the string back into a msg
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(json), builder);
        OneString item = builder.build();
        assertEquals(msg, item);
        assertTrue(Arrays.equals(ctrlChars, item.getData().toCharArray()));
    }
 
    @Test
    public void testStringValueContainsCharactersThatShouldBeEscaped() throws Exception {
    	// input string is \"'
        String testString = "\\\"'";
        OneString msg = OneString.newBuilder().setData(testString).build();
        String json = formatter.printToString(msg);
        // Assert that reverse-solidus and double quotes where escaped using a reverse-solidus

        // Expected string is {"name": "\\\"'"}
        assertEquals("{\"data\":\"\\\\\\\"\'\"}", json);

        // Assert that we can read the string back into a msg
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(json), builder);
        assertEquals(msg, builder.build());
    }
    
    @Test
    public void testNestedExtension() throws Exception {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        UnittestProto.registerAllExtensions(registry);
        
        UnittestProto.TestAllExtensions tae = UnittestProto.TestAllExtensions.newBuilder().setExtension(TestNestedExtension.test, "aTest").build();
        String output = formatter.printToString(tae);
  
        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        formatter.merge(TextUtils.toInputStream(output), registry, builder);
        String value = builder.build().getExtension(TestNestedExtension.test);
        assertEquals("aTest", value);
    }


    
    @Test
    public void testChineseCharacters() throws Exception {
        String data = "検索jan5検索.8@test.relay.symantec.com";
        String testString = "{\"data\":\"" + data + "\"}";

        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(
                TextUtils.toInputStream(testString, Charset.forName("UTF-8")), 
                builder);
        OneString msg = builder.build();

        //System.out.println(msg.getData());

        assertEquals(data, msg.getData());
    }
    
}
