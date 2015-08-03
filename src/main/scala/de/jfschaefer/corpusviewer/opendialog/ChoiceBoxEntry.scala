package de.jfschaefer.corpusviewer.opendialog

class ChoiceBoxEntry[T](content : T, string : String) {
  def unwrap: T = content
  override def toString(): String = string
}
