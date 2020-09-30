//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.34")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.7"