package wind

//import com.vividsolutions.jts.geom._
//import com.vividsolutions.jts.geom.Coordinate
//import com.vividsolutions.jts.geom.GeometryFactory
//import org.wololo.jts2geojson.GeoJSONReader

object WeatherConsumer extends Serializable {

  val windKeys: String = "wind_speed_kt longitude latitude"

//  val geoString = scala.io.Source.fromFile("texas.geojson").mkString
//  println(geoString)
//  val reader = new GeoJSONReader()
//  val texas: Geometry = reader.read(geoString)
//  val factory = new GeometryFactory()

  val latBnds: Array[Double] = Array(25.85172, 36.500326)
  val lngBnds: Array[Double] = Array(-106.609084, -93.569257)


  def parseWeather(report: String): Array[String] = {
    val items = report.replaceAll("[{}\"\\s]", "").split(",")
    items.filter(keyCheck).sorted
  }

  def keyCheck(key: String): Boolean = {
    val cleanKey = key.split(":")(0)
    if (windKeys contains cleanKey) true else false
  }

  def cleanWeather(reportArray: Array[String]): Map[String, Any] = {

    // assume a valid response
    var response: Map[String, Any] = Map(
      "valid" -> 1,
      "type" -> "weather"
    )

    // only weather report arrays having all three filtered elements are considered wind
    if (reportArray.length == 3) {

      val lat = reportArray(0).split(":")(1).toFloat
      val lng = reportArray(1).split(":")(1).toFloat

      // TODO - serialize a geospatial lib and do this properly
      if ((lat >= latBnds(0)) && (lat <= latBnds(1)) && (lng >= lngBnds(0)) && (lng <= lngBnds(1))) {
        println("valid weather")

        response = response ++ Map(
          "latitude" -> lat,
          "longitude" -> lng,
          "value" -> reportArray(2).split(":")(1).toFloat
        )

      } else response = Map("valid" -> 0)


      //val coords = new Coordinate(lat, lng)
      //val point = new Point(coords, factory)
      //val point = factory.createPoint(new Coordinate(lng, lat))
    } else response = Map("valid" -> 0)
    response
  }
}