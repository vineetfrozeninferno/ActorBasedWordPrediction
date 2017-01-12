package com.prediction

import akka.actor.Actor

class UpdateTrieActor extends Actor {

  override def receive: Receive = {
    case sentenceTokens: SentenceTokens =>
      val tokens = sentenceTokens.tokens
      val remainingTokens = tokens.tail

      tokens.headOption
        .foreach(primaryKey => {
          val currentTrie = TrieDataStore.getTrieFromDataStore(primaryKey)
          currentTrie.addSubsentenceTokens(remainingTokens)
          TrieDataStore.addTrieToDataStore(primaryKey, currentTrie)

          println(s"LOG: Added ${tokens.mkString(" ")} to the trie")
          sender ! currentTrie
        })
  }
}
