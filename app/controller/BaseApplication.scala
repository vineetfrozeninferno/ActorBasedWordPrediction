package controller

import com.prediction.TopologyDefinitionActor
import play.api.mvc.{Action, Controller}

object BaseApplication extends Controller {
  val SENTENCE_TERMINATION: String = "<sentence_termination>"

  def addSentence(sentence: String) = Action {
    TopologyDefinitionActor.absorbSentence(sentence)
    Ok("Added sentence to the datastore")
  }

  def addDocument(document: String) = Action {
    TopologyDefinitionActor.absorbDocument(document)
    Ok("Added document to the datastore")
  }

  def getTopNextWord(sentenceFrag: String) = Action {
    val listOfTrieNodes = TopologyDefinitionActor.getTopNextWords(sentenceFrag)
    val outputString = listOfTrieNodes.map(node => s"${node.token} -> ${node.score}").mkString("\n")
    Ok(outputString)
  }

  def getTopSentence(sentenceFrag: String) = Action {
    val outputString = TopologyDefinitionActor.getTopSentence(sentenceFrag)
    Ok(outputString)
  }
}