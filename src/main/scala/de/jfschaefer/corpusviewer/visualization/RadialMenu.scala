package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{Configuration, Main}

import scalafx.beans.property.DoubleProperty
import scalafx.scene.{Group, Node}
import scalafx.scene.image.ImageView
import scalafx.scene.input.MouseEvent
import scalafx.Includes._
import scalafx.scene.shape.Circle
import scalafx.scene.control.Label


/*
    A RadialMenu, that can be provided with a list of MenuEntries.
    A MenuEntry is either a MenuEntryFunction (in this case a function is called when it is selected)
    or a MenuEntryDisplayable (in this case a Displayable can be pulled out of the menu).

    TODO: Rewrite this code, as it got fairly messy
 */

class RadialMenu extends Group {
  var items : List[MenuEntry] = Nil
  var itemNodes: List[Node] = Nil
  var itemPos : List[(Double, Double, MenuEntry)] = Nil
  var isExpanded = false

  val radius = new DoubleProperty()
  radius.set(Configuration.radialMenuExpansionRadius)
  val iv = new ImageView {
    image = Configuration.radialMenuButtonImage
  }

  iv.fitWidth = Configuration.headerMenuButtonRadius * 2
  iv.fitHeight = Configuration.headerMenuButtonRadius * 2
  iv.layoutX = -Configuration.headerMenuButtonRadius
  iv.layoutY = -Configuration.headerMenuButtonRadius

  var draggedDisplayable : Option[Displayable] = None
  var draggedDispEntry: Option[MenuEntryDisplayable] = None

  children.add(iv)

  val expandedBackground = new Circle {
    radius <== RadialMenu.this.radius
    styleClass.clear()
    styleClass.add("radialmenu_expanded_circle")
  }

  def enableInteraction(): Unit = {
    onMousePressed = { ev: MouseEvent =>
      if (!isExpanded) expand()
      isExpanded = true
      ev.consume()
    }

    onMouseDragged = { ev: MouseEvent =>
      if (draggedDisplayable == None) for (item <- items) item.setNormalLayout()
      if (!isExpanded) expand()
      if (draggedDisplayable.isEmpty) {
        for ((x, y, i) <- itemPos) {
          if ((x - ev.x) * (x - ev.x) + (y - ev.y) * (y - ev.y) < entryRadius * entryRadius) {
            i.setMarkedLayout()
            i match {
              case MenuEntryDisplayable(_, producer, _) =>
                val d = producer()
                d.isInInitialExpansion.set(true)
                children.add(d)
                d.scale.set(0)  // will be visible at next drag
                draggedDisplayable = Some(d)
                draggedDispEntry = Some(i.asInstanceOf[MenuEntryDisplayable])
              case _ =>  // Could highlight currently selected entry
            }
          }
        }
      } else {
        draggedDisplayable match {
          case Some(disp) =>
            val distance = math.sqrt(ev.x * ev.x + ev.y * ev.y)
            val scale = if (distance / radius.value < 1.5) distance/(radius.value * 1.5) else 1
            disp.isInInitialExpansion.set(distance < 1.5 * radius.value)
            disp.scale.set(scale)
            disp.layoutX = ev.x - disp.boundsInLocal.value.getWidth * 0.5
            disp.layoutY = ev.y - disp.boundsInLocal.value.getHeight * 0.5
          case None => System.err.println("This shouldn't have happened (in de.jfschaefer.corpusviewer.visualization.RadialMenu)")
        }
      }
      ev.consume()
    }

    onMouseReleased = { ev: MouseEvent =>
      draggedDisplayable match {
        case Some(disp) =>
          val distance = math.sqrt(ev.x * ev.x + ev.y * ev.y)
          if (distance >= 1.5 * radius.value) {
            val boundsInScene = disp.localToScene(disp.boundsInLocal.value)
            children.removeAll(disp)
            Main.corpusScene.getChildren.add(disp)
            disp.scale.set(disp.parentDisplayable match {
              case Some(d) => d.scale.value
              case None => Configuration.initialScale
            })
            disp.translateX = disp.translateX.value - disp.boundsInParent.value.getMinX + boundsInScene.getMinX
            disp.translateY = disp.translateY.value - disp.boundsInParent.value.getMinY + boundsInScene.getMinY
            disp.isInInitialExpansion.set(false)
            disp.enableInteraction()
            draggedDispEntry.orNull.release(disp)

            draggedDisplayable = None
            draggedDispEntry = None
          }
        case None =>
          for ((x, y, i) <- itemPos) {
            if ((x - ev.x) * (x - ev.x) + (y - ev.y) * (y - ev.y) < entryRadius * entryRadius) {
              i match {
                case MenuEntryFunction(_, f) => f()
                case _ =>
              }
            }
            }
      }
      deflate()
      isExpanded = false
      ev.consume()
    }
  }

