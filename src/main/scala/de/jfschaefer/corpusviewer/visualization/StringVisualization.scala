package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, Configuration, InstanceWrapper}

import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.input.ZoomEvent
import scalafx.Includes._

/*
    A Displayable for the string interpretation of an instance.
 */

class StringVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  setupStyleStuff()

  val instanceMap = iw.instance.getInputObjects
  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[java.util.List[String @unchecked]])
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])

  // HEADER
  val menu = new RadialMenu {
    displayable = Some(StringVisualization.this)
    items = new MenuEntryFunction("Copy", () => {
      Util.copyIntoClipboard(stringRepresentation)
    })::new MenuEntryFunction("Trash", () => trash() )::Nil
  }
  menu.enableInteraction()
  override val header = new Header(iw.getIDForUser + ". String", Some(menu))

  children.add(header)

  // CONTENT
  val textField = new Text("\n" + stringRepresentation) {
    wrappingWidth = Configuration.stringvisualizationWidth - 2 * Configuration.stringvisualizationPadding
  }

  textField.translateX = Configuration.stringvisualizationPadding
  textField.translateY = header.getHeight + Configuration.stringvisualizationPadding

  children.add(textField)


  header.toFront()
  header.headerWidth.set(Configuration.stringvisualizationWidth)

  minHeight = textField.boundsInParent.value.getHeight + 2 * Configuration.stringvisualizationPadding + header.getHeight
  minWidth = Configuration.stringvisualizationWidth
  maxWidth = Configuration.stringvisualizationWidth

  def updateSize(): Unit = {
    minHeight = textField.boundsInParent.value.getHeight + 2 * Configuration.stringvisualizationPadding + header.getHeight
    maxHeight = textField.boundsInParent.value.getHeight + 2 * Configuration.stringvisualizationPadding + header.getHeight
    minWidth = Configuration.stringvisualizationWidth * textField.scaleX.value
    maxWidth = Configuration.stringvisualizationWidth * textField.scaleX.value
    header.headerWidth.set(Configuration.stringvisualizationWidth * textField.scaleX.value)
  }


  onZoom = { ev : ZoomEvent => Util.dispHandleZoom(this, textField)(ev); updateSize()}
}

