package wind


object PriceConsumer extends Serializable {

  // Parsing .kml-style strings
  def parsePrice(report: String): Array[String] = {
    report.replaceAll("[ +:;]", "").replaceAll("&nbsp", "").split("\r\n")
  }

  // Only need kml file rows that contain price or location information
  def priceOrCoord(item: String): Boolean = {
    (item contains "Price") || (item contains "coordinates")
  }

  def cleanPrice(reportArray: Array[String]): Map[String, Any] = {
    // assuming a valid response
    var response: Map[String, Any] = Map(
      "valid" -> 1,
      "type" -> "price"
    )

    reportArray.foreach{ item =>

      // regex to extract price value from string
      if (item contains "Price") {
        val pattern = "[0-9]+\\.[0-9]+".r
        val value = pattern.findFirstIn(item).getOrElse("")
        if (value != "") {
          response = response ++ Map("value" -> value.toFloat)
        } else response = Map("valid" -> 0)

        // regex to extract coordinates from string
      } else if (item contains "coordinates") {
        val pattern = "(?<=<coordinates>).*?(?=</coordinates>)".r
        val rawCoord = pattern.findFirstIn(item).getOrElse("")
        if (rawCoord != "") {
          val coords = rawCoord.slice(0, rawCoord.length - 2).split(",")
          response = response ++ Map(
            "latitude" -> coords(1).toFloat,
            "longitude" -> coords(0).toFloat
          )
        } else response = Map("valid" -> 0)
      // should never execute
      } else response = Map("valid" -> 0)
    }
    response
  }
}