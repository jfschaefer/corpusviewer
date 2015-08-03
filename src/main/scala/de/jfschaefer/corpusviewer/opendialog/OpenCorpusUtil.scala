package de.jfschaefer.corpusviewer.opendialog

import de.up.ling.tclup.perf.alto.{CorpusFromDb, GrammarMetadata}

object OpenCorpusUtil {
  def stringFromMeta(grammarMetadata: GrammarMetadata): String = {
    val s = new StringBuilder
    s.append("name:\t")
    s.append(grammarMetadata.name)
    s.append("\nid:\t")
    s.append(grammarMetadata.id)
    s.append("\ncomment:\t")
    s.append(grammarMetadata.comment)
    s.append("\ninterpretations:")
    for ((k, v) <- grammarMetadata.interpretations) {
      s.append("\n    " )
      s.append(k)
      s.append(":   ")
      s.append(v)
    }
    s.toString()
  }

  def stringFromMeta(corpusMeta: CorpusFromDb#CorpusMetadata): String = {
    val s = new StringBuilder
    s.append("name:\t")
    s.append(corpusMeta.name)
    s.append("\nid:\t")
    s.append(corpusMeta.id)
    s.append("\ncomment:\t")
    s.append(corpusMeta.comments match {
      case Some(comment) => comment
      case None => "<no comment>"
    })
    s.append("\ninterpretations:")
    for (i <- corpusMeta.interpretations) {
      s.append("\n    " )
      s.append(i)
    }
    s.toString()
  }
}
