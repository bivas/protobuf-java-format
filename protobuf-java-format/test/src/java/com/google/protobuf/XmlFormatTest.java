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
package com.google.protobuf;

import junit.framework.TestCase;
import protobuf_unittest.UnittestProto.TestEmptyMessage;
import protobuf_unittest.UnittestProto;

/**
 * Unit test for {@link XmlFormat}
 *
 * @author eliran.bivas@orbitz.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class XmlFormatTest extends TestCase {

    private static final String allFieldsSetText = TestUtil.readTextFromFile("xml_format_unittest_data.txt");

    public void testPrintToString() throws Exception {
        String javaText = XmlFormat.printToString(TestUtil.getAllSet());
        assertEquals("xml doesn't match", allFieldsSetText, javaText);
    }

    public void testPrintUnknownFields() throws Exception {
        // Test printing of unknown fields in a message.

        TestEmptyMessage message = TestEmptyMessage.newBuilder().setUnknownFields(UnknownFieldSet.newBuilder().addField(5,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addFixed32(2).addFixed64(3).addLengthDelimited(ByteString.copyFromUtf8("4")).addGroup(UnknownFieldSet.newBuilder().addField(10,
                UnknownFieldSet.Field.newBuilder().addVarint(5).build()).build()).build()).addField(8,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addVarint(2).addVarint(3).build()).addField(15,
                UnknownFieldSet.Field.newBuilder().addVarint(0xABCDEF1234567890L).addFixed32(0xABCD1234).addFixed64(0xABCDEF1234567890L).build()).build()).build();

        assertEquals("<TestEmptyMessage><unknown-field index=\"5\">1</unknown-field><unknown-field index=\"5\">0x00000002</unknown-field><unknown-field index=\"5\">0x0000000000000003</unknown-field><unknown-field index=\"5\">4</unknown-field><unknown-field index=\"5\"><unknown-field index=\"10\">5</unknown-field></unknown-field><unknown-field index=\"8\">1</unknown-field><unknown-field index=\"8\">2</unknown-field><unknown-field index=\"8\">3</unknown-field><unknown-field index=\"15\">12379813812177893520</unknown-field><unknown-field index=\"15\">0xabcd1234</unknown-field><unknown-field index=\"15\">0xabcdef1234567890</unknown-field></TestEmptyMessage>",
                XmlFormat.printToString(message));

    }

    public void testParseFromString() throws Exception {
        String xmlText = XmlFormat.printToString(TestUtil.getAllSet());
        UnittestProto.TestAllTypes.Builder builder = UnittestProto.TestAllTypes.newBuilder();
        XmlFormat.merge(xmlText, builder);

        assertEquals(TestUtil.getAllSet(), builder.build());
    }

    public void testParseFromStringWithExtensions() throws Exception {
        String xmlText = XmlFormat.printToString(TestUtil.getAllExtensionsSet());
        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        XmlFormat.merge(xmlText, TestUtil.getExtensionRegistry(), builder);

        assertEquals(TestUtil.getAllExtensionsSet(), builder.build());
    }
}
