package com.company.az

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.alpakka.cassandra.{CassandraSessionSettings, CassandraWriteSettings}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraFlow, CassandraSession, CassandraSessionRegistry}
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import com.supermarket.az.{Transaction, TransactionReply}

import scala.concurrent.Future

object DatabaseHandler {
  def apply(system: ActorSystem[_]): DatabaseHandler = new DatabaseHandler(system)
}

class DatabaseHandler(system: ActorSystem[_]){   // this object uses alpakka cassandra

  implicit val sys = system
  implicit val ec = system.executionContext
  val sessionSettings: CassandraSessionSettings = CassandraSessionSettings()
  implicit val cassandraSession: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

  def insert(transaction: Transaction): Future[Done] = {
    val statementBinder: (Transaction, PreparedStatement) => BoundStatement =
      (transaction, preparedStatement) => preparedStatement.bind(transaction.uuId, transaction.marketId, Double.box(transaction.transactionAmount))

    Source.single(transaction)
      .via(
        CassandraFlow.create(CassandraWriteSettings.defaults,
          s"INSERT INTO akka.market(id, name, amount) VALUES (?, ?, ?)",
          statementBinder)
      )
      .runWith(Sink.ignore)
  }

  def predCondition() = {
    cassandraSession.selectOne("SELECT table_name FROM system_schema.tables WHERE keyspace_name='akka';")
      .flatMap{
        result =>
          result.find(table => table.getString(0) == "market") match {
            case Some(_) => Future.successful(Done)
            case None =>
              cassandraSession.executeDDL("create table akka.market( id text primary key,name text,amount double);")
          }
      }
  }

  def getTransaction(id: String) = {

    cassandraSession
      .selectOne("select * from akka.market where id = ?", id)
      .map {
        case Some(value) =>
          TransactionReply(s"found value is: ${value.getFormattedContents}")
        case None => TransactionReply("not found")
      }
  }
}