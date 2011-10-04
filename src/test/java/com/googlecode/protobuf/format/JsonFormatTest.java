package com.googlecode.protobuf.format;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.TextFormat;
import com.googlecode.protobuf.format.FormatFactory.Formatter;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.XmlFormat;
import com.googlecode.protobuf.format.test.UnittestImport.ImportMessage;
import com.googlecode.protobuf.format.util.TextUtils;

import org.junit.Test;
import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.OneString;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestNestedExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author eliran.bivas@gmail.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class JsonFormatTest {
    private static final String unknownFieldsText = TestUtil.readTextFromFile("json_format_unknown_fields_data.txt");
    private static final String bogusJson = "{\"default_string\": \"!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">\"}";
    private static final String validJson = "{\"default_string\": \"!@##&*)&*(&*&*&*\\\"}{))_+__+$$(((((((((((((((()!?:\\\">\"}";
    private FormatFactory formatFactory = new FormatFactory();
    private ProtobufFormatter formatter = formatFactory.createFormatter(Formatter.JSON);

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
        //System.out.println(javaText);
        assertEquals("json doesn't match", unknownFieldsText, javaText);

        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(javaText), builder);
        assertEquals(allTypes, builder.build());
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
    public void test_stringValueContainsSurrogatePair() throws Exception {
        String testString = new String(Character.toChars(0x1D11E));
        OneString msg = OneString.newBuilder().setData(testString).build();
        String json = formatter.printToString(msg);
        // Assert that the surrogate pair was encoded
        assertEquals("{\"data\": \"\\ud834\\udd1e\"}", json);
    
        // Assert that we can read the string back into a msg
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(json), builder);
        assertEquals(msg, builder.build());
    }
    
    @Test
    public void test_stringValueContainsControlCharacters() throws Exception {
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
    public void test_stringValueContainsCharactersThatShouldBeEscaped() throws Exception {
    	// input string is \"'
        String testString = "\\\"'";
        OneString msg = OneString.newBuilder().setData(testString).build();
        String json = formatter.printToString(msg);
        // Assert that reverse-solidus and double quotes where escaped using a reverse-solidus

        // Expected string is {"name": "\\\"'"}
        assertEquals("{\"data\": \"\\\\\\\"\'\"}", json);

        // Assert that we can read the string back into a msg
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(json), builder);
        assertEquals(msg, builder.build());
    }
    
    @Test
    public void test_nestedExtension() throws Exception {
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
    public void test_chineseCharacters() throws Exception {
        String data = "検索jan5検索.8@test.relay.symantec.com";
        String testString = "{\"data\":\"" + data + "\"}";

        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(
                TextUtils.toInputStream(testString, Charset.forName("UTF-8")),
                Charset.forName("UTF-8"),
                builder);
        OneString msg = builder.build();

        //System.out.println(msg.getData());

        assertEquals(data, msg.getData());
    }
}
