# Spark Cassandra Blueprints

Templates for getting started or doing quick prototypes, building Scala applications with Spark and Cassandra.

Currently the repo just has the basic samples. Full application samples addressing particular use cases are coming over the next few days.

Some of these will be in the DataBricks github samples as well.

## Environment Pre-Requisites

If you already use Scala skip the SBT step. Similarly, if you already can spin up Cassandra locally, skip that step. 

### SBT: Scala Build Tool
We will be building and running with SBT

* [Download SBT](http://www.scala-sbt.org/download.html) 
* [SBT Setup](http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html) 

### Clone the repo and build the code

```git clone https://github.com/helena/spark-cassandra-blueprints.git```
```cd spark-cassandra-blueprints```
```sbt compile```

[SBT Docs](http://www.scala-sbt.org/0.13/docs/index.html)

### Apache Cassandra

All you should have to do is download and open the tar.

[Download Apache Cassandra 2.1.0](http://cassandra.apache.org/download/)

### Testing Your Install

Start Cassandra ```sudo ./apache-cassandra-2.1.0/bin/cassandra```

Open the CQL shell ```./apache-cassandra-2.1.0/bin/cqlsh```

```CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }"```
```CREATE TABLE IF NOT EXISTS test.mytable (key TEXT PRIMARY KEY, value INT)"```

If all went well, you're G2G!


In Production you would use the `NetworkTopologyStrategy` and a mimimum replication factor of 3.
[NetworkTopologyStrategy](http://www.datastax.com/documentation/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html)



## Fun Reading / Resources

More being added...

* [CQL](http://www.datastax.com/documentation/cql/3.1/cql/cql_reference/cqlsh.html)
* [Cassandra: Getting started](http://wiki.apache.org/cassandra/GettingStarted) 
* [CCM: Tool for creating a local Cassandra cluster](http://www.datastax.com/dev/blog/ccm-a-development-tool-for-creating-local-cassandra-clusters) 
