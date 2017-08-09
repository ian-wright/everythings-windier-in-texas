package wind

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.rdd.RDD

import WeatherConsumer._
import PriceConsumer._

import com.pubnub.api.{Callback, Pubnub, PubnubError}

import org.json4s.native.Json
import org.json4s.DefaultFormats


object Streamer {
  def main(args: Array[String]) {

    // create pubnub publishing client
    val pubKey = "pub-c-9802bbd9-36b8-45db-a00b-c3a3d6bb0d2f"
    val subKey = "sub-c-97c0cd06-7a02-11e7-9c85-0619f8945a4f"
    val pubnub = new Pubnub(pubKey, subKey)
    val callback = new Callback() {
      override def successCallback(channel: String, response: Object): Unit =
        println(response.toString + channel)

      override def errorCallback(channel: String, pubnubError: PubnubError): Unit =
        println(pubnubError.getErrorString + channel)
    }

    // Create Spark Streaming context with 1 second batch interval
    val master: String = "local[4]"
    val sparkConf = new SparkConf().setAppName("wind").setMaster(master)
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    // Create direct kafka stream with brokers and topics
    val topicsSet = Set("weather", "price")
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> "localhost:9092",
      "group.id" -> "consumer",
      "auto.offset.reset" -> "largest"
    )

    // start kafka offsets at 0 (broker configured to clear cache after 1 second)
    val fromOffsets: Map[TopicAndPartition, Long] = Map(
      TopicAndPartition("weather", 0) -> 0,
      TopicAndPartition("price", 0) -> 0
    )

    // Create the Spark direct connection to Kafka broker
    val data = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, Map[String, String]](
      ssc,
      kafkaParams,
      fromOffsets,
      (mmd: MessageAndMetadata[String, String]) => Map("topic" -> mmd.topic, "message" -> mmd.message)
    )

    def parseData(data: Map[String, String]): Map[String, Any] = {
      val topic = data getOrElse ("topic", "throwaway")
      val message = data getOrElse ("message", "throwaway")

      // check Kafka topic, and handle message accordingly
      if (topic == "weather") {
        val lines = parseWeather(message)
        cleanWeather(lines)

      } else if (topic == "price") {
        val lines = parsePrice(message)
        val filteredLines = lines.filter(priceOrCoord)
        cleanPrice(filteredLines)

      } else {
        Map("valid" -> 0)
      }
    }

    def isValid(report: Map[String, Any]): Boolean = {
      val check = report getOrElse("valid", 0)
      check == 1
    }

    // publish stream of msgs to PubNub
    def publishEvents(rdd: RDD[Map[String, Any]]): Unit = {
      val reports = rdd.collect()
      reports.foreach { report =>
        val msg = Json(DefaultFormats).write(report).replaceAll("\"", "'")
        pubnub.publish("wind", msg, callback)
        println(msg)
      }
    }

    data.map(parseData).filter(isValid).foreachRDD(publishEvents _)

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}

