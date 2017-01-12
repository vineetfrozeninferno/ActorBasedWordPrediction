package com.prediction

import akka.actor.Actor
import controller.BaseApplication

class SentenceTokenizer extends Actor {
  override def receive: Receive = {
    case sentenceRequest: AddSentenceRequest =>
      val sentence = sentenceRequest.sentence
      val tokens = sentence.replaceAll("""\p{Punct}""", "")
                          .split(" ")
                          .toList
      val tokensWithTermination = BaseApplication.SENTENCE_TERMINATION :: tokens.reverse
      sender ! SentenceTokens(tokensWithTermination.reverse)

    case sentenceRequest: QueryNextWordRequest =>
      val sentence = sentenceRequest.sentence
      val tokens = sentence.replaceAll("""\p{Punct}""", "")
        .split(" ")
        .toList
      sender ! SentenceTokens(tokens)

    case sentenceRequest: QueryRemainingSentenceRequest =>
      val sentence = sentenceRequest.sentence
      val tokens = sentence.replaceAll("""\p{Punct}""", "")
        .split(" ")
        .toList
      sender ! SentenceTokens(tokens)
  }
}
