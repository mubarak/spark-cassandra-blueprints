# Spark Cassandra Blueprints
Templates for getting started or doing quick prototypes, building Scala applications with Spark and Cassandra. 

## Environment Pre-Requisites
If you already use Scala skip the SBT step. Similarly, if you already can spin up Cassandra locally, skip that step. 

### SBT: Scala Build Tool
We will be building and running with SBT

* [Download SBT](http://www.scala-sbt.org/download.html) 
* [SBT Setup](http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html) 

### Clone the repo and build the code

    git clone https://github.com/helena/spark-cassandra-blueprints.git
    cd spark-cassandra-blueprints
    sbt compile

[SBT Docs](http://www.scala-sbt.org/0.13/docs/index.html)

### Apache Cassandra
All you should have to do is download and open the tar.

[Download Apache Cassandra 2.1.0](http://cassandra.apache.org/download/)
[DataStax Academy - Free video courses on Cassandra!](https://academy.datastax.com/courses)

### Add CASSANDRA_HOME
Many ways to do this. A simple method is to open (or create if you don't have one) ~/.bash_profile
and add a Cassandra env to your path $CASSANDRA_HOME/bin

    export CASSANDRA_HOME=/Users/helena/cassandra  

    PATH=$CASSANDRA_HOME/bin:$JAVA_HOME/bin:$SBT_HOME/bin:$SCALA_HOME/bin$PATH
 
### Testing Your Install
Start Cassandra 

    sudo ./apache-cassandra-2.1.0/bin/cassandra

Follow the instructions to set up the timeseries WeatherCenter schema for Cassandra
on [/spark-cassandra-timeseries/README.md](/spark-cassandra-timeseries/README.md)

If all went well, you're G2G!


In Production you would use the `NetworkTopologyStrategy` and a mimimum replication factor of 3.
[NetworkTopologyStrategy](http://www.datastax.com/documentation/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html)


# Adding The Spark Cassandra Connector To Your Projects
The only dependency required is:

    "com.datastax.spark"  %% "spark-cassandra-connector" % "1.1.0-alpha2"

and possibly slf4j. The others in [SparkCassandraBlueprintBuild.scala](http://github.com/helena/spark-cassandra-blueprints/blob/master/project/SparkCassandraBlueprintBuild.scala) 
are there for other non Spark (core and streaming) and Cassandra code.
  
## Fun Reading / Resources

More being added...

* [CQL](http://www.datastax.com/documentation/cql/3.1/cql/cql_reference/cqlsh.html)
* [Cassandra: Getting started](http://wiki.apache.org/cassandra/GettingStarted) 
* [CCM: Tool for creating a local Cassandra cluster](http://www.datastax.com/dev/blog/ccm-a-development-tool-for-creating-local-cassandra-clusters) 
