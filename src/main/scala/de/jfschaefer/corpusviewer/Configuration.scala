package de.jfschaefer.corpusviewer

import java.io.FileInputStream
import java.util.Properties

object Configuration {
  val (
    // display settings
    displayScreen : Int,
    // layout settings
    sliderWidth: Int,
    sliderThumbHeight: Int,
    windowMargin: Int,
    // style settings
    stylesheet: String
  ) = try {
    val properties = new Properties()
    properties.load(new FileInputStream("corpusviewer.properties"))
    (
      // display settings
      properties getProperty "display_screen" toInt,
      // layout settings
      properties getProperty "slider_width" toInt,
      properties getProperty "slider_thumb_height" toInt,
      properties getProperty "window_margin" toInt,
      //style settings
      properties getProperty "stylesheet"
      )
  } catch {
    case exc: Exception =>
      exc.printStackTrace()
      sys.exit(1)
  }

  val visualizationFactory : AbstractVisualizationFactory = new ConcreteVisualizationFactory
}
