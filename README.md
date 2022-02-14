# spark-nlp
spark-nlp

### submit
```
spark-submit --deploy-mode cluster \
  --conf spark.executor.extraJavaOptions=-XX:+UseG1GC \
  --conf spark.sql.caseSensitive=true \
  --conf spark.driver.memory=16G \
  --conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
  --conf spark.kryoserializer.buffer.max=2000M \
  --conf spark.driver.maxResultSize=0 \
  --packages com.johnsnowlabs.nlp:spark-nlp-spark32_2.12:3.4.0 \
  --class me.rotemfo.nlp.Categorization spark-nlp-assembly-0.1.jar \
  --input-path s3://imaginary-bucket/content/ \
  --output-path s3://imaginary-bucket/model/content/
```