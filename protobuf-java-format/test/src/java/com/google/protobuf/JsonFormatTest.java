package com.google.protobuf;

import junit.framework.TestCase;
import protobuf_unittest.Bigint;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author eliran.bivas@orbitz.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class JsonFormatTest extends TestCase {
//    private static final String allFieldsSetText = TestUtil.readTextFromFile("xml_format_unittest_data.txt");

    public void testPrintToString() throws Exception {
        String javaText = JsonFormat.printToString(TestUtil.getAllSet());
        //assertEquals("json doesn't match", allFieldsSetText, javaText);
    }

    public void testStackOverflow() throws Exception {
        Bigint.BigData bd = Bigint.BigData.newBuilder().setD(ByteString.copyFrom(new byte[1024])).build();
        String jsonText = JsonFormat.printToString(bd);
        Bigint.BigData.Builder builder = Bigint.BigData.newBuilder();
        JsonFormat.merge(jsonText, builder);
    }

    public void testUnknown() throws Exception {
        Bigint.ThreeFields msg = Bigint.ThreeFields.newBuilder().setField1(123).setField2(456).setField3(789).build();
        Bigint.OneField m = Bigint.OneField.parseFrom(msg.toByteArray());
        System.out.println(JsonFormat.printToString(m));
    }
}
