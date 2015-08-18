package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Preview
import de.up.ling.irtg.corpus.Instance

import scalafx.beans.property.IntegerProperty


/**
 * Maintains the ids, which are responsible for the colors
 */
object InstanceWrapper {
  val idsInUse = new scala.collection.mutable.HashSet[Int]

  /** Get a new, free id */
  def getNewId:Int = {
    var i = 0
    while (idsInUse.contains(i)) i+=1
    idsInUse.add(i)
    i
  }

  /** Release an id
    *
    * @param id the id to be released
    */
  def releaseId(id: Int):Unit = {
    assert(idsInUse.contains(id))
    idsInUse.remove(id)
  }

  /** Get the style class corresponding to the id
    *
    * @param id the id
    * @return the style class
    */
  def getIdStyleClass(id: Int): String = {
    "id_" + (id%Configuration.numberOfIds)
  }
}

/** A simple wrapper around [[de.up.ling.irtg.corpus.Instance]], managing e.g. the ids, which correspond to the colors
  *
  * @param instance the instance
  * @param interpretations a map from interpretation names to the corresponding algebra class names
  */

class InstanceWrapper(val instance: Instance, val interpretations : Map[String, String]) {
  var preview: Preview = null
  var id: IntegerProperty = new IntegerProperty
  id.set(-1)       //not assigned
  var index: Int = -1  //not yet assigned
  var corpusOffsetStart: Double = 0d
  var corpusOffsetEnd: Double = 0d

  /** Assigns an id to this instance */
  def assignId():Unit = {
    if (id.value == -1) {
      id.set(InstanceWrapper.getNewId)
    }
  }

  /** Releases the id of this instance */
  def releaseId():Unit = {
    if(id.value != -1) {
      InstanceWrapper.releaseId(id.value)
      id.set(-1)
    }
  }

  /** gets the style class (depends on the id) */
  def getStyleClass: String = {
    if (id.value == -1) "no_id_assigned"
    else InstanceWrapper.getIdStyleClass(id.value)
  }

  /** Gets the id to be displayed for the user. This one is unrelated to the id for colors */
  def getIDForUser: String = {
    if (instance.getComments.containsKey("corpusviewer_id"))
      instance.getComments.get("corpusviewer_id")
    else
      index.toString
  }

  /** Returns true iff an id has been assigned to this instance */
  def hasIdAssigned: Boolean = id.value != -1
}
