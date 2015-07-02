package de.jfschaefer.corpusviewer.visualization

import de.up.ling.irtg.corpus.Instance

import scalafx.scene.Group
import scalafx.scene.shape.Rectangle
import scalafx.scene.control.Label

// Remark: Can be controlled by simply placing bgRect at the desired position
class InterpretationRepresenter(algType : String, instance: Instance) extends Group {
  val bgRect = new Rectangle {
    style = "-fx-fill: red"
    height = 25
  }

  val label = new Label(algType)

  bgRect.boundsInParent onChange {
    updateLabelPos()
  }

  label.boundsInLocal onChange {
    updateLabelPos()
  }

  def updateLabelPos():Unit = {
    val bounds = bgRect.getBoundsInParent
    label.layoutX = bounds.getMinX + bounds.getWidth * 0.5 - label.getBoundsInLocal.getWidth * 0.5
    label.layoutY = bounds.getMinY + bounds.getHeight * 0.5 - label.getBoundsInLocal.getHeight * 0.5
  }

  children.add(bgRect)
  children.add(label)
}
