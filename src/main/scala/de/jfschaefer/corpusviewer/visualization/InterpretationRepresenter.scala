package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Configuration}
import de.up.ling.irtg.corpus.Instance

import scalafx.scene.Group
import scalafx.scene.shape.Rectangle
import scalafx.scene.control.Label
import scalafx.scene.input.MouseEvent

import scalafx.Includes._

// Remark: Can be controlled by simply placing bgRect at the desired position
class InterpretationRepresenter(algType : String, iw: InstanceWrapper, root: TextRoot) extends Group {
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

  def enableInteraction(): Unit = {
    onMousePressed = { ev: MouseEvent =>
      root.draggedInterpretationNode = {
        val node = Configuration.visualizationFactory.getVisualization(iw, algType, root)
        node.scale.set(0.05)
        node.layoutX = boundsInParent.value.getMinX + 0.5 * (boundsInParent.value.getWidth - node.boundsInLocal.value.getWidth)
        node.layoutY = boundsInParent.value.getMinY + 0.5 * (boundsInParent.value.getHeight - node.boundsInLocal.value.getHeight)
        root.children.add(node)
        Some(node)
      }
      root.draggedInterpretationStartPos = (ev.sceneX, ev.sceneY)
      root.draggedInterpretationLastPos = (ev.x, ev.y)
      root.draggedInterpretationStartScale = 0.05     //let's start really small
      ev.consume()
    }
  }
}
