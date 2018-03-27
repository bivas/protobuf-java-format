package com.googlecode.protobuf.format;

import com.google.protobuf.Descriptors;
import com.googlecode.protobuf.format.issue23.Issue23;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;
import java.text.DecimalFormat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kyakkala on 3/22/2018.
 */
public class XmlFormatTest {

    @Test
    public void testDecimalFormat() throws IOException {

        Issue23.MsgWithUnknownFields msg = Issue23.MsgWithUnknownFields.newBuilder()
                .setLeaf4(12345670678.01245)
                .build();
        XmlFormat xmlFormat = new XmlFormat();

        DecimalFormat decimalFormat = new DecimalFormat("#");
        decimalFormat.setMaximumFractionDigits(4);
        xmlFormat.formatter.put(Descriptors.FieldDescriptor.Type.DOUBLE, decimalFormat);

        assertThat(xmlFormat.printToString(msg).toString(),
                is(Files.readFile(XmlFormatTest.class
                .getResourceAsStream(
                "/expectations/xmlFormatTest/xml_format_without_exponent_notation.txt")).trim()));

    }
}
