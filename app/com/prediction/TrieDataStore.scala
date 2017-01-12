package com.prediction

import scala.collection.mutable

object TrieDataStore {
  private val simulatedDataStore:mutable.HashMap[String, WeightedTrie] = new mutable.HashMap[String, WeightedTrie]()

  def getTrieFromDataStore(key: String): WeightedTrie = simulatedDataStore.getOrElse(key, WeightedTrie())

  def addTrieToDataStore(key: String, trie: WeightedTrie): Unit = simulatedDataStore.put(key, trie)
}
