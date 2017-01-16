package com.prediction

import play.api.libs.json._

case class WeightedTrieNode(token: String, score: Float) extends Ordered[WeightedTrieNode] {
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: WeightedTrieNode => that.token.equals(this.token)
    case _ => false
  }

  override def compare(that: WeightedTrieNode): Int = that.score.compare(this.score)
  override def hashCode(): Int = token.hashCode
}

object WeightedTrieNode {
  val tokenJsField = "token"
  val scoreJsField = "score"

  implicit val format = new Format[WeightedTrieNode] {
    override def writes(obj: WeightedTrieNode): JsValue = {
      Json.obj(
        tokenJsField -> obj.token,
        scoreJsField -> obj.score
      )
    }

    override def reads(json: JsValue): JsResult[WeightedTrieNode] = {
      val tokenValue = (json \ tokenJsField).as[String]
      val scoreValue = (json \ scoreJsField).as[Float]
      JsSuccess(WeightedTrieNode(tokenValue, scoreValue))
    }
  }
}
