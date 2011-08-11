/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package com.googlecode.protobuf.format.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.CharBuffer;

public class TextUtils {

	/**
     * Convert an unsigned 64-bit integer to a string.
     */
	 public static String unsignedToString(final long value) {
        if (value >= 0) {
            return Long.toString(value);
        } else {
            // Pull off the most-significant bit so that BigInteger doesn't think
            // the number is negative, then set it again using setBit().
            return BigInteger.valueOf(value & 0x7FFFFFFFFFFFFFFFL).setBit(63).toString();
        }
    }
	 
    /**
     * Convert an unsigned 32-bit integer to a string.
     */
    public static String unsignedToString(final int value) {
        if (value >= 0) {
            return Integer.toString(value);
        } else {
            return Long.toString((value) & 0x00000000FFFFFFFFL);
        }
    }
    
    /**
     * Convert an unsigned 32-bit integer to a string.
     */
    public static Integer unsignedInt(int value) {
        if (value < 0) {
            return (int) ((value) & 0x00000000FFFFFFFFL);
        }
        return value;
    }

    /**
     * Convert an unsigned 64-bit integer to a string.
     */
    public static Long unsignedLong(long value) {
        if (value < 0) {
            // Pull off the most-significant bit so that BigInteger doesn't think
            // the number is negative, then set it again using setBit().
            return BigInteger.valueOf(value & 0x7FFFFFFFFFFFFFFFL).setBit(63).longValue();
        }
        return value;
    }

    /** 
     * Is this a hex digit? 
     */
    public static boolean isHex(final char c) {
      return ('0' <= c && c <= '9') ||
             ('a' <= c && c <= 'f') ||
             ('A' <= c && c <= 'F');
    }
    
    /** 
     * Is this an octal digit? 
     */
    public static boolean isOctal(final char c) {
      return '0' <= c && c <= '7';
    }

    /**
     * Interpret a character as a digit (in any base up to 36) and return the
     * numeric value.  This is like {@code Character.digit()} but we don't accept
     * non-ASCII digits.
     */
    public static int digitValue(final char c) {
      if ('0' <= c && c <= '9') {
        return c - '0';
      } else if ('a' <= c && c <= 'z') {
        return c - 'a' + 10;
      } else {
        return c - 'A' + 10;
      }
    }
    
    private static final int BUFFER_SIZE = 4096;

    // TODO(chrisn): See if working around java.io.Reader#read(CharBuffer)
    // overhead is worthwhile
    public static StringBuilder toStringBuilder(Readable input) throws IOException {
        StringBuilder text = new StringBuilder();
        CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
        while (true) {
            int n = input.read(buffer);
            if (n == -1) {
                break;
            }
            buffer.flip();
            text.append(buffer, 0, n);
        }
        return text;
    }
    
    public static InputStream toInputStream(String input) {
    	return new ByteArrayInputStream(input.getBytes());
    }
}
