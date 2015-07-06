package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Displayable

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.Group

class Trash extends Group {
  val iv = new ImageView {
    image = new Image("file://" + System.getProperty("user.dir") + "/icons/trash.png")
    styleClass.add("displayable")
    styleClass.add("no_trash_alert")
  }

  iv.setFitWidth(Configuration.trashWidth)
  iv.setFitHeight(Configuration.trashHeight)

  iv.layoutX <== Main.stage.width - Configuration.trashWidth - Configuration.windowMargin
  iv.layoutY <== Main.stage.height - Configuration.trashHeight - Configuration.windowMargin
  //println("layoutX: " + (Main.stage.width.value - Configuration.trashWidth - Configuration.windowMargin))

  children.add(iv)

  // Returns true iff the intersection of the intervals (a0, a1) and (b0, b1) is non-empty
  def intervalOverlap(a0: Double, a1: Double, b0: Double, b1: Double): Boolean =
    (a0 < b0 && a1 > b0) || (a0 < b1 && a1 > b1) || (b0 < a0 && a0 < b1)

  def isOverTrash(d: Displayable): Boolean = {
    val sceneBounds = d.localToScene(d.boundsInLocal.value)
    val ivsceneBounds = iv.boundsInParent.value //localToScene(iv.boundsInLocal.value)

    //over preview secton?
    if (Configuration.previewIsTrashZone && sceneBounds.getMinX < Configuration.previewSectionWidth) true

    //over trash icon?
    else if (intervalOverlap(sceneBounds.getMinX, sceneBounds.getMaxX, ivsceneBounds.getMinX, ivsceneBounds.getMaxX) &&
             intervalOverlap(sceneBounds.getMinY, sceneBounds.getMaxY, ivsceneBounds.getMinY, ivsceneBounds.getMaxY)) true

    //otherwise, not over trash
    else false
  }
}
