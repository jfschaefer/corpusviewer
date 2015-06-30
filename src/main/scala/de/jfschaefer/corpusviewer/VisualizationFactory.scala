package de.jfschaefer.corpusviewer

import de.up.ling.irtg.corpus.Instance
import de.jfschaefer.corpusviewer.visualization._

trait AbstractVisualizationFactory {
  def getVisualization(instance: Instance, key: String): Displayable
  def getRootVisualization(instance: Instance): Displayable
}


class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  def getVisualization(instance: Instance, key: String): Displayable = {
    new TextRoot(instance)   //temporary
  }

  def getRootVisualization(instance: Instance): Displayable = {
    new TextRoot(instance)
  }
}
