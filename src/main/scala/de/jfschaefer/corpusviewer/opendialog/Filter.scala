package de.jfschaefer.corpusviewer.opendialog


import java.io.OutputStream

import de.up.ling.irtg.corpus.Instance
import org.python.core.{PyException, PyInteger}
import org.python.util.PythonInterpreter

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

/** Provides functions for filtering */
object Filter {
  /** Creates a Filter using Jython
    *
    * @param s the filter code
    * @return the filter
    */
  def createFromJython(s: String): FilterResult = {
    try {
      FilterOk(new FilterTrait {
        val interpreter = new PythonInterpreter()
        val err = new OutputStream {
          val str = new StringBuilder
          override def write(b: Int): Unit = {
            str.append(b.toChar)
          }
          def tryAlert(): Boolean = {
            val isError = str.nonEmpty
            if (isError) {
              val alert = new Alert(AlertType.ERROR)
              alert.setHeaderText("Error in filter rule")
              alert.setContentText(str.toString())
              alert.showAndWait()
            }
            str.clear()
            isError
          }
        }
        interpreter.setErr(err)
        interpreter.exec(s)
        err.tryAlert()
        override def assess(instance: Instance): Boolean = {
          var result = false
          try {
            interpreter.set("variablenamethatwillnotbeusedinthefilterrules_instance", instance)
            interpreter.set("variablenamethatwillnotbeusedinthefilterrules_interpretations", instance.getInputObjects)
            result = interpreter.eval("filter(variablenamethatwillnotbeusedinthefilterrules_instance," +
              "variablenamethatwillnotbeusedinthefilterrules_interpretations)").asInstanceOf[PyInteger].asInt() != 0
          } catch {
            case e : PyException =>
              val alert = new Alert(AlertType.ERROR)
              alert.setHeaderText("PyException")
              alert.setContentText(e.toString)
              alert.showAndWait()
              throw new FilterException
          }
          err.tryAlert()
          result
        }
      })
    } catch {
      case e : Throwable => e.printStackTrace(); FilterErr(e.toString)
    }
  }
}

/** An exception in the filter */
class FilterException extends RuntimeException

/** The result of creating a filter (either success and a filter, or failure and an error message) */
abstract class FilterResult

/** A filter */
case class FilterOk(filter: FilterTrait) extends FilterResult

/** An error message */
case class FilterErr(error: String) extends FilterResult

/** A filter */
trait FilterTrait {
  /** The only thing a filter has to do: Assess instances
    *
    * @param instance the instance to be assessed
    * @return true, if the instance is accepted, otherwise false
    */
  def assess(instance: Instance): Boolean
}
