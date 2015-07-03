package de.jfschaefer.corpusviewer

import java.io.FileInputStream
import java.util.Properties

import de.jfschaefer.corpusviewer.preview.{PolynomialScalingFunction, AbstractPreviewScalingFunction, ConstantScalingFunction}

object Configuration {
  val (
    // display settings
    displayScreen : Int,
    gestureConfigProperties: String,

    // layout settings
    windowMargin: Int,
    initialScale: Double,

    sliderWidth: Int,
    sliderThumbHeight: Int,
    previewMargin: Double,
    previewScale: Double,
    preferredPreviewWidth: Double,
    previewSectionWidth: Double,

    textrootIrWidth: Double,
    textrootMargin: Double,
    textrootInterpretationDragoutDistance: Double,

    stringvisualizationPadding: Double,
    stringvisualizationWidth: Double,
    // style settings
    stylesheet: String
  ) = try {
    val properties = new Properties()
    properties.load(new FileInputStream("corpusviewer.properties"))
    (
      // display settings
      properties getProperty "display_screen" toInt,
      properties getProperty "gesture_config_properties",
      // layout settings
      properties getProperty "window_margin" toInt,
      properties getProperty "initial_scale" toDouble,

      properties getProperty "slider_width" toInt,
      properties getProperty "slider_thumb_height" toInt,
      properties getProperty "preview_margin" toDouble,
      properties getProperty "preview_scale" toDouble,
      properties getProperty "preferred_preview_width" toDouble,
      properties getProperty "preview_section_width" toDouble,
      properties getProperty "textroot_ir_width" toDouble,
      properties getProperty "textroot_margin" toDouble,
      properties getProperty "textroot_interpretation_dragout_distance" toDouble,

      properties getProperty "stringvisualization_padding" toDouble,
      properties getProperty "stringvisualization_width" toDouble,
      //style settings
      properties getProperty "stylesheet"
      )
  } catch {
    case exc: Exception =>
      exc.printStackTrace()
      sys.exit(1)
  }

  val visualizationFactory : AbstractVisualizationFactory = new ConcreteVisualizationFactory
  val previewScaling: AbstractPreviewScalingFunction = new PolynomialScalingFunction
  //val previewScaling: AbstractPreviewScalingFunction = new ConstantScalingFunction
}
