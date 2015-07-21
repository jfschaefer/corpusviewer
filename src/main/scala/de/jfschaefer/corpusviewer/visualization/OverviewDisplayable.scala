package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Configuration}


import scalafx.scene.layout.Pane

import scala.collection.JavaConversions._

class OverviewDisplayable(iw : InstanceWrapper, parentDisp : Option[Displayable]) extends Pane with Displayable {
  override val parentDisplayable = parentDisp
  override def getIw = iw

  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()

  // HEADER
  val menu = new RadialMenu {
    items = Nil
    for (key <- iw.instance.getInputObjects.keySet) {
      items = new MenuEntryDisplayable(label = key,
        dispProducer = () => Configuration.visualizationFactory.getVisualization(iw, key, Some(OverviewDisplayable.this)),
        onRelease = { disp: Displayable => {
          childDisplayables.add(disp)
        }}) :: items
    }
  }
  menu.enableInteraction()
  val header = new Header(iw.index + ". Overview", Some(menu))

  children.add(header)

  // CONTENT
  val overviewGroup = new OverviewGroup(iw)
  overviewGroup.translateY = header.getHeight + Configuration.previewMargin

  children.add(overviewGroup)


  header.toFront()

  minHeight = overviewGroup.getHeight + Configuration.previewMargin + header.getHeight
  minWidth = Configuration.preferredPreviewWidth
  maxWidth = Configuration.preferredPreviewWidth
}
