package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.InstanceWrapper

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable]
  val scale: DoubleProperty
  def enableInteraction():Unit
  def getIw: InstanceWrapper
}

trait RootDisplayable extends Displayable {
  val index: Int
}