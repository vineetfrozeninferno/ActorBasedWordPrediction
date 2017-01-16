package com.prediction

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import akka.routing.{ConsistentHashingPool, SmallestMailboxPool}
import akka.util.Timeout
import com.prediction.TopologyDefinitionActor._
import controller.BaseApplication
import play.api.libs.json.Json
import play.libs.Akka

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

case class AddSentenceRequest(sentence: String)
case class QueryNextWordRequest(sentence: String)
case class QueryRemainingSentenceRequest(sentence: String)
case class AddDocumentRequest(document: String)
case class SentenceTokens(tokens: List[String])

object TopologyDefinitionActor {
  def firstLetterHashing: ConsistentHashMapping = {
    case sentenceTokens: SentenceTokens => sentenceTokens.tokens.headOption.flatMap(firstToken => Try(firstToken.charAt(0)).toOption).getOrElse("#")
  }

  private val topologyActorRef: ActorRef = Akka.system.actorOf(Props[TopologyDefinitionActor])
  val documentTokenizerActorRef: ActorRef = Akka.system.actorOf(Props[DocumentTokenizer].withRouter(SmallestMailboxPool(10)))
  val sentenceTokenizerActorRef: ActorRef = Akka.system.actorOf(Props[SentenceTokenizer].withRouter(SmallestMailboxPool(10)))
  val trieQueryActorRef: ActorRef = Akka.system.actorOf(Props[TrieQueryActor])
  val updateTrieActorRef: ActorRef = Akka.system.actorOf(Props[UpdateTrieActor].withRouter(ConsistentHashingPool(26, hashMapping = firstLetterHashing)))

  def absorbSentence(sentence: String): Unit = {
    topologyActorRef ! AddSentenceRequest(sentence)
  }

  def absorbDocument(document: String): Unit = {
    topologyActorRef ! AddDocumentRequest(document)
  }

  def getTopNextWords(sentenceFrag: String): List[WeightedTrieNode] = {
    implicit val timeout = Timeout(55 seconds)
    val future = trieQueryActorRef ? QueryNextWordRequest(sentenceFrag)
    val result = Await.result(future, timeout.duration).asInstanceOf[WeightedTrie]
    result.trie.keySet.toList.sorted.filterNot(x => x.token.equals(BaseApplication.SENTENCE_TERMINATION))
  }

  def getTopSentence(sentenceFrag: String): String = {
    implicit val timeout = Timeout(55 seconds)
    val future = trieQueryActorRef ? QueryRemainingSentenceRequest(sentenceFrag)
    val result = Await.result(future, timeout.duration).asInstanceOf[SentenceTokens]
    result.tokens.mkString("\n")
  }
}

class TopologyDefinitionActor extends Actor {

  override def receive: Receive = {
    case addDocumentRequest: AddDocumentRequest => documentTokenizerActorRef ! addDocumentRequest
    case addSentenceRequest: AddSentenceRequest => sentenceTokenizerActorRef ! addSentenceRequest
    case sentenceTokens: SentenceTokens => updateTrieActorRef ! sentenceTokens
    case weightedTrie: WeightedTrie =>
      //Json.toJson(weightedTrie)
      println(s"trie = $weightedTrie")
  }
}
