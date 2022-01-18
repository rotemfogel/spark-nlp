package me.rotemfo.nlp

import scopt.OptionParser

// @formatter:off
object CommentsParser extends OptionParser[CommentsConfig](programName = "comments-app") {
  opt[String](name = "input-path").required.action { (x, p) => p.copy(inputPath = Some(x)) }
    .validate(x => if (x.isEmpty) failure("must provide input-path") else success)
  opt[String](name = "output-path").required.action { (x, p) => p.copy(outputPath = Some(x)) }
    .validate(x => if (x.isEmpty) failure("must provide output-path") else success)
  opt[Unit]("local")                 .action { (_, p) => p.copy(isLocal = true) }
}
// @formatter:on