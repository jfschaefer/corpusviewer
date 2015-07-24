package de.jfschaefer.corpusviewer

import de.jfschaefer.corpusviewer.opendialog.OpenCorpusDialog

import de.up.ling.gesture.JavaFxAdapter

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.stage.{StageStyle, Screen}
import scalafx.Includes._




object Main extends JFXApp {
  val corpusScene = new Scene {
    stylesheets += Configuration.stylesheet
  }

  val openCorpusScene = new Scene {
    stylesheets += Configuration.stylesheet
  }

  private var corpus: Corpus = null

  stage = new JFXApp.PrimaryStage {
    title.value = "Corpus Viewer"
    //Set default size
    width = 800
    height = 600
    scene = openCorpusScene
  }

  val adapter = JavaFxAdapter.start(stage.delegate, Configuration.gestureConfigProperties)

  val numberOfScreens = Screen.screens.size
  if (numberOfScreens > Configuration.displayScreen) {
    if (Configuration.fullscreen) fullscreen(Configuration.displayScreen)
  } else {
    System.err.println("de.jfschaefer.corpusviewer.Main: Invalid configuration 'display_screen':")
    System.err.println("Got '" + Configuration.displayScreen + "' (have only " + numberOfScreens + " screens)")
    System.err.println("Moving to screen 0 instead")
    fullscreen(0)
  }

  openCorpusScene.root = new OpenCorpusDialog(openCorpus)

  def openCorpus(iterator: java.util.Iterator[de.up.ling.irtg.corpus.Instance]): Unit = {
    corpus = new Corpus(iterator)
    corpusScene.root = corpus
    stage.scene = corpusScene
    corpus.previewGroup.update()
  }

  def getCorpus:Corpus = corpus

  def fullscreen(displayId:Int): Unit = {
    val targetScreen = Screen.screens.get(displayId)
    val bounds = targetScreen.getVisualBounds

    stage.setX(bounds.getMinX)
    stage.setY(bounds.getMinY)
    stage.setWidth(bounds.getWidth)
    stage.setHeight(bounds.getHeight)

    stage.initStyle(StageStyle.UNDECORATED)
    stage.setFullScreen(true)
  }
}
