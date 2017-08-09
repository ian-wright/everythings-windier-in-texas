name := "ercot_wind"
version := "1.0"

scalaVersion := "2.11.1"

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % sparkVersion,
  "com.pubnub" % "pubnub" % "3.5.6",
  "org.json4s" % "json4s-native_2.11" % "3.5.3"
)



//libraryDependencies += "com.vividsolutions" % "jts-core" % "1.14.0"
//libraryDependencies += "org.wololo" % "jts2geojson" % "0.11.0"
//libraryDependencies += "com.vividsolutions" % "jts" % "1.13"

//libraryDependencies += "org.locationtech.spatial4j" % "spatial4j" % "0.6"
//libraryDependencies += "org.noggit" % "noggit" % "0.8"