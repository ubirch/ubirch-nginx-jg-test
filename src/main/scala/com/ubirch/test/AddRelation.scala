package com.ubirch.test

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.janusgraph.GremlinConnector
import com.ubirch.test.Elements.Property
import com.ubirch.test.Exceptions.ImportToGremlinException

import scala.language.postfixOps
import scala.util.Success

/**
  * Allows the storage of two nodes (vertices) in the janusgraph server. Link them together
  */
object AddRelation extends LazyLogging {

  private val label = "aLabel"

  /* main part of the program */
  def createRelation(relation: Relation)(implicit propSet: Set[Property], gc: GremlinConnector): String = {

    val relationServer = relation.toRelationServer
    executeRelationCreationStrategy(relationServer)

    "OK BB"
  }

  def executeRelationCreationStrategy(relationServer: RelationServer)(implicit gc: GremlinConnector): Unit = {
    howManyVerticesAlreadyInDb(List(relationServer.vFromDb, relationServer.vToDb)) match {
      case 0 => noneExist(relationServer)
      case 1 => oneExist(relationServer)
      case 2 => twoExist(relationServer)
    }
  }

  private def howManyVerticesAlreadyInDb(vertices: List[VertexDatabase]): Int = vertices.count(v => v.existInJanusGraph)

  /*
  If non of the two vertices that are being processed are not already present in the database.
  1/ create them.
  2/ link them.
   */
  private def noneExist(relation: RelationServer)(implicit gc: GremlinConnector): Unit = {
    logger.debug(Util.relationStrategyJson(relation, "non exist"))

    relation.vFromDb.addVertexWithProperties()
    relation.vToDb.addVertexWithProperties()
    relation.createEdge

  }

  /*
  If only one of the two vertices that are being processed is already present in the database.
  1/ determine which one is missing.
  2/ add it to the DB.
  3/ link them.
 */
  private def oneExist(relation: RelationServer)(implicit gc: GremlinConnector): Unit = {
    def addOneVertexAndCreateEdge(vertexNotInDb: VertexDatabase): Unit = {

      vertexNotInDb.addVertexWithProperties()
      relation.createEdge

    }

    if (relation.vFromDb.existInJanusGraph) {
      logger.debug(Util.relationStrategyJson(relation, "one exit: vFrom"))
      addOneVertexAndCreateEdge(relation.vToDb)
    } else {
      logger.debug(Util.relationStrategyJson(relation, "one exit: vTo"))
      addOneVertexAndCreateEdge(relation.vFromDb)
    }
  }

  /*
  If both vertices that are being processed is already present in the database.
  1/ link them if they're not already linked.
   */
  private def twoExist(relation: RelationServer)(implicit gc: GremlinConnector): Unit = {
    logger.debug(Util.relationStrategyJson(relation, "two exist"))

    if (!areVertexLinked(relation.vFromDb, relation.vToDb)) {
      relation.createEdge
    }
  }

  /**
    * Determine if two vertices are linked (independently of the direction of the edge).
    *
    * @param vFrom first vertex.
    * @param vTo   second vertex.
    * @return boolean. True = linked, False = not linked.
    */
  def areVertexLinked(vFrom: VertexDatabase, vTo: VertexDatabase)(implicit gc: GremlinConnector): Boolean = {
    gc.g.V(vFrom.vertex).bothE().bothV().is(vTo.vertex).l().nonEmpty
  }

}
