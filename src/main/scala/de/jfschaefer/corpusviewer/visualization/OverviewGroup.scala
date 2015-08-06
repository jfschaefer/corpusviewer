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

class OverviewGroup(iw : InstanceWrapper, previewInterpretations: Set[String], forPreview : Boolean = false) extends Group {
  val instanceMap = iw.instance.getInputObjects

  var cumulativeHeight = 0d

  if (forPreview) {
    val text = new Text("\n" + + iw.index)
    text.translateX = Configuration.previewMargin
    text.translateY = cumulativeHeight
    cumulativeHeight += text.boundsInLocal.value.getHeight
    children.add(text)
  }

    /*
      STRING REPRESENTATIONS
     */

  for (key <- previewInterpretations) {
    if (iw.interpretations.get(key).get == "de.up.ling.irtg.algebra.StringAlgebra") {
      assert(instanceMap.containsKey(key))
      val algObj = instanceMap.get(key)
      assert(algObj.isInstanceOf[java.util.List[String @unchecked]])
      val stringRepresentation = (new StringAlgebra).representAsString(algObj.asInstanceOf[java.util.List[String]])
      val text = new Text("\n" + stringRepresentation) {
        wrappingWidth = Configuration.preferredPreviewWidth - 2 * Configuration.previewMargin
      }
      text.translateX = Configuration.previewMargin
      text.translateY = cumulativeHeight
      cumulativeHeight += text.boundsInLocal.value.getHeight + Configuration.previewMargin
      children.add(text)
    }
  }


    /*
      GRAPH REPRESENTATIONS
     */

  for (key <- previewInterpretations) {
    if (iw.interpretations.get(key).get == "de.up.ling.irtg.algebra.graph.GraphAlgebra") {
      val algObj = instanceMap.get(key)
      assert(algObj.isInstanceOf[SGraph])
      val sgraph: SGraph = algObj.asInstanceOf[SGraph]
      val graphpane = new SGraphPane(sgraph)
      val maxDim = math.max(graphpane.getWidth, graphpane.getHeight)
      // don't show overly huge graphs in preview.
      if (!forPreview || maxDim / Configuration.preferredPreviewWidth < Configuration.previewMaxDownscale) {
        val scale = math.min(Configuration.preferredPreviewWidth / maxDim, 1d)
        graphpane.scaleX = scale
        graphpane.scaleY = scale

        children.add(graphpane)
        graphpane.translateX = -(graphpane.getWidth * 0.5 * (1 - scale))
        graphpane.translateY = cumulativeHeight - (graphpane.getHeight * 0.5 * (1 - scale))
        cumulativeHeight += graphpane.getHeight * scale
      }
    }
  }

  /*
    TREE REPRESENTATIONS
   */

  for (key <- previewInterpretations) {
    if (Set("de.up.ling.irtg.algebra.TreeAlgebra", "de.up.ling.irtg.algebra.BinarizingTreeAlgebra",
      "de.up.ling.irtg.algebra.BinarizingTreeWithAritiesAlgebra", "de.up.ling.irtg.algebra.TagTreeAlgebra",
        "de.up.ling.irtg.algebra.TreeWithAritiesAlgebra").contains(iw.interpretations.get(key).get)) {
      val algObj = instanceMap.get(key)
      assert(algObj.isInstanceOf[de.up.ling.tree.Tree[String @unchecked]])
      val tree = algObj.asInstanceOf[de.up.ling.tree.Tree[String]]
      val treePane = new TreePane(tree)
      val maxDim = math.max(treePane.getWidth, treePane.getHeight)
      // don't show overly huge trees in preview
      if (!forPreview || maxDim / Configuration.preferredPreviewWidth < Configuration.previewMaxDownscale) {
        val scale = math.min(Configuration.preferredPreviewWidth / maxDim, 1d)
        treePane.scaleX = scale
        treePane.scaleY = scale

        children.add(treePane)
        treePane.translateX = -(treePane.getWidth * 0.5 * (1 - scale))
        treePane.translateY = cumulativeHeight - (treePane.getHeight * 0.5 * (1 - scale))
        cumulativeHeight += treePane.getHeight * scale
      }
    }
  }

  cumulativeHeight += 2 * Configuration.previewMargin


    /* def findInterpretationKey(value: String): Option[String] = {
      for ((a, b) <- iw.interpretations) {
        if (b == value) return Some(a)
      }
      None
    } */

    def getHeight : Double = {
      cumulativeHeight * scaleY.value
    }
  }
