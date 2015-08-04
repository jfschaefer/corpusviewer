package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, InstanceWrapper}

import scalafx.scene.layout.Pane


/*
  A preview that displays several interpretations if they are available.
 */

class BigPreview(iw : InstanceWrapper, interpretations: Set[String]) extends Pane with Preview {
  override def getIw = iw
  scaleX <== scale
  scaleY <== scale

  setupStyleStuff()

  val overviewGroup = new OverviewGroup(iw, interpretations, forPreview = true)

  overviewGroup.translateY = Configuration.previewMargin

  children.add(overviewGroup)

  minHeight = overviewGroup.boundsInParent.value.getHeight + Configuration.previewMargin * 3
  minWidth = Configuration.preferredPreviewWidth

  overviewGroup.boundsInLocal.onChange {
    minHeight = overviewGroup.getHeight + Configuration.previewMargin
  }

  override def getHeight : Double = overviewGroup.getHeight + Configuration.previewMargin
}
