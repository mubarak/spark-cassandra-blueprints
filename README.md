# Spark Cassandra Blueprints

Templates for getting started or doing quick prototypes, building Scala applications with Spark and Cassandra.

Currently just basic samples. More documentation coming soon as well as larger application samples addressing particular use cases.

## Environment Pre-Requisites

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

Start Cassandra 

```sudo ./apache-cassandra-2.1.0/bin/cassandra```

Open the CQL shell 

```./apache-cassandra-2.1.0/bin/cqlsh```

Create A Keyspace

```CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }"```


In Production you would use the `NetworkTopologyStrategy` and a mimimum replication factor of 3.
[NetworkTopologyStrategy](http://www.datastax.com/documentation/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html)

Create A Table
```CREATE TABLE IF NOT EXISTS test.mytable (key TEXT PRIMARY KEY, value INT)"```

If all went well, you're G2G!

## Optional For Later 

* [CQL](http://www.datastax.com/documentation/cql/3.1/cql/cql_reference/cqlsh.html)
* [Cassandra: Getting started](http://wiki.apache.org/cassandra/GettingStarted) 
* [CCM: Tool for creating a local Cassandra cluster](http://www.datastax.com/dev/blog/ccm-a-development-tool-for-creating-local-cassandra-clusters) 
