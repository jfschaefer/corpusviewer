package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.Configuration

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.scene.text.Text

/** Top part of a Displayable. It contains a title and a RadialMenu.
  * @param textString the title
  * @param radialMenu the menu
  */

class Header(textString: String, radialMenu: Option[RadialMenu]) extends Pane {
  val headerWidth = new DoubleProperty()
  headerWidth.set(Configuration.preferredPreviewWidth)

  val text = new Text("\n" + textString) {
    styleClass.add("header_font")
  }

  children.add(text)
  text.layoutX = Configuration.headerMargin
  text.layoutY = Configuration.headerMargin

  radialMenu match {
    case Some(menu) =>
      children.add (menu)
      menu.layoutX <== headerWidth - Configuration.headerMenuButtonRadius - 5
      menu.layoutY = Configuration.headerMenuButtonRadius + 5
    case None =>
  }

  styleClass.clear()
  styleClass.add("header")
  minWidth <== headerWidth
  maxWidth <== headerWidth

  minHeight.set(getHeight)
  maxHeight.set(getHeight)

  def getHeight: Double = 2 * Configuration.headerMenuButtonRadius + 10
}
