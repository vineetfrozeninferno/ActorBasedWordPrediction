package com.prediction

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import controller.BaseApplication

import scala.concurrent.duration.DurationDouble
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

class TrieQueryActor extends Actor {
  implicit val timeout = Timeout(55 seconds)
  override def receive: Receive = {
    case querySentenceRequest: QueryNextWordRequest =>
      val sentenceTokensFuture =
        (TopologyDefinitionActor.sentenceTokenizerActorRef ? querySentenceRequest).mapTo[SentenceTokens]

      val senderRef = sender
      sentenceTokensFuture.map(sentenceTokens => {
        val firstTrie = sentenceTokens.tokens.headOption.map(TrieDataStore.getTrieFromDataStore).getOrElse(WeightedTrie())
        val remainingTokens = sentenceTokens.tokens.tail
        senderRef ! firstTrie.get(remainingTokens)
      })

    case queryRemainingSentenceRequest: QueryRemainingSentenceRequest =>
      val originalSender = sender
      val sentenceTokensFuture =
        (TopologyDefinitionActor.sentenceTokenizerActorRef ? queryRemainingSentenceRequest).mapTo[SentenceTokens]
      sentenceTokensFuture.map(sentenceTokens => {
        val firstTrie = sentenceTokens.tokens.headOption.map(TrieDataStore.getTrieFromDataStore).getOrElse(WeightedTrie())
        val remainingTokens = sentenceTokens.tokens.tail
        val possibleTokens = firstTrie.get(remainingTokens)
        if(possibleTokens.trie.keySet.toSet.exists(x => x.token.equals(BaseApplication.SENTENCE_TERMINATION)))
          originalSender ! sentenceTokens
        else {
          val updatedTokenSetOption = possibleTokens.trie.keySet.toList.sorted.headOption
          if(updatedTokenSetOption.isEmpty) originalSender ! sentenceTokens
          else {
            val updatedSentence = (updatedTokenSetOption.get.token :: sentenceTokens.tokens.reverse).reverse.mkString(" ")
            self.tell(QueryRemainingSentenceRequest(updatedSentence), originalSender)
          }
        }
      })
  }
}
