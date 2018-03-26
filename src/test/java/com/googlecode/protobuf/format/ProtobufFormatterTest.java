package com.googlecode.protobuf.format;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import protobuf_unittest.UnittestProto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotEquals;

public abstract class ProtobufFormatterTest<T extends ProtobufFormatter> {
    private T protobufFormatter;
    private Charset platformDefaultCharset = Charset.defaultCharset();
    private Charset nonDefaultCharset = Charset.forName("UTF-16");
    private String testData = "A";
    private final String testDataAsJson = "\"" + testData + "\"";
    private UnittestProto.OneString testProto = UnittestProto.OneString.newBuilder().setData(testData).build();

    protected abstract T getFormatterUnderTest();

    @BeforeMethod
    public void setUp() throws Exception {
        protobufFormatter = getFormatterUnderTest();
    }

    @Test
    public void validateTestSetUp() {
        //test is redundant unless default and non default charsets produce different results
        assertNotEquals(platformDefaultCharset, nonDefaultCharset);
        byte[] nonDefaultBytes = testData.getBytes(nonDefaultCharset);
        assertNotEquals(testData.getBytes(platformDefaultCharset), nonDefaultBytes);
        assertNotEquals(testData, new String(nonDefaultBytes, platformDefaultCharset));
    }

    @Test
    public void canSerialiseToBytesUsingNonDefaultCharset() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        protobufFormatter.print(testProto, output, nonDefaultCharset);
        assertThat(output.toString(nonDefaultCharset.name()), containsString(testDataAsJson));
    }

    @Test
    public void canSerialiseUsingNonDefaultCharSet() {
        protobufFormatter.setDefaultCharset(nonDefaultCharset);
        UnittestProto.OneString message = UnittestProto.OneString.newBuilder().setData(testData).build();
        assertThat(protobufFormatter.printToString(message), containsString(testDataAsJson));
    }
}