package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper}

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text

class NoVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable{
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()


  // HEADER
  val menu = new RadialMenu
  menu.enableInteraction()
  val header = new Header(iw.index + ". " + key, Some(menu))

  children.add(header)

  // CONTENT
  val textField = new Text("\nSorry, there is no visualization for this interpretation (yet)") {
  }

  textField.translateX = Configuration.previewMargin
  textField.translateY = header.getHeight + Configuration.previewMargin

  children.add(textField)

  header.toFront()

  minHeight = textField.boundsInParent.value.getHeight + 2 * Configuration.previewMargin + header.getHeight
  minWidth = Configuration.preferredPreviewWidth
  maxWidth = Configuration.preferredPreviewWidth
}
