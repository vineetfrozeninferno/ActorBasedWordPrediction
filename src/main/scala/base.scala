import akka.actor._
import akka.routing.{ConsistentHashingPool, RoundRobinPool}
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping

object Base {
  val sentenceTokenizerActorRef: ActorRef = TypedActor.context.actorOf(Props[SentenceTokenizer].withRouter(RoundRobinPool(10)))


  def firstLetterHashing: ConsistentHashMapping = {
    case tokens: List[String] => tokens.headOption.map(_.charAt(0)).getOrElse("#")
  }

  val UpdateTrieActorRef: ActorRef = TypedActor.context.actorOf(Props[UpdateTrieActor].withRouter(ConsistentHashingPool(26, hashMapping = firstLetterHashing)))
}