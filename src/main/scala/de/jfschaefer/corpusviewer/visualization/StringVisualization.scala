package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper}

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

  // HEADER
  val menu = new RadialMenu
  menu.enableInteraction()
  val header = new Header(iw.index + ". String", Some(menu))

  children.add(header)

  // CONTENT
  assert(instanceMap.containsKey(key))
  val algObj = instanceMap.get(key)
  assert(algObj.isInstanceOf[java.util.List[String @unchecked]])
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])
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
