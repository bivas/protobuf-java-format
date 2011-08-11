/* 
    Copyright (c) 2009, Orbitz LLC
    All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, 
    are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright notice, 
          this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright notice, 
          this list of conditions and the following disclaimer in the documentation 
          and/or other materials provided with the distribution.
        * Neither the name of the Orbitz LLC nor the names of its contributors 
          may be used to endorse or promote products derived from this software 
          without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
    "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
    LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
    OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.googlecode.protobuf.format;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.XmlFormat;
import com.googlecode.protobuf.format.FormatFactory.Formatter;
import com.googlecode.protobuf.format.util.TextUtils;

import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.TestEmptyMessage;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author eliran.bivas@gmail.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class XmlFormatTest {

    private static final String allFieldsSetText = TestUtil.readTextFromFile("xml_format_unittest_data.txt");
    private FormatFactory formatFactory = new FormatFactory();
    private ProtobufFormatter formatter = formatFactory.createFormatter(Formatter.XML);

    @Test
    public void testPrintToString() throws Exception {
        String javaText = formatter.printToString(TestUtil.getAllSet());
        assertEquals("xml doesn't match", allFieldsSetText, javaText);
    }

    @Test
    public void testPrintUnknownFields() throws Exception {
        // Test printing of unknown fields in a message.

        TestEmptyMessage message = TestEmptyMessage.newBuilder().setUnknownFields(UnknownFieldSet.newBuilder().addField(5,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addFixed32(2).addFixed64(3).addLengthDelimited(ByteString.copyFromUtf8("4")).addGroup(UnknownFieldSet.newBuilder().addField(10,
                UnknownFieldSet.Field.newBuilder().addVarint(5).build()).build()).build()).addField(8,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addVarint(2).addVarint(3).build()).addField(15,
                UnknownFieldSet.Field.newBuilder().addVarint(0xABCDEF1234567890L).addFixed32(0xABCD1234).addFixed64(0xABCDEF1234567890L).build()).build()).build();

        assertEquals("<TestEmptyMessage><unknown-field index=\"5\">1</unknown-field><unknown-field index=\"5\">0x00000002</unknown-field><unknown-field index=\"5\">0x0000000000000003</unknown-field><unknown-field index=\"5\">4</unknown-field><unknown-field index=\"5\"><unknown-field index=\"10\">5</unknown-field></unknown-field><unknown-field index=\"8\">1</unknown-field><unknown-field index=\"8\">2</unknown-field><unknown-field index=\"8\">3</unknown-field><unknown-field index=\"15\">12379813812177893520</unknown-field><unknown-field index=\"15\">0xabcd1234</unknown-field><unknown-field index=\"15\">0xabcdef1234567890</unknown-field></TestEmptyMessage>",
        		formatter.printToString(message));

    }
    
    @Test
    public void testPrintUnknownFieldSet() throws Exception {
        UnknownFieldSet fieldSet = UnknownFieldSet.newBuilder().addField(5,
                                   UnknownFieldSet.Field.newBuilder().addVarint(1).addFixed32(2).addFixed64(3).addLengthDelimited(ByteString.copyFromUtf8("4")).addGroup(
                                   UnknownFieldSet.newBuilder().addField(10, 
                                   UnknownFieldSet.Field.newBuilder().addVarint(5).build()).build()).build()).addField(8,
                                   UnknownFieldSet.Field.newBuilder().addVarint(1).addVarint(2).addVarint(3).build()).addField(15,
                                   UnknownFieldSet.Field.newBuilder().addVarint(0xABCDEF1234567890L).addFixed32(0xABCD1234).addFixed64(0xABCDEF1234567890L).build()).build();
        assertEquals("unknown fields message doesn't match", 
        		"<message><unknown-field index=\"5\">1</unknown-field><unknown-field index=\"5\">0x00000002</unknown-field><unknown-field index=\"5\">0x0000000000000003</unknown-field><unknown-field index=\"5\">4</unknown-field><unknown-field index=\"5\"><unknown-field index=\"10\">5</unknown-field></unknown-field><unknown-field index=\"8\">1</unknown-field><unknown-field index=\"8\">2</unknown-field><unknown-field index=\"8\">3</unknown-field><unknown-field index=\"15\">12379813812177893520</unknown-field><unknown-field index=\"15\">0xabcd1234</unknown-field><unknown-field index=\"15\">0xabcdef1234567890</unknown-field></message>",  
        		formatter.printToString(fieldSet));
    }

    @Test
    public void testParseFromString() throws Exception {
        String xmlText = formatter.printToString(TestUtil.getAllSet());
        UnittestProto.TestAllTypes.Builder builder = UnittestProto.TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(xmlText), builder);

        assertEquals(TestUtil.getAllSet(), builder.build());
    }

    @Test
    public void testParseFromStringWithExtensions() throws Exception {
        String xmlText = formatter.printToString(TestUtil.getAllExtensionsSet());
        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        formatter.merge(TextUtils.toInputStream(xmlText), TestUtil.getExtensionRegistry(), builder);

        assertEquals(TestUtil.getAllExtensionsSet(), builder.build());
    }

    /*
    public void testStackOverflow() throws Exception {
        Bigint.BigData bd = Bigint.BigData.newBuilder().setD(ByteString.copyFrom(new byte[1024])).build();
        String xmlText = XmlFormat.printToString(bd);
        Bigint.BigData.Builder builder = Bigint.BigData.newBuilder();
        XmlFormat.merge(xmlText, builder);
    }

    public void testSpacesInStringValues() throws Exception {
        Bigint.TestItem.Builder itemBuilder =  Bigint.TestItem.newBuilder();
        itemBuilder.setName("name with spaces");
        String itemTxt = XmlFormat.printToString(itemBuilder.build());
        itemBuilder = Bigint.TestItem.newBuilder();
        XmlFormat.merge(itemTxt,  itemBuilder);
    }

    public void testUnknown() throws Exception {
        Bigint.ThreeFields msg = Bigint.ThreeFields.newBuilder().setField1(123).addField2(456).addField2(789).
                setField3(Bigint.ThreeFields.Nested.newBuilder().setField1("foo").addField2("bar").addField2("blah").build()).
                build();
        Bigint.OneField m = Bigint.OneField.parseFrom(msg.toByteArray());
        System.out.println(XmlFormat.printToString(m));
    }
    */
}
