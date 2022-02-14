package me.rotemfo.nlp

case class NlpConfig(inputPath: Option[String] = None,
                     outputPath: Option[String] = None,
                     isLocal: Boolean = false)
