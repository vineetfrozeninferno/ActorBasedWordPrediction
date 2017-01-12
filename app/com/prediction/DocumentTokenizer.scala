package com.prediction

import akka.actor.Actor

class DocumentTokenizer extends Actor {
  val sentenceTerminatorPattern = """[!"&'(),.:;?`{}]"""

  override def receive: Receive = {
    case docRequest: AddDocumentRequest =>
      val doc = docRequest.document
      val tokens = doc.split(sentenceTerminatorPattern).toList
      val tplgActorRef = sender

      tokens.map(AddSentenceRequest).foreach(request => tplgActorRef ! request)
  }
}
