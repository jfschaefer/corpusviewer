package de.jfschaefer.corpusviewer

import java.io.FileInputStream
import java.util.Properties

import de.jfschaefer.corpusviewer.preview.{PolynomialScalingFunction, AbstractPreviewScalingFunction, ConstantScalingFunction}

object Configuration {
  var properties = new Properties()
  try {
    properties.load(new FileInputStream("corpusviewer.properties"))
  } catch {
    case exc: Exception =>
      System.err.println("de.jfschaefer.corpusviewer.Configuration: Couldn't load corpusviewer.properties")
      exc.printStackTrace()
      sys.exit(1)
  }

  val displayScreen = load("display_screen", _.toInt)
  val gestureConfigProperties = load[String]("gesture_config_properties", {x: String => x})
  val fullscreen = load("fullscreen", _.toBoolean)

  val windowMargin = load("window_margin", _.toInt)
  val initialScale = load("initial_scale", _.toDouble)

  val trashWidth = load("trash_width", _.toDouble)
  val trashHeight = load("trash_height", _.toDouble)

  val sliderWidth = load("slider_width", _.toInt)
  val sliderThumbHeight = load("slider_thumb_height", _.toInt)

  val previewMargin = load("preview_margin", _.toDouble)
  val previewScale = load("preview_scale", _.toDouble)
  val preferredPreviewWidth = load("preferred_preview_width", _.toDouble)
  val previewSectionWidth = load("preview_section_width", _.toDouble)

  val textrootIrWidth = load("textroot_ir_width", _.toDouble)
  val textrootMargin = load("textroot_margin", _.toDouble)
  val textrootInterpretationDragoutDistance = load("textroot_interpretation_dragout_distance", _.toDouble)

  val stringvisualizationPadding = load("stringvisualization_padding", _.toDouble)
  val stringvisualizationWidth = load("stringvisualization_width", _.toDouble)

  val graphvisualizationPadding = load("graphvisualization_padding", _.toDouble)
  val graphvisualizationNodePadding = load("graphvisualization_node_padding", _.toDouble)

  val stylesheet = load[String]("stylesheet", {x: String => x})
  val numberOfIds = load("number_of_ids", _.toInt)

  val previewIsTrashZone = load("preview_is_trash_zone", _.toBoolean)


  def load[E](name: String, conversion: String => E): E =
     try {
       conversion(properties.getProperty(name))
     } catch {
       case exc: Exception =>
         System.err.println("de.jfschaefer.corpusviewer.Configuration: Couldn't load property " + name + " in corpusviewer.properties")
         exc.printStackTrace()
         sys.exit(1)
     }

  val visualizationFactory : AbstractVisualizationFactory = new ConcreteVisualizationFactory
  val previewScaling: AbstractPreviewScalingFunction = new PolynomialScalingFunction
  //val previewScaling: AbstractPreviewScalingFunction = new ConstantScalingFunction
}
