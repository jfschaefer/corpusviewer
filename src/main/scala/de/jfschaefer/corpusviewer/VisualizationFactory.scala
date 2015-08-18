package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.visualization._

/** A factory that generates the different kinds of visualizations required by the program */
trait AbstractVisualizationFactory {
  /** Creates a Displayable visualizing one specific interpretation
   *
   * @param iw the instance
   * @param key the interpretation to be visualized
   * @param parentDisplayable the parent Displayable, if it exists
   * @return the new Displayable
   */
  def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable

  /** Generates a preview, showing a certain subset of the interpretations
   *
   * @param iw the instance
   * @param interpretations the interpretations to be shown (if possible)
   * @return the new Preview
   */
  def getPreview(iw: InstanceWrapper, interpretations: Set[String]): Preview

  /** Creates an overview Displayable for an instance
   *
   * @param iw the instance
   * @param interpretations the interpretations to be shown (if possible)
   * @return the new Displayable
   */
  def getOverview(iw: InstanceWrapper, interpretations: Set[String]): Displayable
}

/** A concrete visualization factory */
class ConcreteVisualizationFactory extends AbstractVisualizationFactory {
  override def getVisualization(iw: InstanceWrapper, key: String, parentDisplayable: Option[Displayable]): Displayable = {
    val className = iw.interpretations.get(key).get
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

  override def getPreview(iw: InstanceWrapper, interpretations: Set[String]): Preview = {
    new BigPreview(iw, interpretations)
  }

  override def getOverview(iw: InstanceWrapper, interpretations: Set[String]) : Displayable = {
    new OverviewDisplayable(iw, None, interpretations)
  }
}
