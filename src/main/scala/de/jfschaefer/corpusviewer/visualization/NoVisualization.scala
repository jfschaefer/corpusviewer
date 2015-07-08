package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Main, InstanceWrapper, Util, Configuration}

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.input.{ZoomEvent, ScrollEvent}
import scalafx.Includes._

class NoVisualization(iw: InstanceWrapper, key: String, parentD: Displayable) extends Pane with Displayable {
  override val parentDisplayable = Some(parentD)
  override val scale = new DoubleProperty
  override def getIw = iw
  scale.set(1d)

  setupStyleStuff()

  scaleX <== scale
  scaleY <== scale

  val text = new Text("\nThere is no implementation for the visualization of [" + key + "] yet.")
  children.add(text)

  text.translateX = text.translateX.value + Configuration.stringvisualizationPadding
  text.translateY = text.translateY.value + Configuration.stringvisualizationPadding

  minWidth = Configuration.stringvisualizationWidth + 2 * Configuration.stringvisualizationPadding
  minHeight = boundsInLocal.value.getHeight + 2 * Configuration.stringvisualizationPadding


  override def enableInteraction(): Unit = {
    onZoom = {ev : ZoomEvent => Util.handleZoom(this, scale)(ev); Util.trashStyleUpdate(this, this) }
    onScroll = {ev: ScrollEvent => Util.handleScroll(this)(ev); Util.trashStyleUpdate(this, this); drawLocationLines() }

    onZoomFinished = {ev: ZoomEvent => Util.trashIfRequired(this) }
    onScrollFinished = {ev: ScrollEvent => Util.trashIfRequired(this); removeLocationLines() }
  }

  override def trash(): Unit = {
    removeLocationLines()
    Main.corpusScene.getChildren.remove(this)
  }
}
