package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Displayable): Displayable
  def getRootVisualization(iw: InstanceWrapper, index: Int): RootDisplayable
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Displayable): Displayable = {
    if (key == "string") new StringVisualization(iw, key, parentDisplayable)
    else new NoVisualization(iw, key, parentDisplayable)
  }

  def getRootVisualization(iw: InstanceWrapper, index: Int): RootDisplayable = {
    new TextRoot(iw, index)
  }
}
