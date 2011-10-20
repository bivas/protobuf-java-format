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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;

import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.ForeignMessage;
import protobuf_unittest.UnittestProto.OneString;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestCamelCaseFieldNames;
import protobuf_unittest.UnittestProto.TestEmptyMessage;
import protobuf_unittest.UnittestProto.TestNestedExtension;
import protobuf_unittest.UnittestProto.TestRequired;
import protobuf_unittest.UnittestProto.TestRequiredForeign;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.TextFormat;
import com.google.protobuf.UnknownFieldSet;
import com.googlecode.protobuf.format.FormatFactory.Formatter;
import com.googlecode.protobuf.format.test.UnittestImport.ImportMessage;
import com.googlecode.protobuf.format.util.TextUtils;

/**
 * Unit test for {@link XmlFormat}
 * @author jeffrey.damick@neustar.biz Jeffrey Damick
 * @author eliran.bivas@gmail.com Eliran Bivas
 *         <p/>
 *         Based on {@link TextFormat} originally written by:
 * @author wenboz@google.com (Wenbo Zhu)
 */
public class XmlJavaxFormatTest {

    private static final String allFieldsSetText = TestUtil.readTextFromFile("xml_format_unittest_data.txt");
    private FormatFactory formatFactory = new FormatFactory();
    private ProtobufFormatter formatter = formatFactory.createFormatter(Formatter.XML_JAVAX);
    private static final String XML_VERSION = "<?xml version=\"1.0\" ?>";

    
    @Test
    public void testPrintToString() throws Exception {
        String javaText = formatter.printToString(TestUtil.getAllSet());
//        System.out.println(javaText);
        assertEquals("xml doesn't match", XML_VERSION + allFieldsSetText, javaText);
    }

    @Test
    public void testNestedMessages() throws Exception {
        TestRequiredForeign.Builder builder = TestRequiredForeign.newBuilder();
        builder.addRepeatedMessage(TestRequired.newBuilder().setA(1).setB(3).setC(5).build());
        builder.addRepeatedMessage(TestRequired.newBuilder().setA(9).setB(11).setC(13).build());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TestRequiredForeign builtMsg = builder.build();
        formatter.print(builtMsg, output);
        TestRequiredForeign.Builder builder2 = TestRequiredForeign.newBuilder();
        formatter.merge(new ByteArrayInputStream(output.toByteArray()), builder2);
        assertEquals(builtMsg.toString(), builder2.build().toString());
    }
    
    @Test
    public void testNestedEmptyMessages() throws Exception {
        TestCamelCaseFieldNames.Builder builder = TestCamelCaseFieldNames.newBuilder();
        builder.addRepeatedMessageField(ForeignMessage.newBuilder().build());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TestCamelCaseFieldNames builtMsg = builder.build();
        formatter.print(builtMsg, output);
        TestCamelCaseFieldNames.Builder builder2 = TestCamelCaseFieldNames.newBuilder();
        formatter.merge(new ByteArrayInputStream(output.toByteArray()), builder2);
        assertEquals(builtMsg.toString(), builder2.build().toString());
    }
    
    @Test
    public void testEmptyMessage() throws Exception {
        TestCamelCaseFieldNames.Builder builder = TestCamelCaseFieldNames.newBuilder();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TestCamelCaseFieldNames builtMsg = builder.build();
        formatter.print(builtMsg, output);
        TestCamelCaseFieldNames.Builder builder2 = TestCamelCaseFieldNames.newBuilder();
        formatter.merge(new ByteArrayInputStream(output.toByteArray()), builder2);
        assertEquals(builtMsg.toString(), builder2.build().toString());
    }
    
    @Test
    public void testPrintUnknownFields() throws Exception {
        // Test printing of unknown fields in a message.

        TestEmptyMessage message = TestEmptyMessage.newBuilder().setUnknownFields(UnknownFieldSet.newBuilder().addField(5,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addFixed32(2).addFixed64(3).addLengthDelimited(ByteString.copyFromUtf8("4")).addGroup(UnknownFieldSet.newBuilder().addField(10,
                UnknownFieldSet.Field.newBuilder().addVarint(5).build()).build()).build()).addField(8,
                UnknownFieldSet.Field.newBuilder().addVarint(1).addVarint(2).addVarint(3).build()).addField(15,
                UnknownFieldSet.Field.newBuilder().addVarint(0xABCDEF1234567890L).addFixed32(0xABCD1234).addFixed64(0xABCDEF1234567890L).build()).build()).build();

        assertEquals(
                XML_VERSION + "<TestEmptyMessage><unknown-field index=\"5\">1</unknown-field><unknown-field index=\"5\">0x00000002</unknown-field><unknown-field index=\"5\">0x0000000000000003</unknown-field><unknown-field index=\"5\">4</unknown-field><unknown-field index=\"5\"><unknown-field index=\"10\">5</unknown-field></unknown-field><unknown-field index=\"8\">1</unknown-field><unknown-field index=\"8\">2</unknown-field><unknown-field index=\"8\">3</unknown-field><unknown-field index=\"15\">12379813812177893520</unknown-field><unknown-field index=\"15\">0xabcd1234</unknown-field><unknown-field index=\"15\">0xabcdef1234567890</unknown-field></TestEmptyMessage>",
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
                XML_VERSION + "<message><unknown-field index=\"5\">1</unknown-field><unknown-field index=\"5\">0x00000002</unknown-field><unknown-field index=\"5\">0x0000000000000003</unknown-field><unknown-field index=\"5\">4</unknown-field><unknown-field index=\"5\"><unknown-field index=\"10\">5</unknown-field></unknown-field><unknown-field index=\"8\">1</unknown-field><unknown-field index=\"8\">2</unknown-field><unknown-field index=\"8\">3</unknown-field><unknown-field index=\"15\">12379813812177893520</unknown-field><unknown-field index=\"15\">0xabcd1234</unknown-field><unknown-field index=\"15\">0xabcdef1234567890</unknown-field></message>",  
        		formatter.printToString(fieldSet));
    }
    
