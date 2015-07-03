package de.jfschaefer.corpusviewer.visualization

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable]
  val scale: DoubleProperty
  def enableInteraction():Unit
}

trait RootDisplayable extends Displayable {
  val index: Int
}