package com.google.protobuf;

import org.junit.Test;
import protobuf_unittest.UnittestMultiNestedProto;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link JavaPropsFormat}
 *
 * @author alex@antonov.ws - Alex Antonov
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class JavaPropsFormatTest {
    @Test
    public void testPrintToString() throws Exception {
//        String javaText = JavaPropsFormat.printToString(TestUtil.getAllSet());
//
//        System.out.println(javaText);
        //assertEquals("props doesn't match", allFieldsSetText, javaText);

        UnittestMultiNestedProto.Outer msg = createNestedMessage();

        String javaText = JavaPropsFormat.printToString(msg);

        System.out.println(javaText);

        UnittestMultiNestedProto.Outer.Builder builder = UnittestMultiNestedProto.Outer.newBuilder();
        JavaPropsFormat.merge(javaText, builder);
        assertEquals(msg, builder.build());
    }

    private UnittestMultiNestedProto.Outer createNestedMessage() {
      UnittestMultiNestedProto.Outer.Builder builder = UnittestMultiNestedProto.Outer.newBuilder();

      UnittestMultiNestedProto.Inner.Builder inner1 = UnittestMultiNestedProto.Inner.newBuilder();
      UnittestMultiNestedProto.Inner.Builder inner2 = UnittestMultiNestedProto.Inner.newBuilder();

      UnittestMultiNestedProto.Holder.Builder holder1 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder2 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder3 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder4 = UnittestMultiNestedProto.Holder.newBuilder();

      UnittestMultiNestedProto.Holder.Builder holder5 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder6 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder7 = UnittestMultiNestedProto.Holder.newBuilder();
      UnittestMultiNestedProto.Holder.Builder holder8 = UnittestMultiNestedProto.Holder.newBuilder();

      holder1.setData("boo").setId("100").setRev("n/a");
      holder2.setData("bar").setId("200").setRev("n/a");
      holder3.setData("baz").setId("300").setRev("n/a");
      holder4.setData("ban").setId("400").setRev("n/a");

      holder5.setData("foo").setId("500").setRev("n/a");
      holder6.setData("far").setId("600").setRev("n/a");
      holder7.setData("faz").setId("700").setRev("n/a");
      holder8.setData("fan").setId("800").setRev("n/a");

      inner1.addData1(holder1);
      inner1.addData1(holder2);
      inner1.addData2(holder3);
      inner1.addData2(holder4);

      inner2.addData1(holder5);
      inner2.addData1(holder6);
      inner2.addData2(holder7);
      inner2.addData2(holder8);

      builder.addOne(inner1);
      builder.addOne(inner2);

      return builder.build();
    }
}
