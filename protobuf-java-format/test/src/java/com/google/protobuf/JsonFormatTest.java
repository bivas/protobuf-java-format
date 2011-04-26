package com.google.protobuf;

import java.util.Arrays;

import junit.framework.TestCase;
import protobuf_unittest.Bigint;
import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.TestNestedExtension;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author eliran.bivas@gmail.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class JsonFormatTest extends TestCase {
    private static final String unknownFieldsText = TestUtil.readTextFromFile("json_format_unknown_fields_data.txt");
    private static final String bogusJson = "{\"name\": \"!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">\"}";
    private static final String validJson = "{\"name\": \"!@##&*)&*(&*&*&*\\\"}{))_+__+$$(((((((((((((((()!?:\\\">\"}";

    public void testStackOverflow() throws Exception {
        Bigint.BigData bd = Bigint.BigData.newBuilder().setD(ByteString.copyFrom(new byte[1024])).build();
        String jsonText = JsonFormat.printToString(bd);
        Bigint.BigData.Builder builder = Bigint.BigData.newBuilder();
        JsonFormat.merge(jsonText, builder);
    }

    public void testUnknown() throws Exception {
        Bigint.ThreeFields threeFields = Bigint.ThreeFields.newBuilder().setField1(123).addField2(456).addField2(789).
                setField3(Bigint.ThreeFields.Nested.newBuilder().setField1("foo").addField2("bar").addField2("blah").build()).
                build();
        Bigint.OneField oneField = Bigint.OneField.parseFrom(threeFields.toByteArray());
        String javaText = JsonFormat.printToString(oneField);
        System.out.println(javaText);
        assertEquals("json doesn't match", unknownFieldsText, javaText);

        Bigint.ThreeFields.Builder builder = Bigint.ThreeFields.newBuilder();
        JsonFormat.merge(javaText, builder);
        assertEquals(threeFields, builder.build());
    }

    public void testInvalidJson() throws Exception {
        Bigint.TestItem msg = Bigint.TestItem.newBuilder().setName("!@##&*)&*(&*&*&*\"}{))_+__+$$(((((((((((((((()!?:\">").build();
        String javaText = JsonFormat.printToString(msg);
        System.out.println(javaText);
        assertEquals(javaText, validJson);

        Bigint.TestItem.Builder builder = Bigint.TestItem.newBuilder();
        try {
            JsonFormat.merge(bogusJson, builder);
            fail("Expect parsing error due to malformed JSON");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test_stringValueContainsSurrogatePair() throws Exception {
        String testString = new String(Character.toChars(0x1D11E));
        Bigint.TestItem msg = Bigint.TestItem.newBuilder().setName(testString).build();
        String json = JsonFormat.printToString(msg);
        // Assert that the surrogate pair was encoded
        assertEquals("{\"name\": \"\\ud834\\udd1e\"}", json);
    
        // Assert that we can read the string back into a msg
        Bigint.TestItem.Builder builder = Bigint.TestItem.newBuilder();
        JsonFormat.merge(json, builder);
        assertEquals(msg, builder.build());
    }

    public void test_stringValueContainsControlCharacters() throws Exception {
        char[] ctrlChars = new char[0x001F + 1];
        for(char c = 0; c < 0x001F+1; c++) {
          ctrlChars[c] = c;
        }
        String testString = new String(ctrlChars);
        Bigint.TestItem msg = Bigint.TestItem.newBuilder().setName(testString).build();
        String json = JsonFormat.printToString(msg);
    
        // Assert that we can read the string back into a msg
        Bigint.TestItem.Builder builder = Bigint.TestItem.newBuilder();
        JsonFormat.merge(json, builder);
        Bigint.TestItem item = builder.build();
        assertEquals(msg, item);
        assertTrue(Arrays.equals(ctrlChars, item.getName().toCharArray()));
    }

    public void test_stringValueContainsCharactersThatShouldBeEscaped() throws Exception {
    	// input string is \"'
        String testString = new String("\\\"'");
        Bigint.TestItem msg = Bigint.TestItem.newBuilder().setName(testString).build();
        String json = JsonFormat.printToString(msg);
        // Assert that reverse-solidus and double quotes where escaped using a reverse-solidus

        // Expected string is {"name": "\\\"'"}
        assertEquals("{\"name\": \"\\\\\\\"\'\"}", json);

        // Assert that we can read the string back into a msg
        Bigint.TestItem.Builder builder = Bigint.TestItem.newBuilder();
        JsonFormat.merge(json, builder);
        assertEquals(msg, builder.build());
    }
    
    public void test_nestedExtension() throws Exception {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        UnittestProto.registerAllExtensions(registry);
        
        UnittestProto.TestAllExtensions tae = UnittestProto.TestAllExtensions.newBuilder().setExtension(TestNestedExtension.test, "aTest").build();
        String output = JsonFormat.printToString(tae);
  
        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        JsonFormat.merge(output, registry, builder);
        String value = builder.build().getExtension(TestNestedExtension.test);
        assertEquals("aTest", value);
    }
}