    @Test
    public void testPathInProperty() throws IOException {
        final String value = "/some/path/to/something?param=1";
        OneString msg = OneString.newBuilder().setData(value).build();
        String xmlText = formatter.printToString(msg);

        assertEquals(XML_VERSION + "<OneString><data>" + value + "</data></OneString>", xmlText);
        OneString.Builder builder = OneString.newBuilder();        
        formatter.merge(TextUtils.toInputStream(xmlText), builder);
        assertEquals(builder.build().toString(), msg.toString());
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
        
        ProtobufFormatter formatterXmlOrig = formatFactory.createFormatter(Formatter.XML);
        String xmlTextOrig = formatterXmlOrig.printToString(TestUtil.getAllExtensionsSet());
        assertEquals(XML_VERSION + xmlTextOrig, xmlText);

        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        formatter.merge(TextUtils.toInputStream(xmlText), TestUtil.getExtensionRegistry(), builder);
        assertEquals(TestUtil.getAllExtensionsSet(), builder.build());
    }

 
    @Test
    public void testStackOverflow() throws Exception {
        TestAllTypes bd = TestAllTypes.newBuilder().setDefaultBytes(ByteString.copyFrom(new byte[1024])).build();
        String xmlText = formatter.printToString(bd);
        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(xmlText), builder);
    }
    
    @Test
    public void testSpacesInStringValues() throws Exception {
        String spacedString = " name with spaces ";
        OneString msg = OneString.newBuilder().setData(spacedString).build();
        String itemTxt = formatter.printToString(msg);
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(itemTxt),  builder);
        assertEquals(spacedString, builder.build().getData());
    }
    

    @Test
    public void testOptionalGroup() throws Exception {
        TestAllTypes allTypes = TestAllTypes.newBuilder().setDefaultInt32(123).setOptionalInt64(456l).setOptionalString("foo").setOptionalImportMessage(ImportMessage.newBuilder().setD(123)).build();
        String javaText = formatter.printToString(allTypes);

        TestAllTypes.Builder builder = TestAllTypes.newBuilder();
        formatter.merge(TextUtils.toInputStream(javaText), builder);
        TestAllTypes built = builder.build();
        assertEquals(allTypes, built);
        assertEquals(123, built.getOptionalImportMessage().getD());
    }
    
    @Test
    public void testUnknown() throws Exception {
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
        
        String xmlText = formatter.printToString(data);
//System.out.println(xmlText);
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(xmlText), builder);
        assertEquals(data.getData(), builder.build().getData());
    }
    
    @Test
    public void testUnknownParseOrder() throws Exception {
        String unknownXml = XML_VERSION + "<OneString><unknown-field index=\"5\">0x000003e7</unknown-field><unknown-field index=\"6\">testUnknown</unknown-field><data>12345</data></OneString>";
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(unknownXml), builder);
        OneString built = builder.build();
        assertEquals("12345", built.getData());
    }
    
    @Test(expected=IOException.class)
    public void testMalformedXml() throws Exception {
        String badXml = XML_VERSION + "<OneString><data>12345</OneString>";
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(badXml), builder);
    }
    
    @Test(expected=IOException.class)
    public void testMalformedXml2() throws Exception {
        String badXml = XML_VERSION + "<OneString>12345</data></OneString>";
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(badXml), builder);
    }
    

    @Test
    public void testNoVersionXml() throws Exception {
        String xml = "<OneString><data>12345</data></OneString>";
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(xml), builder);
        assertEquals("12345", builder.getData());
    }
    
    @Test
    public void testWhitespaceXml() throws Exception {
        String xml = " <OneString>  <data>12345</data>       </OneString>";
        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(TextUtils.toInputStream(xml), builder);
        assertEquals("12345", builder.getData());
    }
    
    @Test
    public void testNestedExtension() throws Exception {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        UnittestProto.registerAllExtensions(registry);
        
        UnittestProto.TestAllExtensions tae = UnittestProto.TestAllExtensions.newBuilder().setExtension(TestNestedExtension.test, "aTest").build();
        String output = formatter.printToString(tae);
  //System.out.println(output);
        UnittestProto.TestAllExtensions.Builder builder = UnittestProto.TestAllExtensions.newBuilder();
        formatter.merge(TextUtils.toInputStream(output), registry, builder);
        String value = builder.build().getExtension(TestNestedExtension.test);
        assertEquals("aTest", value);
    }
    
    @Test
    public void testChineseCharacters() throws Exception {
        String data = "検索jan5検索.8@test.relay.symantec.com";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><OneString><data>" + data + "</data></OneString>";

        OneString.Builder builder = OneString.newBuilder();
        formatter.merge(
                TextUtils.toInputStream(xml, Charset.forName("UTF-8")), 
                builder);
        OneString msg = builder.build();

        //System.out.println(msg.getData());
        assertEquals(data, msg.getData());
    }
}
