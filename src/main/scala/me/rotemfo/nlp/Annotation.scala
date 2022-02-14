package me.rotemfo.nlp

import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions.{col, desc, length}

object Annotation {
  def main(args: Array[String]): Unit = {
    val sparkSessionWithConfig = SparkFactory.getSparkSession(args)
    val spark = sparkSessionWithConfig.spark
    val config = sparkSessionWithConfig.config


    val id = "id"
    val content = "content"
    val contentLength = s"${content}_length"

    val df = spark.read.parquet(config.inputPath.get).select(id, content)
      .withColumn(contentLength, length(col(content)))
      .orderBy(desc(contentLength))
      .select(content)
      .limit(10)
    val explainDocumentPipeline = PretrainedPipeline("explain_document_ml")
    val annotationsDf = explainDocumentPipeline.transform(df)
    annotationsDf.write.mode(SaveMode.Overwrite).parquet(config.outputPath.get)
    spark.close()
  }
}
