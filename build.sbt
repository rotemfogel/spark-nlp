ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "spark-nlp",
    resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
      Resolver.jcenterRepo,
      Resolver.mavenLocal
    ),
    // @formatter:off
    libraryDependencies ++= {
      Seq(
        "org.apache.spark"      %% "spark-mllib"    % "3.1.2", // % Provided,
        "com.johnsnowlabs.nlp"  %% "spark-nlp"      % "3.4.0", // % Provided,
        "com.github.scopt"      %% "scopt"          % "4.0.1"
      )
    },
    // @formatter:on
    Compile / scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-target:jvm-1.8",
      "-deprecation",
      "-feature",
      "-unchecked",
      // Fail the compilation if there are any warnings
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Yrangepos",
      "-Ywarn-dead-code",
      "-Ywarn-nullary-unit",
      "-Ywarn-unused-import"
    ),
    assemblyPackageScala / assembleArtifact := false,
    assemblyMergeStrategy := {
      // @formatter:off
      case "module-info.class"                                => MergeStrategy.discard
      case PathList("javax", "servlet", _@_*)                 => MergeStrategy.first
      case PathList("org","tensorflow","NativeLibrary.class") => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".html"        => MergeStrategy.first
      case "log4j.propreties"                                 => MergeStrategy.first
      // ----
      // required for spark-sql to read different data types (e.g. parquet/orc/csv...)
      // ----
      case PathList("META-INF", "services", _@_*)             => MergeStrategy.first
      case PathList("META-INF", _@_*)                         => MergeStrategy.discard
      case _                                                  => MergeStrategy.deduplicate
      // @formatter:on
    }
  )

