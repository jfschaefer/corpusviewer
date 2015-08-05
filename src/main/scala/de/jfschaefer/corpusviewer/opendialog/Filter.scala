package de.jfschaefer.corpusviewer.opendialog


import de.up.ling.irtg.corpus.Instance
import org.python.core.PyInteger
import org.python.util.PythonInterpreter

object Filter {
  def createFromJython(s: String): FilterResult = {
    try {
      val interpreter = new PythonInterpreter()
      interpreter.exec(s)

      FilterOk(new FilterTrait {
        override def assess(instance: Instance): Boolean = {
          interpreter.set("variablenamethatwillnotbeusedinthefilterrules_instance", instance)
          interpreter.set("variablenamethatwillnotbeusedinthefilterrules_interpretations", instance.getInputObjects)
          interpreter.eval("filter(variablenamethatwillnotbeusedinthefilterrules_instance," +
            "variablenamethatwillnotbeusedinthefilterrules_interpretations)").asInstanceOf[PyInteger].asInt() != 0
        }
      })
    } catch {
      case e : Throwable => e.printStackTrace(); FilterErr(e.getMessage)
    }
  }
}

abstract class FilterResult

case class FilterOk(filter: FilterTrait) extends FilterResult

case class FilterErr(error: String) extends FilterResult


trait FilterTrait {
  def assess(instance: Instance): Boolean
}
