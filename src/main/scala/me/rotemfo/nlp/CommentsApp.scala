package me.rotemfo.nlp

import com.johnsnowlabs.nlp.DocumentAssembler
import com.johnsnowlabs.nlp.SparkNLP.MavenSpark32
import com.johnsnowlabs.nlp.annotator.{Normalizer, SentenceDetector}
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.embeddings.WordEmbeddingsModel
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.{SaveMode, SparkSession}

object CommentsApp {
  def main(args: Array[String]): Unit = {
    val config = CommentsParser.parse(args, CommentsConfig()).get
    require(config.inputPath.isDefined && config.inputPath.isDefined)
    val builder = SparkSession.builder()
      .appName("Spark NLP Comments Application")
      .config("spark.driver.memory", "16G")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "2000M")
      .config("spark.driver.maxResultSize", "0")
    val spark = (if (config.isLocal) {
      builder.master("local[*]")
        .config("spark.jars.packages", MavenSpark32)
    } else {
      builder
    }).getOrCreate()
    val documentAssembler = new DocumentAssembler().setInputCol("content").setOutputCol("document")
    val sentenceDetector = new SentenceDetector().setInputCols(Array("document")).setOutputCol("sentences")
    val tokenizer = new Tokenizer().setInputCols(Array("sentences")).setOutputCol("token")
    val normalizer = new Normalizer().setInputCols(Array("token")).setOutputCol("normal")
    val wordEmbeddings = WordEmbeddingsModel.pretrained().setInputCols("document", "normal").setOutputCol("embeddings")
    val nlpPipeline = new Pipeline().setStages(Array(documentAssembler, sentenceDetector, tokenizer, normalizer, wordEmbeddings))
    val df = spark.read.parquet(config.inputPath.get)
    val pipelineModel = nlpPipeline.fit(df)
    val data = pipelineModel.transform(df)
    val outputPath = if (config.outputPath.get.endsWith("/")) {
      config.outputPath.get
    } else {
      config.outputPath.get.dropRight(1)
    }
    val modelOutputPath = s"$outputPath/model"
    val datasetOutputPath = s"$outputPath/dataset"
    pipelineModel.write.overwrite.save(modelOutputPath)
    data.write.mode(SaveMode.Overwrite).save(datasetOutputPath)
    spark.close()
  }
}
