package de.jfschaefer.corpusviewer

import java.io.FileInputStream
import java.util.Properties

import scalafx.scene.image.Image

import de.jfschaefer.corpusviewer.preview.AbstractPreviewScalingFunction

/** Contains a lot of settings, most of which are defined in the file corpusviewer.properties */

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

  // display settings
  val displayScreen = load("display_screen", _.toInt)
  val gestureConfigProperties = load("gesture_config_properties", identity)
  val fullscreen = load("fullscreen", _.toBoolean)

  // layout settings
  val windowMargin = load("window_margin", _.toDouble)
  val initialScale = load("initial_scale", _.toDouble)

  val openCorpusCols = load("open_corpus_cols", _.toInt)
  val openCorpusMargin = load("open_corpus_margin", _.toDouble)
  val openCorpusMaxEntryHeight = load("open_corpus_max_entry_height", _.toDouble)
  val openCorpusMaxEntriesPerCol = load("open_corpus_max_entries_per_col", _.toInt)

  val trashWidth = load("trash_width", _.toDouble)
  val trashHeight = load("trash_height", _.toDouble)

  val sliderWidth = load("slider_width", _.toDouble)
  val sliderThumbHeight = load("slider_thumb_height", _.toDouble)

  val previewMargin = load("preview_margin", _.toDouble)
  val previewScale = load("preview_scale", _.toDouble)
  val preferredPreviewWidth = load("preferred_preview_width", _.toDouble)
  val previewSectionWidth = load("preview_section_width", _.toDouble)
  val previewMaxDownscale = load("preview_max_downscale", _.toDouble)

  val headerMargin = load("header_margin", _.toDouble)
  val headerMenuButtonRadius = load("header_menu_button_radius", _.toDouble)
  val radialMenuExpansionRadius = load("radial_menu_expansion_radius", _.toDouble)
  val radialMenuEntryRadius = load("radial_menu_entry_radius", _.toDouble)
  val radialMenuDragoutFactor = load("radial_menu_dragout_factor", _.toDouble)

  val stringvisualizationPadding = load("stringvisualization_padding", _.toDouble)
  val stringvisualizationWidth = load("stringvisualization_width", _.toDouble)

  val graphvisualizationCentralizeEdges = load("graphvisualization_centralize_edges", _.toBoolean)

  // style settings
  val stylesheet = load[String]("stylesheet", identity)
  val backgroundColor = load("background_color", identity)
  val graphColor = load("graph_color", identity)
  val numberOfIds = load("number_of_ids", _.toInt)
  val locationPolygonColor1 = load("location_polygon_color_1", identity)
  val locationPolygonColor2 = load("location_polygon_color_2", identity)

  // behaviour settings
  val previewIsTrashZone = load("preview_is_trash_zone", _.toBoolean)
  val previewScaling = Class.forName(load("preview_scaling_function", identity))
                                        .newInstance().asInstanceOf[AbstractPreviewScalingFunction]

  /** Loads a property from the properties file and converts it
    *
    * @param name the name of the property
    * @param conversion the conversion function
    * @tparam E the target type
    * @return the converted property
    */
  def load[E](name: String, conversion: String => E): E =
     try {
       conversion(properties.getProperty(name))
     } catch {
       case exc: Exception =>
         System.err.println("de.jfschaefer.corpusviewer.Configuration: Couldn't load property " +
                                                                                   name + " in corpusviewer.properties")
         exc.printStackTrace()
         sys.exit(1)
     }

  val visualizationFactory : AbstractVisualizationFactory = new ConcreteVisualizationFactory

  val radialMenuButtonImage = new Image("file://" + System.getProperty("user.dir") + "/icons/radialmenu.png")
}
