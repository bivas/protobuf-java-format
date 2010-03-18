package com.google.protobuf;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aantonov
 * Date: Mar 16, 2010
 * Time: 4:06:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CouchDBFormat extends JsonFormat {

    /**
     * Outputs a textual representation of the Protocol Message supplied into the parameter output.
     * (This representation is the new version of the classic "ProtocolPrinter" output from the
     * original Protocol Buffer system)
     */
    public static void print(Message message, Appendable output) throws IOException {
        CouchDBGenerator generator = new CouchDBGenerator(output);
        generator.print("{");
        print(message, generator);
        generator.print("}");
    }

    /**
     * Outputs a textual representation of {@code fields} to {@code output}.
     */
    public static void print(UnknownFieldSet fields, Appendable output) throws IOException {
        CouchDBGenerator generator = new CouchDBGenerator(output);
        generator.print("{");
        printUnknownFields(fields, generator);
        generator.print("}");
    }

    /**
     * Like {@code print()}, but writes directly to a {@code String} and returns it.
     */
    public static String printToString(Message message) {
        try {
            StringBuilder text = new StringBuilder();
            print(message, text);
            return text.toString();
        } catch (IOException e) {
            throw new RuntimeException("Writing to a StringBuilder threw an IOException (should never happen).",
                                       e);
        }
    }

    /**
     * Like {@code print()}, but writes directly to a {@code String} and returns it.
     */
    public static String printToString(UnknownFieldSet fields) {
        try {
            StringBuilder text = new StringBuilder();
            print(fields, text);
            return text.toString();
        } catch (IOException e) {
            throw new RuntimeException("Writing to a StringBuilder threw an IOException (should never happen).",
                                       e);
        }
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     */
    public static void merge(Readable input, Message.Builder builder) throws IOException {
        merge(input, ExtensionRegistry.getEmptyRegistry(), builder);
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     */
    public static void merge(CharSequence input, Message.Builder builder) throws ParseException {
        merge(input, ExtensionRegistry.getEmptyRegistry(), builder);
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     * Extensions will be recognized if they are registered in {@code extensionRegistry}.
     */
    public static void merge(Readable input,
                             ExtensionRegistry extensionRegistry,
                             Message.Builder builder) throws IOException {
        // Read the entire input to a String then parse that.

        // If StreamTokenizer were not quite so crippled, or if there were a kind
        // of Reader that could read in chunks that match some particular regex,
        // or if we wanted to write a custom Reader to tokenize our stream, then
        // we would not have to read to one big String. Alas, none of these is
        // the case. Oh well.

        merge(JsonFormat.toStringBuilder(input), extensionRegistry, builder);
    }

    /**
     * Parse a text-format message from {@code input} and merge the contents into {@code builder}.
     * Extensions will be recognized if they are registered in {@code extensionRegistry}.
     */
    public static void merge(CharSequence input,
                             ExtensionRegistry extensionRegistry,
                             Message.Builder builder) throws ParseException {
        Tokenizer tokenizer = new Tokenizer(input);

        // Based on the state machine @ http://json.org/

        tokenizer.consume("{"); // Needs to happen when the object starts.
        while (!tokenizer.tryConsume("}")) { // Continue till the object is done
            JsonFormat.mergeField(tokenizer, extensionRegistry, builder);
        }
    }

    protected static class Tokenizer extends JsonFormat.Tokenizer {

        /**
         * Construct a tokenizer that parses tokens from the given text.
         */
        public Tokenizer(CharSequence text) {
            super(text);
        }

        @Override
        public String consumeIdentifier() throws ParseException {
            String id = super.consumeIdentifier();
            if ("_id".equals(id)) {
                return "id";
            } else if ("_rev".equals(id)) {
                return "rev";
            }
            return id;
        }
    }

    protected static class CouchDBGenerator extends JsonFormat.JsonGenerator {

        public CouchDBGenerator(Appendable output) {
            super(output);
        }

        @Override
        public void print(CharSequence text) throws IOException {
            if ("id".equals(text)) {
                super.print("_id");
            } else if ("rev".equals(text)) {
                super.print("_rev");
            } else {
                super.print(text);
            }
        }
    }
}
