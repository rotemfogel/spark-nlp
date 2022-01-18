package me.rotemfo.nlp

case class CommentsConfig(inputPath: Option[String] = None,
                          outputPath: Option[String] = None,
                          isLocal: Boolean = false)
