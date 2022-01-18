# spark-nlp
spark-nlp

### submit
```
spark-submit --deploy-mode cluster \
  --conf spark.executor.extraJavaOptions=-XX:+UseG1GC --conf spark.sql.caseSensitive=true \
  --packages com.johnsnowlabs.nlp:spark-nlp_2.12:3.4.0 \
  --class me.rotemfo.nlp.CommentsApp spark-nlp-assembly-0.1.jar --input-path s3://imaginary-s3-bucket/source/comments/ \
  --output-path s3://imaginary-s3-bucket/target/comments/ 
```