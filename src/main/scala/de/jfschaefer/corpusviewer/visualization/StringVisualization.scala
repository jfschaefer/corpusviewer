package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.Configuration
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

  scaleX <== scale
  scaleY <== scale


  assert(instance.getInputObjects.containsKey(key))
  val algObj = instance.getInputObjects.get(key)
  assert(algObj.isInstanceOf[java.util.List[String]])
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])

  val text = new Text("\n" + stringRepresentation) {   //leading \n fixes alignment
    wrappingWidth = 500
  }

  children.add(text)

  override def enableInteraction(): Unit = {

  }
}
