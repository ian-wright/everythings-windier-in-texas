name := "ercot_wind"
version := "1.0"

scalaVersion := "2.11.1"

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % sparkVersion,
  "com.pubnub" % "pubnub" % "3.5.6"
)


libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.5.3"

//libraryDependencies += "com.vividsolutions" % "jts-core" % "1.14.0"
//libraryDependencies += "org.wololo" % "jts2geojson" % "0.11.0"
//
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5"
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
//dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.5"

//libraryDependencies += "com.vividsolutions" % "jts" % "1.13"

//libraryDependencies += "org.locationtech.spatial4j" % "spatial4j" % "0.6"
//libraryDependencies += "org.noggit" % "noggit" % "0.8"