package com.ubirch.test

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.janusgraph.{ ConnectorType, GremlinConnector, GremlinConnectorFactory }
import com.ubirch.test.Elements.Property
import gremlin.scala.{ Key, KeyValue }

import scala.util.Random

object Main extends LazyLogging {

  lazy val podAssignedValue: String = "pod-" + Random.alphanumeric.take(6).mkString + "-"
  val vertexLabel = "TEST"
  val edgeLabel = "CHAIN"
  val hashKey: Key[Any] = Key[Any]("hash")
  val kvCentralV = KeyValue(hashKey, "test")

  val mainVertex = VertexCore(List(ElementProperty(kvCentralV, PropertyType.String)), "TEST")

  implicit val propSet: Set[Property] = Set(HASH)

  case object HASH extends Property("hash", true)
  /*
  Get a central vertex "TEST", hash="test"
  create relations emaning from this one vertex with edge: EDGE("CHAIN"), vTo: label = "TEST", property = madeBy: "randomly assigned value assigned to the pod, generated at pod creation", number: counter
   */
  def main(args: Array[String]): Unit = {
    implicit val gc: GremlinConnector = GremlinConnectorFactory.getInstance(ConnectorType.JanusGraph)
    logger.info("podAssignedValue: " + podAssignedValue)
    for (_ <- 1 to 500) {
      AddRelation.createRelation(generateRelation)
      Thread.sleep(50)
      if (counter % 10 == 0) {
        logger.info(s"made $counter")
      }
    }

  }

  def giveMeATimestamp: String = System.currentTimeMillis.toString

  def generateRelation = Relation(mainVertex, generateVertex, generateEdge)

  def generateEdge: EdgeCore = {
    val label = edgeLabel
    EdgeCore(Nil, label)
      .addProperty(generateElementProperty("timestamp", giveMeATimestamp))
  }

  def generateVertex: VertexCore = {
    val label = vertexLabel
    VertexCore(Nil, label)
      .addProperty(generateElementProperty("hash", podAssignedValue + getCounterAndIncrement))
      .addProperty(generateElementProperty("timestamp", giveMeATimestamp))
  }

  var counter = 0

  def getCounterAndIncrement: Int = {
    counter += 1
    counter
  }

  def generateElementProperty(key: String, value: String): ElementProperty = {
    val vAsString = value.toString
    val newValue = if (vAsString forall Character.isDigit) (vAsString.toLong, PropertyType.Long) else (vAsString, PropertyType.String)
    ElementProperty(KeyValue[Any](Key[Any](key), newValue._1), newValue._2)
  }

}
