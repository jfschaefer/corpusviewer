package de.jfschaefer.corpusviewer.visualization

import scalafx.scene.Node

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable]
  def enableInteraction():Unit
}

trait RootDisplayable extends Displayable {
  val index: Int
}