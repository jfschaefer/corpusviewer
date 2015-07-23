package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable
  def getPreview(iw: InstanceWrapper): Preview
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  override def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable = {
    if (key.equals("overview")) {
      new OverviewDisplayable(iw, parentDisplayable)
    } else if (key.equals("string")) {
      new StringVisualization(iw, parentDisplayable, key)
    } else if (key.equals("graph")) {
      new GraphVisualization(iw, parentDisplayable, key)
    } else {
      new NoVisualization(iw, parentDisplayable, key)
    }
  }

  override def getPreview(iw: InstanceWrapper): Preview = {
    new BigPreview(iw)
  }
}
