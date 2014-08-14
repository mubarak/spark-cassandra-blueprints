/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import sbt.Keys._

import scala.language.postfixOps

object Settings extends Build {

  lazy val buildSettings = Seq(
    name := "Spark Cassandra Connector Project Blueprints",
    normalizedName := "spark-cassandra-blueprints",
    organization := "com.helenaedelson",
    organizationHomepage := Some(url("http://www.github.com/helena/spark-cassandra-blueprints")),
    version in ThisBuild := "1.0.0-SNAPSHOT",
    scalaVersion := Versions.Scala,
    homepage := Some(url("https://github.com/datastax/helena/spark-cassandra-blueprints")),
    licenses := Seq(("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))
  )

  lazy val defaultSettings = Seq(
    scalacOptions in (Compile, doc) ++= Seq("-implicits","-doc-root-content", "rootdoc.txt"),
    scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-${Versions.JDK}", "-deprecation", "-feature", "-language:_", "-unchecked", "-Xlint"),
    javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", Versions.JDK, "-target", Versions.JDK, "-Xlint:unchecked", "-Xlint:deprecation"),
    ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,
    parallelExecution in ThisBuild := false,
    parallelExecution in Global := false
  )

  override lazy val settings = super.settings ++ buildSettings ++ Seq(shellPrompt := ShellPrompt.prompt)
}

/**
 * TODO make plugin
 * Shell prompt which shows the current project, git branch
 */
object ShellPrompt {

  def gitBranches = ("git branch" lines_! devnull).mkString

  def current: String = """\*\s+([\.\w-]+)""".r findFirstMatchIn gitBranches map (_ group 1) getOrElse "-"

  def currBranch: String = ("git status -sb" lines_! devnull headOption) getOrElse "-" stripPrefix "## "

  lazy val prompt = (state: State) =>
    "%s:%s:%s> ".format("spark-cassandra-blueprints", Project.extract (state).currentProject.id, currBranch)

  object devnull extends ProcessLogger {
    def info(s: => String) {}
    def error(s: => String) {}
    def buffer[T](f: => T): T = f
  }
}