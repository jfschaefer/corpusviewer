package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, Configuration}
import de.up.ling.irtg.algebra.StringAlgebra
import de.up.ling.irtg.corpus.Instance

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.Includes._
import scalafx.scene.text.Text

class StringVisualization(instance: Instance, key: String, parentD: Displayable) extends Pane with Displayable {
  override val parentDisplayable = Some(parentD)
  override val scale = new DoubleProperty
  scale.set(1d)

  styleClass.clear()
  styleClass.add("displayable")
  styleClass.add("no_trash_alert")
  styleClass.add("no_id_assigned")

  scaleX <== scale
  scaleY <== scale

  minWidth = Configuration.stringvisualizationWidth + 2 * Configuration.stringvisualizationPadding


  assert(instance.getInputObjects.containsKey(key))
  val algObj = instance.getInputObjects.get(key)
  assert(algObj.isInstanceOf[java.util.List[String]])
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])

  val text = new Text("\n" + stringRepresentation) {   //leading \n fixes alignment
    wrappingWidth = Configuration.stringvisualizationWidth
  }

  children.add(text)

  text.translateX = text.translateX.value + Configuration.stringvisualizationPadding
  text.translateY = text.translateY.value + Configuration.stringvisualizationPadding

  minHeight = boundsInLocal.value.getHeight + Configuration.stringvisualizationPadding

  override def enableInteraction(): Unit = {
    onScroll = Util.handleScroll(this)
    onZoom = Util.handleZoom(this, scale)
  }
}
