package com.company.az

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.supermarket.az.SuperMarketMessages

object SuperMarketGuardianActor {


  def apply(): Behavior[SuperMarketMessages] = Behaviors.setup {  // guardian actor for actor system
    context =>
              List.from(1 to 10).foreach{
                index =>
                  context.spawnAnonymous(SuperMarketActor(index))  // creates new children actors
              }
      Behaviors.same
  }
}