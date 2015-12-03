Fork from http://code.google.com/p/protobuf-java-format/

[![Build Status](https://travis-ci.org/bivas/protobuf-java-format.svg?branch=master)](https://travis-ci.org/bivas/protobuf-java-format)

## Description

This fork adds control on how the protobuf enums are encoded. The original code is able to decode enums by either their name or their integer value depending on what is found in the encoded stream.
It always and only encoded the enum values by name though: with this version it's possible to control that behaviour by an option provided by a (new) builder.

##Example
ProtobufFormatter jsonFormat =
            new FormatFactory()
                    .createFormatter(FormatFactory.Formatter.JSON)
                    .withEnumWriteMode(EnumWriteMode.NAME)            // This is the default behaviour
                    .build();
                    
ProtobufFormatter jsonFormat =
            new FormatFactory()
                    .createFormatter(FormatFactory.Formatter.JSON)
                    .withEnumWriteMode(EnumWriteMode.NUMBER)          // This is the new behaviour
                    .build();

