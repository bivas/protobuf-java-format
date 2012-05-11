Fork from http://code.google.com/p/protobuf-java-format/


## Description

Provide serialization and de-serialization of different formats based on Google’s protobuf Message. Enables overriding the default (byte array) output to text based formats such as XML, JSON and HTML.

##Example
For XML output, use XmlFormat

```java
Message someProto = SomeProto.getDefaultInstance();
String xmlFormat = XmlFormat.printToString(someProto);
```

For XML input, use XmlFormat
```java
Message.Builder builder = SomeProto.newBuilder();
String xmlFormat = _load xml document from a source_;
XmlFormat.merge(xmlFormat, builder);
```

For Json output, use JsonFormat
```java
Message someProto = SomeProto.getDefaultInstance();
String jsonFormat = JsonFormat.printToString(someProto);
```

For Json input, use JsonFormat
```java
Message.Builder builder = SomeProto.newBuilder();
String jsonFormat = _load json document from a source_;
JsonFormat.merge(jsonFormat, builder);
```

For HTML output, use HtmlFormat
```java
Message someProto = SomeProto.getDefaultInstance();
String htmlFormat = HtmlFormat.printToString(someProto);
```
