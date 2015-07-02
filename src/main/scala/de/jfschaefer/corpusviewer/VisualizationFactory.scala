package de.jfschaefer.corpusviewer

import de.up.ling.irtg.corpus.Instance
import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(instance: Instance, key: String, parentDisplayable: Displayable): Displayable
  def getRootVisualization(instance: Instance, index: Int): RootDisplayable
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  def getVisualization(instance: Instance, key: String, parentDisplayable: Displayable): Displayable = {
    if (key == "string") new StringVisualization(instance, key, parentDisplayable)
    else new TextRoot(instance, 0)   //TODO: Create a Displayable with an error message instead
  }

  def getRootVisualization(instance: Instance, index: Int): RootDisplayable = {
    new TextRoot(instance, index)
  }
}
