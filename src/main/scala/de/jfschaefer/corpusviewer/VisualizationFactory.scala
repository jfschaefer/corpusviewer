package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable
  def getPreview(iw: InstanceWrapper): Preview
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  override def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable = {
    /* if (key.equals("string")) new StringVisualization(iw, key, parentDisplayable)
    else if (key.equals("graph")) new GraphVisualization(iw, key, parentDisplayable)
    else new NoVisualization(iw, key, parentDisplayable) */
    if (key.equals("overview")) {
      return new OverviewDisplayable(iw, parentDisplayable)
    }
    null
  }

  override def getPreview(iw: InstanceWrapper): Preview = {
    // new TextRoot(iw, index)
    //new GraphicalRoot(iw)
    new BigPreview(iw)
  }
}
