package de.jfschaefer.corpusviewer.opendialog


import java.io.OutputStream

import de.up.ling.irtg.corpus.Instance
import org.python.core.{PyException, PyInteger}
import org.python.util.PythonInterpreter

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

object Filter {
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
              val alert = new Alert(AlertType.Error)
              alert.setHeaderText("Error in filter rule")
              alert.setContentText(str.toString)
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
            case e : PyException => {
              val alert = new Alert(AlertType.Error)
              alert.setHeaderText("PyException")
              alert.setContentText(e.toString)
              alert.showAndWait()
              throw new FilterException
            }
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

class FilterException extends RuntimeException

abstract class FilterResult

case class FilterOk(filter: FilterTrait) extends FilterResult

case class FilterErr(error: String) extends FilterResult


trait FilterTrait {
  def assess(instance: Instance): Boolean
}