  var entryRadius = Configuration.radialMenuEntryRadius

  def expand(): Unit = {
    toFront()
    for (n <- itemNodes) children.removeAll(n)
    itemNodes = Nil
    children.add(expandedBackground)
    iv.toFront()
    if (items.nonEmpty) {
      val angleStep = math.Pi * 2 / items.length
      var cumulativeAngle = 0d
      itemPos = Nil
      for (i <- items) {
        val circ = new Circle {
          radius = entryRadius
          styleClass.clear()
          styleClass.add(i match {
            case MenuEntryDisplayable(_, _, _) => "radialmenu_entry_displayable"
            case MenuEntryFunction(_, _) => "radialmenu_entry_function"
          })
        }
        circ.centerX = math.sin(cumulativeAngle) * (radius.value - entryRadius)
        circ.centerY = math.cos(cumulativeAngle) * (radius.value - entryRadius)
        children.add(circ)

        i.setCircle(Some(circ))

        itemPos = (circ.centerX.value, circ.centerY.value, i) :: itemPos

        val text = new Label(i.getLabel) {
          styleClass.clear()
          styleClass.add( i match {
            case MenuEntryDisplayable(_, _, _) => "radialmenu_entry_displayable_text"
            case MenuEntryFunction(_, _) => "radialmenu_entry_function_text"
          })
          textAlignment.set(javafx.scene.text.TextAlignment.CENTER)
        }

        def updateTextPos(): Unit = {
          text.layoutX = circ.centerX.value - text.boundsInParent.value.getWidth * 0.5
          text.layoutY = circ.centerY.value - text.boundsInParent.value.getHeight * 0.5
        }

        updateTextPos()
        text.boundsInLocal onChange updateTextPos
        children.add(text)

        itemNodes = text :: circ :: itemNodes

        cumulativeAngle += angleStep
      }
    }
  }

  def deflate(): Unit = {
    for (n <- itemNodes) children.removeAll(n)
    children.removeAll(expandedBackground)
    draggedDisplayable match {
      case Some(disp) => children.removeAll(disp)
      case None =>
    }
    for (i <- items) {
      i.setCircle(None)
    }
    draggedDisplayable = None
  }
}



abstract class MenuEntry(label: String) {
  var circle: Option[Circle] = None
  def getLabel: String = label
  def getCircle: Option[Circle] = circle
  def setCircle(circle: Option[Circle]): Unit = { this.circle = circle }
  def setNormalLayout(): Unit
  def setMarkedLayout(): Unit
}

case class MenuEntryDisplayable(label: String, dispProducer: () => Displayable, onRelease: (Displayable) => Unit) extends MenuEntry(label) {
  // use currying to achieve lazy evaluation
  def getDisplayable: Displayable = dispProducer()
  def release(d : Displayable) = onRelease(d)

  override def setNormalLayout(): Unit = {
    println("THIS: " + this + "    normal")
    getCircle match {
      case Some(x: Circle) => { x.styleClass.clear(); x.styleClass.add("radialmenu_entry_displayable") }
      case None => {}
    }
  }
  override def setMarkedLayout(): Unit = {
    println("THIS: " + this + "    unnormal")
    getCircle match {
      case Some(x: Circle) => { x.styleClass.clear(); x.styleClass.add("radialmenu_entry_displayable_marked") }
      case None => {}
    }
  }
}

case class MenuEntryFunction(label: String, function: () => Unit) extends MenuEntry(label) {
  def callFunction(): Unit = function()

  override def setNormalLayout(): Unit = {
    getCircle match {
      case Some(x: Circle) => { x.styleClass.clear(); x.styleClass.add("radialmenu_entry_function") }
      case None => {}
    }
  }
  override def setMarkedLayout(): Unit = {
    getCircle match {
      case Some(x: Circle) => { x.styleClass.clear(); x.styleClass.add("radialmenu_entry_function_marked") }
      case None => {}
    }
  }
}

