package com.googlecode.protobuf.format;
/* 
	Copyright (c) 2009, Orbitz World Wide
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, 
	are permitted provided that the following conditions are met:

		* Redistributions of source code must retain the above copyright notice, 
		  this list of conditions and the following disclaimer.
		* Redistributions in binary form must reproduce the above copyright notice, 
		  this list of conditions and the following disclaimer in the documentation 
		  and/or other materials provided with the distribution.
		* Neither the name of the Orbitz World Wide nor the names of its contributors 
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.smile.SmileFactory;
import org.codehaus.jackson.smile.SmileGenerator;
import org.codehaus.jackson.smile.SmileParser;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;

/**
 * Provide ascii text parsing and formatting support for proto2 instances. The implementation
 * largely follows google/protobuf/text_format.cc.
 * <p>
 * (c) 2011 Neustar, Inc. All Rights Reserved.
 *
 * @author jeffrey.damick@neustar.biz Jeffrey Damick
 *         Based on the original code by:
 * @author eliran.bivas@gmail.com Eliran Bivas
 * @author aantonov@orbitz.com Alex Antonov
 *         <p/>
 * @author wenboz@google.com Wenbo Zhu
 * @author kenton@google.com Kenton Varda
 */
public class SmileFormat extends JsonJacksonFormat {
    private static SmileFactory smileFactory = new SmileFactory();

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     * Extensions will be recognized if they are registered in {@code extensionRegistry}.
     * @throws IOException 
     */
    public void merge(InputStream input, Charset cs,
    		ExtensionRegistry extensionRegistry, Message.Builder builder) throws IOException {
    	
    	SmileParser parser = smileFactory.createJsonParser(input); 
    	merge(parser, extensionRegistry, builder);
    }
    
    protected JsonGenerator createGenerator(OutputStream output) throws IOException {
    	SmileGenerator generator = smileFactory.createJsonGenerator(output);
    	generator.enable(SmileGenerator.Feature.WRITE_HEADER);
    	generator.enable(SmileGenerator.Feature.WRITE_END_MARKER);
    	generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    	return generator;
    }
}
