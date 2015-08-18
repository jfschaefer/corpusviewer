package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization.Displayable

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.Group

/** The trash - not just the icon in the bottom right corner, but also any other area where Displayables can be trashed
  *
  */
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

  children.add(iv)

  /** Returns true iff the intersection of two intervals is non-empty
    *
    * @param a0 start of the first interval
    * @param a1 end of the first interval
    * @param b0 start of the second interval
    * @param b1 end of the second interval
    * @return
    */
  def intervalOverlap(a0: Double, a1: Double, b0: Double, b1: Double): Boolean =
    (a0 < b0 && a1 > b0) || (a0 < b1 && a1 > b1) || (b0 < a0 && a0 < b1)

  /** Checks if a Displayable is currently over some trash area
    *
    * @param d the Displayable
    * @return true iff it is over some trash area
    */
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
