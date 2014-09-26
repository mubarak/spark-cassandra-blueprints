# Spark Cassandra Blueprints

Templates for getting started or doing quick prototypes, building Scala applications with Spark and Cassandra.

Currently just basic samples. More documentation coming soon as well as larger application samples addressing particular use cases.

## Environment Pre-Requisites

### SBT: Scala Build Tool
We will be building and running with SBT

* [Download SBT](http://www.scala-sbt.org/download.html) 
* [SBT Setup](http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html) 
* [SBT Docs](http://www.scala-sbt.org/0.13/docs/index.html)

### Apache Cassandra
All you should have to do is download and open the tar.

* [Download Apache Cassandra 2.1.0](http://cassandra.apache.org/download/)
* [Cassandra: Getting started](http://wiki.apache.org/cassandra/GettingStarted) 


Optional for later / your own reference:
Easy tool for creating a local Cassandra cluster
 
* [CCM](http://www.datastax.com/dev/blog/ccm-a-development-tool-for-creating-local-cassandra-clusters) 

### Testing Your Install

Start Cassandra
```sudo ./apache-cassandra-2.1.0/bin/cassandra```

Create A Keyspace
* Open the CQL shell
```./apache-cassandra-2.1.0/bin/cqlsh```
[CQL](http://www.datastax.com/documentation/cql/3.1/cql/cql_reference/cqlsh.html)

```CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }"```

Normally you would use the `NetworkTopologyStrategy` and a mimimum replication factor of 3.
[NetworkTopologyStrategy](http://www.datastax.com/documentation/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html)



