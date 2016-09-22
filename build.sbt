lazy val `lti-commander` = (project in file(".")).
  settings(
    organization := "net.paulgray",
    name := "lti-commander",
    version := "1.0.0",
    scalaVersion := "2.11.2"
  )

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  //"commons-codec" % "commons-codec" % "1.10",
  "pl.project13.scala" %% "rainbow" % "0.2",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.apache.httpcomponents" % "httpclient" % "4.3.3",
  "org.imsglobal" % "basiclti-util" % "1.1.3-SNAPSHOT",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5",
  "log4j" % "log4j" % "1.2.17",

  "com.github.jsonld-java" % "jsonld-java" % "0.8.3",

  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

mainClass in assembly := Some("net.paulgray.lticommander.LtiCommander")

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

sourceDirectories in Compile += new File("source")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}