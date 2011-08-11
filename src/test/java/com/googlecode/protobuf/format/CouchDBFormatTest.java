package com.googlecode.protobuf.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import com.googlecode.protobuf.format.CouchDBFormat;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.util.TextUtils;

import junit.framework.TestCase;
import protobuf_unittest.UnittestCouchDbProto;

/**
 * Created by IntelliJ IDEA.
 * User: aantonov
 * Date: Mar 16, 2010
 * Time: 4:07:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CouchDBFormatTest extends TestCase {

	@Test
    public void testGeneral() throws IOException {
        UnittestCouchDbProto.Data msg = UnittestCouchDbProto.Data.newBuilder().
                setId("0647385e9c8e51f0df3ec18973ca0696").
                setRev("1-60df7591a733530f565481358664c0dd").
                build();
        
        String data = "{\"_id\": \"0647385e9c8e51f0df3ec18973ca0696\",\"_rev\": \"1-60df7591a733530f565481358664c0dd\",\"data1\": \"Some random text and what not\",\"msg\": {\"id\": 123, \"foo\": false}, \"arr\": [123, 456]}";
        UnittestCouchDbProto.Data.Builder builder = UnittestCouchDbProto.Data.newBuilder();
        CouchDBFormat formatter = new CouchDBFormat();
        formatter.merge(TextUtils.toInputStream(data), builder);

        assertEquals(formatter.printToString(msg), formatter.printToString(builder.build()));
    }
}
