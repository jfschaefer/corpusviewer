package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable
  def getPreview(iw: InstanceWrapper): Preview
  def getOverview(iw: InstanceWrapper): Displayable
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  override def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable = {
    /*if (key.equals("overview")) {
      new OverviewDisplayable(iw, parentDisplayable)
    } else if (key.equals("string")) {
      new StringVisualization(iw, parentDisplayable, key)
    } else if (key.equals("graph")) {
      new GraphVisualization(iw, parentDisplayable, key)
    } else {
      new NoVisualization(iw, parentDisplayable, key)
    }*/
    val className = Main.interpretations.get(key).get
    if (className == null) {
      new NoVisualization(iw, parentDisplayable, key, "Sorry, but this interpretation doesn't appear to be defined in the grammar")
    } else if (className == "de.up.ling.irtg.algebra.StringAlgebra") {
      new StringVisualization(iw, parentDisplayable, key)
    } else if (className == "de.up.ling.irtg.algebra.graph.GraphAlgebra") {
      new GraphVisualization(iw, parentDisplayable, key)
    } else if (className == "de.up.ling.irtg.algebra.BinarizingTreeWithAritiesAlgebra") {
      new TreeStringVisualization(iw, parentDisplayable, key)
    } else {
      new NoVisualization(iw, parentDisplayable, key, "Sorry, visualizations for objects of " + className + " aren't supported yet")
    }
  }

  override def getPreview(iw: InstanceWrapper): Preview = {
    new BigPreview(iw)
  }

  override def getOverview(iw: InstanceWrapper) : Displayable = {
    new OverviewDisplayable(iw, None)
  }
}
