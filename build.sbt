name := "jackson-module-caseclass"

scalaVersion := "2.12.8"

val jacksonVersion = "2.9.8"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,

  // test dependencies
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion % "test",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-guava" % jacksonVersion % "test",
  "com.fasterxml.jackson.module" % "jackson-module-jsonSchema" % jacksonVersion % "test",
  
  "org.scalatest" %% "scalatest" % "3.0.6-SNAP3" % "test",
  "junit" % "junit" % "4.12" % "test"
)