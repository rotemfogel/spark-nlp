package me.rotemfo.nlp

import com.johnsnowlabs.nlp.annotators.{Normalizer, Stemmer, StopWordsCleaner, Tokenizer}
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, IDF, IndexToString, StringIndexer}

import scala.Double.NaN

object Categorization {
  def main(args: Array[String]): Unit = {
    val sparkSessionWithConfig = SparkFactory.getSparkSession(args)
    val spark = sparkSessionWithConfig.spark
    val config = sparkSessionWithConfig.config

    val df = spark.read.parquet(config.inputPath.get)
      .select("primary_ticker", "content")

    val documentAssembler = new DocumentAssembler().setInputCol("content").setOutputCol("document")
    val tokenizer = new Tokenizer().setInputCols(Array("document")).setOutputCol("token")

    // clean tokens
    val normalizer = new Normalizer().setInputCols(Array("token")).setOutputCol("normalized")

    // remove stopwords
    val stopWordsCleaner = new StopWordsCleaner()
      .setInputCols("normalized")
      .setOutputCol("cleanTokens")
      .setCaseSensitive(false)
      .setLocale("en_US")

    // stems tokens to bring it to root form
    val stemmer = new Stemmer().setInputCols(Array("cleanTokens")).setOutputCol("stem")

    // Convert custom document structure to array of tokens.
    val finisher = new Finisher()
      .setInputCols(Array("stem"))
      .setOutputCols(Array("token_features"))
      .setOutputAsArray(true)
      .setCleanAnnotations(false)

    // generate Term Frequency
    val hashingTF = new HashingTF()
      .setInputCol("token_features")
      .setOutputCol("raw_features")
      .setNumFeatures(1000)

    // generate Inverse Document Frequency
    val idf = new IDF().setInputCol("raw_features").setOutputCol("features").setMinDocFreq(5)

    // convert labels (string) to integers. Easy to process compared to string.
    val labelStringIdx = new StringIndexer()
      .setInputCol("primary_ticker")
      .setOutputCol("label")
      .setHandleInvalid("skip")

    // define a simple Multinomial logistic regression model.
    // Try different combination of hyper-parameters and see what suits your data.
    // You can also try different algorithms and compare the scores.
    val lr = new LogisticRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.0)

    // To convert index(integer) to corresponding class labels
    val labelToStringIdx = new IndexToString().setInputCol("label").setOutputCol("article_class")

    // define the nlp pipeline
    val pipeline = new Pipeline().setStages(
      Array(
        documentAssembler,
        tokenizer,
        normalizer,
        stopWordsCleaner,
        stemmer,
        finisher,
        hashingTF,
        idf,
        labelStringIdx,
        lr,
        labelToStringIdx)
    )

    val splitDf = df.randomSplit(Array(0.7, 0.3), seed = 100)

    val trainingData = splitDf.head
    val model = pipeline.fit(trainingData)

    val testData = splitDf.last
    val predictions = model.transform(testData)

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    val accuracy = evaluator.evaluate(predictions)
    println(s"Accuracy = $accuracy")
    print(s"Test Error = ${1.0 - accuracy}")

    if (accuracy != NaN) {
      val outputPath = if (config.outputPath.get.endsWith("/")) {
        config.outputPath.get.dropRight(1)
      } else {
        config.outputPath.get
      }
      val modelOutputPath = s"$outputPath/model"
      model.write.overwrite.save(modelOutputPath)
    }
    spark.close()
  }
}
