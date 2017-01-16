package com.prediction

import scala.collection.mutable
import play.api.libs.json._

case class WeightedTrie(trie: mutable.Map[WeightedTrieNode, WeightedTrie] = mutable.HashMap()) {
  def incrementChildKeyScore(key: String): Unit = {
    val childKey = getKey(key)
    val childValue = get(key)
    remove(key)
    trie.put(childKey.copy(score = childKey.score + 1), childValue)
    val test = "tst"
  }

  private def getKey(token: String): WeightedTrieNode = {
    val baseKey = WeightedTrieNode(token, 0)
    trie.keySet.toSet.find(_.equals(baseKey)).getOrElse(baseKey)
  }

  def get(token: String): WeightedTrie = {
    val key = getKey(token)
    trie.getOrElse(key, WeightedTrie())
  }

  def get(tokens: List[String]): WeightedTrie = {
    val remainingTokens = tokens.tail
    val currentTrie = get(tokens.head)
    if (remainingTokens.isEmpty) currentTrie
    else currentTrie.get(remainingTokens)
  }

  def remove(token: String): Unit = {
    val childKey = getKey(token)
    trie.remove(childKey)
  }

  def addSubsentenceTokens(tokens: List[String]): Unit = {
    val remainingTokens = tokens.tail
    val keyOption = tokens.headOption
    keyOption.foreach { key =>
      incrementChildKeyScore(key)
      if (remainingTokens.nonEmpty) {
        val subTrie = get(key)
        subTrie.addSubsentenceTokens(remainingTokens)
      }
    }
  }

  override def toString: String = {
    val keyset = trie.keySet.map(x => x.token).toSet

    val thisSet = keyset.mkString("(", ", ", ")")

    val childDetails = keyset.map(key => s"$key -> ${get(key).toString}").mkString("\n")

    s"$thisSet\n $childDetails"
  }
}

object WeightedTrie {
  val keyJsField = "key"
  val valueJsField = "value"
  val entriesJsField = "entries"

  def jsonWrites(obj: WeightedTrie): JsValue = {
    lazy val trieAsJson = obj.trie.toList.map {
      case (wtNodeKey, wtValue) => Json.obj(
        keyJsField -> Json.toJson(wtNodeKey),
        valueJsField -> WeightedTrie.jsonWrites(wtValue)
      )
    }
    Json.obj(entriesJsField -> JsArray(trieAsJson))
  }

  def jsonReads(json: JsValue): JsResult[WeightedTrie] = {
    val reconstructedTrie = mutable.Map[WeightedTrieNode, WeightedTrie]()
    val entries = (json \ entriesJsField).as[JsArray].value

    entries.foreach(jsVal => {
      val key = (jsVal \ keyJsField).as[WeightedTrieNode]
      val valueOptional = jsonReads(jsVal).asOpt
      valueOptional.map(value => reconstructedTrie.put(key, value))
    })

    JsSuccess(WeightedTrie())
  }

  implicit lazy val format = new Format[WeightedTrie] {

    def writes(obj: WeightedTrie): JsValue = jsonWrites(obj)

    override def reads(json: JsValue): JsResult[WeightedTrie] = jsonReads(json)
  }
}