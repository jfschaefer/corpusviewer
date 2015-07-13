package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Configuration, Main, Util}
import de.up.ling.irtg.algebra.StringAlgebra
import de.up.ling.irtg.algebra.graph.SGraph

import scalafx.beans.property.DoubleProperty
import scalafx.scene.Group
import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.input.{ScrollEvent, ZoomEvent}
import scalafx.Includes._

import scala.collection.JavaConversions._
import scala.collection.mutable

class GraphicalRoot(iw: InstanceWrapper, indeX: Int) extends Group with RootDisplayable {
  override val parentDisplayable = None
  override val index = indeX
  override def getIw = iw
  var childrenMap: mutable.Map[String, Displayable] = new mutable.HashMap
  val instanceMap = iw.instance.getInputObjects
  scale.set(1d)


  val pane = new Pane {
    scaleX <== scale
    scaleY <== scale
    styleClass.clear()
    styleClass.add("displayable")
    styleClass.add("no_trash_alert")
    styleClass.add("no_id_assigned")

    var idstyleclass: String = iw.getStyleClass
    styleClass.add(idstyleclass)

    iw.id onChange {
      onStyleClassIdUpdate()
    }

    def onStyleClassIdUpdate():Unit = {
      //styleClass.removeAll(idstyleclass)
      styleClass.remove(styleClass.indexOf(idstyleclass))
      idstyleclass = iw.getStyleClass
      styleClass.add(idstyleclass)
    }

    minWidth = Configuration.preferredPreviewWidth
  }


  /*
    HEADER VISUALIZATION
   */

  val menu = new RadialMenu {
    items = new MenuEntryFunction("trash", trash)::Nil
    for (key <- instanceMap.keySet) {
      items = new MenuEntryDisplayable(/* key = */ key, /* producer = */ () => Configuration.visualizationFactory.getVisualization(iw, key, GraphicalRoot.this),
                         /* onRelease = */ { disp: Displayable =>
                              if (childrenMap.contains(key)) childrenMap.get(key).orNull.trash()
                              // childrenMap.add((key, disp))
              childrenMap = childrenMap + (key -> disp)
      }) :: items
    }
  }
  menu.enableInteraction()
  val header = new Header(getIw.index.toString + ". Overview", menu)
  header.translateY = 10
  pane.children.add(header)

  children.add(pane)

  val headerBottom = new DoubleProperty
  headerBottom.set(2 * Configuration.graphicalrootMargin + 2 * Configuration.graphicalrootMenuButtonRadius + 10)


  /*
    STRING REPRESENTATION
   */

  val textBottom = new DoubleProperty
  textBottom.set(headerBottom.value)

  if (instanceMap.containsKey("string")) {
    val stringAlgebra = instanceMap.get("string")
    assert (stringAlgebra.isInstanceOf[java.util.List[String @unchecked]])
    val stringRepresentation = (new StringAlgebra).representAsString(stringAlgebra.asInstanceOf[java.util.List[String]])
    val stringText = new Text("\n" + stringRepresentation) {
      wrappingWidth = Configuration.preferredPreviewWidth - 2 * Configuration.graphicalrootMargin
    }

    stringText.layoutX = Configuration.graphicalrootMargin
    stringText.layoutY <== headerBottom + Configuration.graphicalrootMargin
    pane.children.add(stringText)

    textBottom.set(headerBottom.value + stringText.boundsInLocal.value.getHeight + Configuration.graphicalrootMargin)

    stringText.boundsInLocal onChange {
      textBottom.set(headerBottom.value + stringText.boundsInLocal.value.getHeight + Configuration.graphicalrootMargin)
    }

    headerBottom onChange {
      textBottom.set(headerBottom.value + stringText.boundsInLocal.value.getHeight + Configuration.graphicalrootMargin)
    }
  }


  /*
    GRAPH REPRESENTATION
   */

  val graphBottom = new DoubleProperty
  graphBottom.set(textBottom.value)

  if (instanceMap.containsKey("graph")) {
    val graphPane = new Pane {
      val algObj = instanceMap.get("graph")
      assert(algObj.isInstanceOf[SGraph])

      val sgraph: SGraph = algObj.asInstanceOf[SGraph]
      val graphGroup = new GraphVisualizationGraphGroup(sgraph, this, 1.0)
      children.add(graphGroup)

      minWidth = graphGroup.boundsInParent.value.getWidth + 2 * Configuration.graphvisualizationPadding
      minHeight = graphGroup.boundsInParent.value.getHeight + 2 * Configuration.graphvisualizationPadding
      maxWidth = minWidth.value
      maxHeight = minHeight.value
    }

    pane.children.add(graphPane)

    // graphPane.scaleX = 0.5
    // graphPane.scaleY = 0.5
    // val GRAPHFACTOR = -0.5

    // GRAPHFACTOR(SCALE) = 0.5 * SCALE - 0.5

    graphPane.scaleX = math.min(1d, Configuration.preferredPreviewWidth / graphPane.boundsInParent.value.getWidth)
    graphPane.scaleY = graphPane.scaleX.value

    def updateGraphAndItsBottom(): Unit = {
      graphBottom.set(textBottom.value + graphPane.boundsInParent.value.getHeight)
      graphPane.translateX = graphPane.translateX.value - graphPane.boundsInParent.value.getMinX
      //graphPane.translateY = graphPane.translateY.value + textBottom.value - graphPane.boundsInParent.value.getMinY - graphPane.boundsInLocal.value.getMinY
      graphPane.translateY = graphPane.translateY.value + textBottom.value - graphPane.boundsInParent.value.getMinY -
                        graphPane.boundsInLocal.value.getMinY //+ (0.5 * graphPane.scaleY.value - 0.5) * graphPane.boundsInLocal.value.getHeight
    }

    updateGraphAndItsBottom()

    graphPane.boundsInLocal onChange {
      graphPane.scaleX = math.min(1, graphPane.scaleX.value * Configuration.preferredPreviewWidth / graphPane.boundsInParent.value.getWidth)
      graphPane.scaleY = graphPane.scaleX.value
      updateGraphAndItsBottom()
    }

    textBottom onChange {
      updateGraphAndItsBottom()
    }
  }

  pane.minHeight = graphBottom.value + Configuration.graphicalrootMargin
  pane.maxHeight = graphBottom.value + Configuration.graphicalrootMargin
  pane.minWidth = Configuration.preferredPreviewWidth
  pane.maxWidth = Configuration.preferredPreviewWidth
  graphBottom onChange { (_, _, _) =>
    pane.minHeight = graphBottom.value + Configuration.graphicalrootMargin
    pane.maxHeight = graphBottom.value + Configuration.graphicalrootMargin
  }

  menu.toFront()

  override def enableInteraction(): Unit = {
    pane.onZoom = {ev : ZoomEvent => Util.handleZoom(pane, scale)(ev); Util.trashStyleUpdate(this, pane) }
    onScroll = {ev: ScrollEvent => Util.handleScroll(this)(ev); Util.trashStyleUpdate(this, pane); drawLocationLines() }

    pane.onZoomFinished = {ev: ZoomEvent => Util.trashIfRequired(this) }
    onScrollFinished = {ev: ScrollEvent => Util.trashIfRequired(this); removeLocationLines() }
  }

  override def trash(): Unit = {
    iw.releaseId()
    for (child <- childrenMap.values) {
      child.trash()
    }
    Main.corpusScene.getChildren.remove(this)
    removeLocationLines()
  }
}
