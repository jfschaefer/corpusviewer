package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Main, Configuration, Util}

import de.up.ling.irtg.corpus.Instance
import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.beans.property.DoubleProperty
import scalafx.scene.layout.Pane
import scalafx.scene.Group
import scalafx.scene.control.Label
import scalafx.scene.text.Text
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.scene.input.{MouseEvent, ZoomEvent}
import scalafx.Includes._

import scala.collection.JavaConversions._
import scala.collection.mutable

// Uses some kind of a String representation for preview
// Since it is a root, it can be used to open the other interpretations
class TextRoot(instance: Instance, indeX: Int) extends Group with RootDisplayable {
  override val parentDisplayable = None
  override val index = indeX
  override val scale = new DoubleProperty
  scale.set(1d)

  val instanceMap = instance.getInputObjects


  // FIND STRING REPRESENTATION

  var stringRepresentation: String = ""
  if (instanceMap.containsKey("string")) {
    val stringAlgebra = instanceMap.get("string")
    if (stringAlgebra.isInstanceOf[java.util.List[String]]) {
      stringRepresentation = (new StringAlgebra).representAsString(stringAlgebra.asInstanceOf[java.util.List[String]])
    }
  }
  // If no string algebra has been found, use the string representation of the instance instead
  if (stringRepresentation == "") {
    stringRepresentation = instance.toString
  }
  if (stringRepresentation == "") {
    stringRepresentation = "[No string representation has been found]"
  }

  // GENERATE CHILDREN
  val pane = new Pane {
    scaleX <== scale
    scaleY <== scale
    styleClass.clear()
    styleClass.add("displayable")
    styleClass.add("no_trash_alert")
    styleClass.add("no_id_assigned")
    minWidth = Configuration.preferredPreviewWidth
  }

  val text = new Text("\n" + stringRepresentation) {   //leading \n fixes alignment
    wrappingWidth = Configuration.preferredPreviewWidth - 2 * Configuration.textrootMargin
  }

  var draggedInterpretationStartPos = (0d, 0d)    //In sceneX|sceneY coordinates
  var draggedInterpretationLastPos = (0d, 0d)     //In ev.x|ev.y coordinates
  var draggedInterpretationStartScale = 0d
  var draggedInterpretationNode: Option[Displayable] = None

  val keyset = instanceMap.keySet
  val interpretationsMap = mutable.HashMap.empty[String, InterpretationRepresenter]
  var irXPos = Configuration.textrootMargin

  for (key <- keyset) {
    val ir = new InterpretationRepresenter(key, instance, this)
    interpretationsMap += (key -> ir)
    ir.bgRect.width = Configuration.textrootIrWidth
    pane.children.add(ir)
    ir.bgRect.layoutX = irXPos
    ir.bgRect.layoutY = text.boundsInParent.value.getHeight + Configuration.textrootMargin
    irXPos += Configuration.textrootIrWidth + 2 * Configuration.textrootMargin
  }

  pane.children.add(text)
  text.translateX = text.translateX.value + Configuration.textrootMargin
  text.translateY = text.translateY.value + Configuration.textrootMargin

  children.add(pane)


  override def enableInteraction(): Unit = {
    onZoom = Util.handleZoom(this, scale)
    onScroll = Util.handleScroll(this)

    onMouseDragged = { ev: MouseEvent =>
      draggedInterpretationNode match {
        case Some(d: Displayable) =>
          d.translateX = d.translateX.value + ev.x - draggedInterpretationLastPos._1
          d.translateY = d.translateY.value + ev.y - draggedInterpretationLastPos._2
          val distance = math.sqrt((ev.sceneX - draggedInterpretationStartPos._1) * (ev.sceneX - draggedInterpretationStartPos._1) +
                                   (ev.sceneY - draggedInterpretationStartPos._2) * (ev.sceneY - draggedInterpretationStartPos._2))
          val goald = Configuration.textrootInterpretationDragoutDistance * math.sqrt(scale.value)
          val scalE = if (distance < goald)
              draggedInterpretationStartScale + (scale.value - draggedInterpretationStartScale) * distance / goald
            else scale.value     //distance non-negative, so this covers all the cases

          d.scale.set(scalE)

          draggedInterpretationLastPos = (ev.x, ev.y)
        case None =>
      }
    }

    onMouseReleased = { ev: MouseEvent =>
      draggedInterpretationNode match {
        case Some(d: Displayable) =>
          val distance = math.sqrt((ev.sceneX - draggedInterpretationStartPos._1) * (ev.sceneX - draggedInterpretationStartPos._1) +
            (ev.sceneY - draggedInterpretationStartPos._2) * (ev.sceneY - draggedInterpretationStartPos._2))
          val goald = Configuration.textrootInterpretationDragoutDistance * math.sqrt(scale.value)
          if (distance < goald) {
            children.remove(d)
          } else {
            d.scale.set(scale.value)
            /*
              d.translateX = d.translateX.value * scaleX.value + translateX.value
              d.translateY = d.translateY.value * scaleY.value + translateY.value
             */
            val bounds = d.localToScene(d.getBoundsInLocal)
            children.remove(d)
            Main.corpusScene.getChildren.add(d)
            d.translateX = d.translateX.value + bounds.getMinX - d.boundsInParent.value.getMinX
            d.translateY = d.translateY.value + bounds.getMinY - d.boundsInParent.value.getMinY
            d.enableInteraction()
          }
        case None =>
      }
      draggedInterpretationNode = None
    }

    for (ir <- interpretationsMap.values) {
      ir.enableInteraction()
    }
  }
}
