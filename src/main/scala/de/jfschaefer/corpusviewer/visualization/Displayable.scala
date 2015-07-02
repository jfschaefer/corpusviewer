package de.jfschaefer.corpusviewer.visualization

import scalafx.scene.Node

// Something that can be "put directly onto the screen", a first-class citizen so to say
trait Displayable extends Node {
  val parentDisplayable: Option[Displayable] = None     //By default, it's a root
  def enableInteraction():Unit
}
