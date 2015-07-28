package de.jfschaefer.corpusviewer.visualization

import de.jfschaefer.corpusviewer.{InstanceWrapper, Configuration}

import de.up.ling.irtg.algebra.graph.SGraph
import de.up.ling.irtg.algebra.StringAlgebra

import scalafx.scene.Group
import scalafx.scene.text.Text

/*
    A group displaying several interpretations, if they are available.
    If a graph is too large, it's scaled down.
    This makes it perfect for preview/overview Displayables.
 */

class OverviewGroup(iw : InstanceWrapper, forPreview : Boolean = false) extends Group {
  val instanceMap = iw.instance.getInputObjects

  var cumulativeHeight = 0d

  /*
    STRING REPRESENTATION
   */

  if (instanceMap.containsKey("string")) {
    val algObj = instanceMap.get("string")
    assert(algObj.isInstanceOf[java.util.List[String @unchecked]])
    val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])
    val text = new Text("\n" + (if (forPreview) iw.index + ". " else "") + stringRepresentation) {
      wrappingWidth = Configuration.preferredPreviewWidth - 2 * Configuration.previewMargin
    }
    text.translateX = Configuration.previewMargin
    text.translateY = cumulativeHeight
    cumulativeHeight += text.boundsInLocal.value.getHeight
    children.add(text)
  }


  /*
    GRAPH REPRESENTATION
   */

  if (instanceMap.containsKey("graph")) {
    val algObj = instanceMap.get("graph")
    assert(algObj.isInstanceOf[SGraph])
    val sgraph: SGraph = algObj.asInstanceOf[SGraph]
    val graphpane = new SGraphPane(sgraph)
    val maxDim = math.max(graphpane.getWidth, graphpane.getHeight)
    val scale = math.min(Configuration.preferredPreviewWidth / maxDim, 1d)
    graphpane.scaleX = scale
    graphpane.scaleY = scale

    children.add(graphpane)
    graphpane.translateX = -(graphpane.getWidth * 0.5 * (1 - scale))
    graphpane.translateY = cumulativeHeight - (graphpane.getHeight * 0.5 * (1 - scale))
    cumulativeHeight += graphpane.getHeight * scale
  }

  def getHeight : Double = {
    cumulativeHeight * scaleY.value
  }
}
