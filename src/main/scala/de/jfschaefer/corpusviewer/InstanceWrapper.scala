package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Preview
import de.up.ling.irtg.corpus.Instance

import scalafx.beans.property.IntegerProperty

object InstanceWrapper {
  val idsInUse = new scala.collection.mutable.HashSet[Int]

  def getNewId:Int = {
    var i = 0
    while (idsInUse.contains(i)) i+=1
    idsInUse.add(i)
    i
  }

  def releaseId(id: Int):Unit = {
    assert(idsInUse.contains(id))
    idsInUse.remove(id)
  }

  def getIdStyleClass(id: Int): String = {
    "id_" + (id%Configuration.numberOfIds)
  }
}

class InstanceWrapper(val instance: Instance) {
  var preview: Preview = null
  var id: IntegerProperty = new IntegerProperty
  id.set(-1)       //not assigned
  var index: Int = -1  //not yet assigned
  var corpusOffsetStart: Double = 0d
  var corpusOffsetEnd: Double = 0d

  def assignId():Unit = {
    if (id.value == -1) {
      id.set(InstanceWrapper.getNewId)
    }
  }

  def releaseId():Unit = {
    if(id.value != -1) {
      InstanceWrapper.releaseId(id.value)
      id.set(-1)
    }
  }

  def getStyleClass: String = {
    if (id.value == -1) "no_id_assigned"
    else InstanceWrapper.getIdStyleClass(id.value)
  }

  def hasIdAssigned: Boolean = id.value != -1
}
