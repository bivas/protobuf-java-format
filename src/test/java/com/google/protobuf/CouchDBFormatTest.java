package com.google.protobuf;

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

    public void testGeneral() throws JsonFormat.ParseException {
        UnittestCouchDbProto.Data msg = UnittestCouchDbProto.Data.newBuilder().
                setId("0647385e9c8e51f0df3ec18973ca0696").
                setRev("1-60df7591a733530f565481358664c0dd").
                build();
        
        String data = "{\"_id\": \"0647385e9c8e51f0df3ec18973ca0696\",\"_rev\": \"1-60df7591a733530f565481358664c0dd\",\"data1\": \"Some random text and what not\",\"msg\": {\"id\": 123, \"foo\": false}, \"arr\": [123, 456]}";
        UnittestCouchDbProto.Data.Builder builder = UnittestCouchDbProto.Data.newBuilder();
        CouchDBFormat.merge(data, builder);

        assertEquals(CouchDBFormat.printToString(msg), CouchDBFormat.printToString(builder.build()));
    }
}
