# Everything's WINDIER in TEXAS
### This project uses Kafka, Spark Streaming, and PubNub to stream localized wholesale power price data and wind data throughout Texas @ https://ian-wright.github.io/everythings-windier-in-texas/ 

## Motivation
Mainly because I wanted a project to learn some data streaming technologies, and dive into Scala programming. 

But also... the Texas electricity market (ERCOT - Electricity Reliability Council of Texas) produces more wind-generated power than any other state. This massive wind capacity, combined with the effects of the Federal [Production Tax Credit](https://energy.gov/savings/renewable-electricity-production-tax-credit-ptc), make for an interesting (and often dramatic) dynamic between realtime wholesale power prices and wind resources. On several particularly [windy occasions](https://cleantechnica.com/2015/10/01/texas-electricity-prices-going-negative/) in the past, localized power prices have actually gone negative. This visualization is meant to monitor the wind - power price relationship, spatially and in realtime.

## How the pipeline works
+ Python scripts pull weather data from [here](https://www.satori.com/channels/METAR-AWC-US) and pricing data from [here](http://www.ercot.com/content/cdr/contours/rtmLmp.html), and push the data to a local single-node two-topic Kafka broker.
+ A Scala app uses Spark Streaming to connect to the Kafka broker, and cleans/filters/processes all data according to topic
+ Scala app publishes the DStream to a realtime messaging cloud service, PubNub, on a singular channel
+ A simple frontend client subscribes to the channel and receives all messages in realtime; 

## Requirements:
+ Scala 2.11
+ [sbt](http://www.scala-sbt.org/download.html)
+ [Spark 2.x](https://kafka.apache.org/downloads)
+ pip install kafka-python
+ pip install satori-rtm-sdk

## Make it go:
### There are several concurrent processes running, so we'll need a whopping FIVE tabs open to the repo's directory
- clone this repo.
- (in new terminal tab) - start a local single-node zookeeper instance

  *kafka_2.11-0.11.0.0/bin/zookeeper-server-start.sh kafka_2.11-0.11.0.0/config/zookeeper.properties*
  
- (in new terminal tab) - start the single-node kafka broker

  *kafka_2.11-0.11.0.0/bin/kafka-server-start.sh kafka_2.11-0.11.0.0/config/server.properties*

- (in a new terminal tab) - create the kafka topics on the broker

  *kafka_2.11-0.11.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic price*
  
  *kafka_2.11-0.11.0.0/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic weather*
  
- (in new terminal tab) - publish weather data to kafka

  *python data_generators/satori_to_kafka.py*
  
- (in new terminal tab) - publish price data to kafka

  *python data_generators/price_to_kafka.py*
  
- (in a new terminal tab) - compile and run

    *sbt compile*
    
    *sbt run*
  
- navigate to https://ian-wright.github.io/everythings-windier-in-texas/ and watch some data pour in!
