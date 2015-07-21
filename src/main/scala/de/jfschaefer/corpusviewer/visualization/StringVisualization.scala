package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, Configuration, InstanceWrapper}

import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text

class StringVisualization(iw : InstanceWrapper, parentDisp : Option[Displayable], key : String) extends Pane with Displayable{
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()

  val instanceMap = iw.instance.getInputObjects
  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[java.util.List[String @unchecked]])
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])

  // HEADER
  val menu = new RadialMenu {
    items = new MenuEntryFunction("Copy", () => {
      Util.copyIntoClipboard(stringRepresentation)
    })::new MenuEntryFunction("Trash", () => trash() )::Nil
  }
  menu.enableInteraction()
  override val header = new Header(iw.index + ". String", Some(menu))

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
}
