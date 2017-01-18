package com.prediction

import anorm._
import play.api.db.DB
import play.api.libs.json.Json

import play.api.Play.current

import scala.util.{Failure, Try}

object TrieDataStore {
  val IdKey: String = "id"
  val Data: String = "data"
  val TableName: String = "trieDataStore"

  val createTableSql: String =
    s"""CREATE TABLE IF NOT EXISTS $TableName (
       |  $IdKey VARCHAR(64) NOT NULL,
       |  $Data TEXT NOT NULL,
       |  PRIMARY KEY ($IdKey)
       |) DEFAULT CHARACTER SET=utf8mb4;
    """.stripMargin.trim

  val insertSql: String =
    s"""INSERT INTO $TableName($IdKey, $Data)
       |  VALUES ({$IdKey}, {$Data})
       |  ON DUPLICATE KEY UPDATE $Data = {$Data};
    """.stripMargin.trim

  val selectSql: String =
    s"""SELECT $Data FROM $TableName
       |  WHERE $IdKey = {$IdKey};
    """.stripMargin.trim

  val isTableCreated: Boolean = DB.withConnection { implicit connection =>
    Try(SQL(createTableSql).execute) match {
      case scala.util.Success(_) => true
      case Failure(e) =>
        println(s"Exception creating $TableName table", e)
        false
    }
  }

  def addTrieToDataStore(key: String, trie: WeightedTrie): Unit = {
    DB.withConnection(implicit connection => {
      val data = Json.toJson(trie).toString
      Try(SQL(insertSql).on(IdKey -> key, Data -> data).execute) match {
        case scala.util.Success(_) => println(s"Added record to $TableName with id:$key")
        case Failure(e) => println(s"Error adding record to $TableName with id:$key", e)
      }
    })
  }

  def getTrieFromDataStore(key: String): WeightedTrie = {
    DB.withConnection(implicit connection => {
      val jsonDataList = Try(SQL(selectSql).on(IdKey -> key))
        .toOption
        .map(row => row.as(SqlParser.str(Data).*))
        .getOrElse(List.empty)

      jsonDataList
        .map(json => Json.parse(json))
        .map(jsVal => Json.fromJson[WeightedTrie](jsVal).getOrElse(WeightedTrie()))
        .headOption
        .getOrElse(WeightedTrie())
    })
  }

}
