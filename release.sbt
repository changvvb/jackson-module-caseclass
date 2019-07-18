
publishMavenStyle := true

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials_sonatype")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

organization := "com.github.changvvb"

homepage := Some(url("https://github.com/changvvb/jackson-module-caseclass"))

pomExtra := {
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:git@github.com:changvvb/jackson-module-caseclass.git</connection>
      <developerConnection>scm:git:git@github.com:changvvb/jackson-module-caseclass.git</developerConnection>
      <url>https://github.com/changvvb/jackson-module-caseclass</url>
    </scm>
    <developers>
      <developer>
        <id>changvvb</id>
        <name>Weiwei Chang</name>
        <email>changvvb@gmail.com</email>
      </developer>
    </developers>
}

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)