package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.Configuration

import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.shape.Line


class Header(textString: String, radialMenu: RadialMenu) extends Pane {
  val text = new Text("\n" + textString) {
    Configuration.preferredPreviewWidth - 3 * Configuration.graphicalrootMargin - 2 * Configuration.graphicalrootMenuButtonRadius
    styleClass.add("header_font")
  }

  children.add(text)
  text.layoutX = Configuration.graphicalrootMargin
  text.layoutY = Configuration.graphicalrootMargin

  children.add(radialMenu)
  radialMenu.layoutX = Configuration.preferredPreviewWidth - Configuration.graphicalrootMargin - Configuration.graphicalrootMenuButtonRadius
  radialMenu.layoutY = Configuration.graphicalrootMargin + Configuration.graphicalrootMenuButtonRadius

  val separator = new Line {
    startX = Configuration.graphicalrootMargin
    startY = Configuration.graphicalrootMargin * 2 + 2 * Configuration.graphicalrootMenuButtonRadius
    endX = Configuration.preferredPreviewWidth - Configuration.graphicalrootMargin * 2
    endY = Configuration.graphicalrootMargin * 2 + 2 * Configuration.graphicalrootMenuButtonRadius
  }

  children.add(separator)

}
