package com.google.protobuf;

import junit.framework.TestCase;
import protobuf_unittest.Bigint;

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
}
