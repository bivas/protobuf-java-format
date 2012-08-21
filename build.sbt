organization := "com.libitec"

name := "protobuf-java-format"

version := "0.1.4"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "2.4.1" withSources(),
  "org.codehaus.jackson" % "jackson-smile" % "1.9.7",
  "junit" % "junit" % "4.8.2" % "test",
  "org.easymock" % "easymock" % "3.0" % "test"
)
