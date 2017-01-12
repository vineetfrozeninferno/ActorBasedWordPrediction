package com.prediction

import scala.collection.mutable

case class WeightedTrieNode(token: String, score: Float) extends Ordered[WeightedTrieNode] {
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: WeightedTrieNode => that.token.equals(this.token)
    case _ => false
  }

  override def compare(that: WeightedTrieNode): Int = that.score.compare(this.score)
  override def hashCode(): Int = token.hashCode
}

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
    if(remainingTokens.isEmpty) currentTrie
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
      if(remainingTokens.nonEmpty) {
        val subTrie = get(key)
        subTrie.addSubsentenceTokens(remainingTokens)
      }
    }
  }

  override def toString: String = {
    val keyset = trie.keySet.map(x => x.token).toSet

    val thisSet = keyset.mkString("(",", ", ")")

    val childDetails = keyset.map(key => s"$key -> ${get(key).toString}").mkString("\n")

    s"$thisSet\n $childDetails"
  }
}