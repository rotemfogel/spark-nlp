package me.rotemfo.nlp

import com.johnsnowlabs.nlp.SparkNLP.MavenSpark32
import org.apache.spark.sql.SparkSession

case class SparkSessionWithConfig(spark: SparkSession,
                                  config: NlpConfig)

object SparkFactory {
  def getSparkSession(args: Array[String]): SparkSessionWithConfig = {
    val config = NlpParser.parse(args, NlpConfig()).get
    require(config.inputPath.isDefined && config.inputPath.isDefined)
    val builder = SparkSession.builder()
      .appName("Spark NLP")
      .config("spark.driver.memory", "16G")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "2000M")
      .config("spark.driver.maxResultSize", "0")
    val spark = (if (config.isLocal) {
      builder.master("local[*]")
        .config("spark.ui.enabled", "false")
        .config("spark.jars.packages", MavenSpark32)
    } else {
      builder
    }).getOrCreate()
    SparkSessionWithConfig(spark, config)
  }
}
