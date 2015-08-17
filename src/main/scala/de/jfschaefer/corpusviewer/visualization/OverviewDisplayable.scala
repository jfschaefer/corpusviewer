package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Util, InstanceWrapper, Configuration}

import scalafx.scene.input.ZoomEvent
import scalafx.scene.layout.Pane
import scalafx.Includes._

import scala.collection.JavaConversions._

/** Visualizes several interpretations of an instance
  *
  * Every interpretations (including the ones excluded in the overview) can be obtained as a child Displayable
  * via the menu in the header
  *
  * @param iw the instance
  * @param parentDisp the parent Displayable
  * @param interpretations the interpretations that shall be included in the overview
  */

class OverviewDisplayable(iw : InstanceWrapper, parentDisp : Option[Displayable], interpretations: Set[String])
                                                                                      extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX  // <== scale
  scaleY  // <== scale

  setupStyleStuff()

  // HEADER
  val menu = new RadialMenu {
    displayable = Some(OverviewDisplayable.this)
    items = new NormalMenuEntryFunction("Trash", () => trash())::
      new NormalMenuEntryFunction("Children\nToFront", () =>
        for (child <- childDisplayables) {
          child.toFront()
        }
      )::new NormalMenuEntryFunction("Trash\nChildren", () =>
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
  override val header = new Header(iw.getIDForUser + ". Overview", Some(menu))

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

