package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, InstanceWrapper, Configuration}

import scalafx.scene.input.ZoomEvent
import scalafx.scene.layout.Pane
import scalafx.Includes._

import scala.collection.JavaConversions._

/*
  A good root Displayable, as it displays several interpretations, if they are available
  and provides menu entries to get each interpretation as a separate child Displayable.
 */

class OverviewDisplayable(iw : InstanceWrapper, parentDisp : Option[Displayable], interpretations: Set[String]) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX  // <== scale
  scaleY  // <== scale

  setupStyleStuff()

  // HEADER
  val menu = new RadialMenu {
    displayable = Some(OverviewDisplayable.this)
    items = new MenuEntryFunction("Trash", () => trash())::
      new MenuEntryFunction("Children\nToFront", () =>
        for (child <- childDisplayables) {
          child.toFront()
        }
      )::new MenuEntryFunction("Trash\nChildren", () =>
        for (child <- childDisplayables) child.trash())::
      Nil
    for (key <- iw.instance.getInputObjects.keySet) {
      items = new MenuEntryDisplayable(label = key,
        dispProducer = () => Configuration.visualizationFactory.getVisualization(iw, key, Some(OverviewDisplayable.this)),
        onRelease = { disp: Displayable => {
          childDisplayables.add(disp)
        }}) :: items
    }
  }

  menu.enableInteraction()
  override val header = new Header(iw.index + ". Overview", Some(menu))

  children.add(header)

  // CONTENT
  val overviewGroup = new OverviewGroup(iw, interpretations) {
    /* scaleX <== scale
    scaleY <== scale */
  }

  overviewGroup.translateY = header.getHeight + Configuration.previewMargin

  children.add(overviewGroup)


  header.toFront()

  updateSize()

  def updateSize(): Unit = {
    minHeight = overviewGroup.getHeight + Configuration.previewMargin + header.getHeight
    maxHeight = overviewGroup.getHeight + Configuration.previewMargin + header.getHeight
    minWidth = Configuration.preferredPreviewWidth * overviewGroup.getScaleX
    maxWidth = Configuration.preferredPreviewWidth * overviewGroup.getScaleX
    header.headerWidth.set(Configuration.preferredPreviewWidth * overviewGroup.getScaleX)
  }


  onZoom = { ev : ZoomEvent => Util.dispHandleZoom(this, overviewGroup)(ev); updateSize()}
}

