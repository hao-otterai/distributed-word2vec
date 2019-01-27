import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
object  mapTest{
def main(args: Array[String]) = {
val spark = SparkSession.builder.appName("mapExample").master("local").getOrCreate()
val data = spark.read.textFile("~/Downloads/test.txt").rdd
val mapFile = data.map(line => (line,line.length))
mapFile.foreach(println)
}
}
System.exit(0)
