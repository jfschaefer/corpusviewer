package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.InstanceWrapper

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node

/** A Node that can be used for preview when scrolling through the corpus */
trait Preview extends Node {
  val scale = new DoubleProperty()
  def getIw : InstanceWrapper
  /** Get the actual height (the height property of a Node isn't reliable) */
  def getHeight : Double

  var idstyleclass : String = "no_id_assigned"
  def setupStyleStuff(): Unit = {
    styleClass.clear()
    styleClass.add("displayable")
    styleClass.add("no_trash_alert")
    styleClass.add("no_id_assigned")
    onStyleClassIdUpdate()
    getIw.id onChange {
      onStyleClassIdUpdate()
    }

    def onStyleClassIdUpdate(): Unit = {
      while (styleClass.contains(idstyleclass)) styleClass.remove(styleClass.indexOf(idstyleclass))
      idstyleclass = getIw.getStyleClass
      styleClass.add(idstyleclass)
    }
  }
}
