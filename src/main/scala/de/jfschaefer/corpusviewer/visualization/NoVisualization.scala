package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper}

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text

/**  Displayable for stating that no visualization is available for a certain interpretation.
  *
  * @param iw the instance
  * @param parentDisp the parent Displayable
  * @param key the name of the interpretations
  * @param message the message explaining why there is no visualization
  */

class NoVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String,
                       message : String) extends Pane with Displayable{
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX  // <== scale
  scaleY  // <== scale

  setupStyleStuff()


  // HEADER
  val menu = new RadialMenu {
    displayable = Some(NoVisualization.this)
    items = new NormalMenuEntryFunction("Trash", () => trash())::Nil
  }
  menu.enableInteraction()
  override val header = new Header(iw.getIDForUser + ". " + key, Some(menu))

  children.add(header)

  // CONTENT
  val textField = new Text("\n" + message) {
    wrappingWidth = Configuration.preferredPreviewWidth - 2 * Configuration.previewMargin
  }

  textField.translateX = Configuration.previewMargin
  textField.translateY = header.getHeight + Configuration.previewMargin

  children.add(textField)

  header.toFront()

  minHeight = textField.boundsInParent.value.getHeight + 2 * Configuration.previewMargin + header.getHeight
  minWidth = Configuration.preferredPreviewWidth
  maxWidth = Configuration.preferredPreviewWidth
}
