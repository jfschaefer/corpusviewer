package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Main, Configuration}

import de.up.ling.irtg.corpus.Instance
import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.scene.layout.Pane
import scalafx.scene.Group
import scalafx.scene.control.Label
import scalafx.scene.text.Text
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.Includes._

import scala.collection.JavaConversions._
import scala.collection.mutable

// Uses some kind of a String representation for preview
// Since it is a root, it can be used to open the other interpretations
class TextRoot(instance: Instance) extends Pane with Displayable {
  styleClass.add("textRoot")

  val instanceMap = instance.getInputObjects

  // FIND STRING REPRESENTATION

  var stringRepresentation: String = ""
  if (instanceMap.containsKey("string")) {
    val stringAlgebra = instanceMap.get("string")
    //if (stringAlgebra.isInstanceOf[StringAlgebra]) {
     // stringRepresentation = Main.getCorpus.algebraMap.get("string").asInstanceOf[StringAlgebra].representAsString(stringAlgebra.asInstanceOf[java.util.List[String]])
    stringRepresentation = (new StringAlgebra).representAsString(stringAlgebra.asInstanceOf[java.util.List[String]])
    //} else if (stringAlgebra.isInstanceOf[java.util.ArrayList[java.lang.String]]) {
  }
  // If no string algebra has been found, use the string representation of the instance instead
  if (stringRepresentation == "") {
    stringRepresentation = instance.toString
  }
  if (stringRepresentation == "") {
    stringRepresentation = "[No string representation has been found]"
  }


  // GENERATE CHILDREN

  val text = new Text("\n" + stringRepresentation) {   //leading \n fixes alignment
    wrappingWidth = Configuration.preferredPreviewWidth
  }

  val keyset = instanceMap.keySet
  val interpretationsMap = mutable.HashMap.empty[String, InterpretationRepresenter]
  var irXPos = 0d
  for (key <- keyset) {
    val ir = new InterpretationRepresenter(key, instance)
    interpretationsMap += (key -> ir)
    ir.bgRect.width = Configuration.textrootIrWidth
    children.add(ir)
    ir.bgRect.layoutX = irXPos
    ir.bgRect.layoutY = text.boundsInParent.value.getHeight + 15
    irXPos += Configuration.textrootIrWidth + Configuration.textrootIrGap
  }

  children.add(text)


  override def enableInteraction(): Unit = {

  }
}
