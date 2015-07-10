package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Util, Configuration, Main}
import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.Includes._
import scalafx.scene.text.Text
import scalafx.scene.input.{ZoomEvent, ScrollEvent}

class StringVisualization(iw: InstanceWrapper, key: String, parentD: Displayable) extends Pane with Displayable {
  override val parentDisplayable = Some(parentD)
  override val scale = new DoubleProperty
  override def getIw = iw
  isInInitialExpansion.set(true)
  scale.set(1d)

  setupStyleStuff()

  scaleX <== scale
  scaleY <== scale

  minWidth = Configuration.stringvisualizationWidth + 2 * Configuration.stringvisualizationPadding

  val menu = new RadialMenu
  menu.enableInteraction()
  val header = new Header(getIw.index.toString + ". String", menu)
  header.translateY = 10
  children.add(header)


  assert(iw.instance.getInputObjects.containsKey(key))
  val algObj = iw.instance.getInputObjects.get(key)
  assert(algObj.isInstanceOf[java.util.List[String @unchecked]])    // is unchecked due to type erasure
  val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])

  val text = new Text("\n" + stringRepresentation) {   //leading \n fixes alignment
    wrappingWidth = Configuration.stringvisualizationWidth
  }

  children.add(text)

  text.translateX = text.translateX.value + Configuration.stringvisualizationPadding
  text.translateY = text.translateY.value + Configuration.stringvisualizationPadding + header.boundsInParent.value.getMaxY

  minHeight = boundsInLocal.value.getHeight + Configuration.stringvisualizationPadding

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
