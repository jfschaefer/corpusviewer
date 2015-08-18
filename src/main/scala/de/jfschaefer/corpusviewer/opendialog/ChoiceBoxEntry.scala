package de.jfschaefer.corpusviewer.opendialog

/** A wrapper to put arbitrary objects in a [[scalafx.scene.control.ChoiceBox]]
  *
  * @param content the object
  * @param string the string representation of it, which will be displayed in the ChoiceBox
  * @tparam T the type of the object
  */
class ChoiceBoxEntry[T](content : T, string : String) {
  def unwrap: T = content
  override def toString(): String = string
}
