package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.Main

import de.up.ling.irtg.corpus.Instance
import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.scene.layout.Pane
import scalafx.scene.control.Label


// Uses some kind of a String representation for preview
// Since it is a root, it can be used to open the other interpretations
class TextRoot(instance: Instance) extends Pane with Displayable {
  val instanceMap = instance.getInputObjects
  var stringRepresentation: String = ""
  if (instanceMap.containsKey("string")) {
    val stringAlgebra = instanceMap.get("string")
    if (stringAlgebra.isInstanceOf[StringAlgebra]) {
      //val stringAlgebraActual : StringAlgebra = stringAlgebra.asInstanceOf
      stringRepresentation = Main.getCorpus.algebraMap.get("string").asInstanceOf[StringAlgebra].representAsString(stringAlgebra.asInstanceOf[java.util.List[String]])
    }
  }
  // If no string algebra has been found, use the string representation of the instance instead
  if (stringRepresentation == "") {
    stringRepresentation = instance.toString
  }
  if (stringRepresentation == "") {
    stringRepresentation = "[No string representation has been found]"
  }

  val textLabel = new Label(stringRepresentation) {
    styleClass.clear()
    styleClass.add("textRoot_string")
  }


  children.add(textLabel)
}
