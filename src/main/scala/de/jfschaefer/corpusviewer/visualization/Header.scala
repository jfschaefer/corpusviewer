package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.Configuration

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.scene.shape.Line


class Header(textString: String, radialMenu: Option[RadialMenu]) extends Pane {
  val headerWidth = new DoubleProperty()
  headerWidth.set(Configuration.preferredPreviewWidth)

  val text = new Text("\n" + textString) {
    styleClass.add("header_font")
  }
  text.setFill(Color.WHITE)

  children.add(text)
  text.layoutX = Configuration.graphicalrootMargin
  text.layoutY = Configuration.graphicalrootMargin

  radialMenu match {
    case Some(menu) => {
      children.add (menu)
      menu.layoutX <== headerWidth - Configuration.graphicalrootMenuButtonRadius - 5
      menu.layoutY = Configuration.graphicalrootMenuButtonRadius + 5
    }
    case None =>
  }

  /* val separator = new Line {
    startX = Configuration.graphicalrootMargin
    startY = 2 * Configuration.graphicalrootMenuButtonRadius
    endX <== headerWidth - Configuration.graphicalrootMargin * 2
    endY = 2 * Configuration.graphicalrootMenuButtonRadius
  }

  children.add(separator) */
  style = "-fx-background-color: rgba(0, 0, 0, 0.5)"
  minWidth <== headerWidth
  maxWidth <== headerWidth

  minHeight.set(getHeight)
  maxHeight.set(getHeight)

  def getHeight: Double = 2 * Configuration.graphicalrootMenuButtonRadius + 10
}
