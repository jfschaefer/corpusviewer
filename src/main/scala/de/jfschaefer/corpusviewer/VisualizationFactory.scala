package de.jfschaefer.corpusviewer

import de.up.ling.irtg.corpus.Instance
import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Displayable): Displayable
  def getRootVisualization(iw: InstanceWrapper, index: Int): RootDisplayable
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Displayable): Displayable = {
    if (key == "string") new StringVisualization(iw, key, parentDisplayable)
    else new TextRoot(iw, 0)   //TODO: Create a Displayable with an error message instead
  }

  def getRootVisualization(iw: InstanceWrapper, index: Int): RootDisplayable = {
    new TextRoot(iw, index)
  }
}
