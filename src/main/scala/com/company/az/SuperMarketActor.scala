package com.company.az

import akka.actor.typed.{ActorRefResolver, Behavior}
import akka.actor.typed.receptionist.Receptionist.Register
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.supermarket.az._

import scala.util.{Failure, Success}

object SuperMarketActor {

  val name = "retail"

  def apply(id: Int): Behavior[SuperMarketMessages] = Behaviors
    .setup{
      context =>
        val persistence: DatabaseHandler = DatabaseHandler(context.system)
        persistence.predCondition()
        context.system.receptionist ! Register(ServiceKey[SuperMarketMessages](s"retail-$id"),context.self)  // registers new actor in the receptionist
        println(s"retail with id $id registered")
        Behaviors.receiveMessage {
          case cmd: SuperMarketCmd =>
            context.log.info("command received to actor with command {}",cmd)
            commandHandler(cmd,persistence,context)
          case query: SuperMarketQuery =>
            context.log.info("command received to actor with query {}",query)
            queryHandler(persistence,query,context)
        }
    }

  def commandHandler(command: SuperMarketCmd, databaseHandler: DatabaseHandler, context: ActorContext[SuperMarketMessages]): Behavior[SuperMarketMessages] = {
    implicit val ec = context.executionContext
    command match {
      case AddTransactionToRetail(_,transaction, replTo, _) =>
        databaseHandler
          .insert(transaction)
        .onComplete {
          case Failure(exception) =>
            ActorRefResolver(context.system).resolveActorRef[TransactionReply](replTo) ! TransactionReply(s"${exception.getMessage}")
          case Success(_) =>
            ActorRefResolver(context.system).resolveActorRef[TransactionReply](replTo) ! TransactionReply("transaction added")
        }
        Behaviors.same
    }
  }

  def queryHandler(databaseHandler: DatabaseHandler, query: SuperMarketQuery, context: ActorContext[SuperMarketMessages]): Behavior[SuperMarketMessages] = {
    query match {
      case GetTransaction(_,tId, replyTo, _) =>
        databaseHandler
          .getTransaction(tId)
        .onComplete {
          case Failure(exception) =>
            ActorRefResolver(context.system).resolveActorRef[TransactionReply](replyTo) ! TransactionReply(exception.getMessage)
          case Success(rpl) =>
            ActorRefResolver(context.system).resolveActorRef[TransactionReply](replyTo) ! rpl
        }(context.executionContext)
    }
    Behaviors.same
  }
}