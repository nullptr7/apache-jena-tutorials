package com.github.nullptr7.jenademo

import org.apache.jena.query.{ Dataset, ReadWrite }
import org.apache.jena.rdf.model.*
import org.apache.jena.tdb2.TDB2Factory

import java.nio.file.{ Files, Paths }
import scala.io.Source
import scala.util.Using

object InsertionDemo extends App {

  // ---- Hardcoded config (adjust as needed) ----
  private val STORE_DIR   = "D:\\softwares\\apache-jena-fuseki-5.6.0\\db" // Fuseki --loc=db folder
  private val GRAPH_IRI   = "http://example.org/graph/addressbook"
  private val SUBJECT_IRI = "http://learningsparql.com/ns/addressBook#jaz"
  private val TEMP_FILE   = "temp_statements.txt"

  // File format (per line; blank lines and '#' comments ignored):
  //   <predicate-IRI>\t"literal value"
  //   <predicate-IRI>\t<object-IRI>
  //   <predicate-IRI>\tbare literal
  //
  // Example:
  //   <http://learningsparql.com/ns/addressBook#email>    "gaz@in.com"
  //   <http://learningsparql.com/ns/addressBook#homeTel>  "(229) 222-2222"

  // ---------- parsing helpers ----------
  private def isIRI(s: String): Boolean =
    s.startsWith("<") && s.endsWith(">") && s.length > 2

  private def stripAngles(s: String): String =
    if isIRI(s) then s.substring(1, s.length - 1) else s

  private def isQuoted(s: String): Boolean =
    s.startsWith("\"") && s.endsWith("\"") && s.length >= 2

  private def stripQuotes(s: String): String =
    if isQuoted(s) then s.substring(1, s.length - 1) else s

  /** Parse one line into an optional Statement (bound to SUBJECT_IRI). */
  private def parseLineToStatement(line: String, subject: Resource): Option[Statement] =
    val trimmed = line.trim
    if trimmed.isEmpty || trimmed.startsWith("#") then return None

    val parts = trimmed.split("@-@", 2)
    if parts.length != 2 then
      System.err.println(s"Ignored (missing TAB): $line")
      return None

    val predRaw = parts(0).trim
    val objRaw  = parts(1).trim

    if !isIRI(predRaw) then
      System.err.println(s"Ignored (predicate must be <IRI>): $line")
      return None

    val pIri: String   = stripAngles(predRaw)
    val p:    Property = ResourceFactory.createProperty(pIri)
    val o:    RDFNode  =
      if isIRI(objRaw) then ResourceFactory.createResource(stripAngles(objRaw))
      else ResourceFactory.createStringLiteral(stripQuotes(objRaw))

    Some(ResourceFactory.createStatement(subject, p, o))

  /** Read all statements from file for a fixed subject. */
  private def readStatementsFromFile(file: String, subjectIri: String): List[Statement] =
    val subject: Resource = ResourceFactory.createResource(subjectIri)
    val buf = scala.collection.mutable.ListBuffer.empty[Statement]
    Using.resource(Source.fromFile(file, "UTF-8")) { src =>
      for (raw <- src.getLines())
        parseLineToStatement(raw, subject) match
          case Some(stmt) => buf += stmt
          case None       => ()
    }
    buf.toList

  /** Insert a list of statements into a named graph in one write transaction. */
  private def insertStatements(graphIri: String, statements: List[Statement]): Int =
    if statements.isEmpty then return 0

    // Files.createDirectories(Paths.get(STORE_DIR))
    val dataset: Dataset = TDB2Factory.connectDataset(STORE_DIR)

    dataset.begin(ReadWrite.WRITE)
    try
      val model: Model = dataset.getNamedModel(graphIri)
      // Model.add has an overload for Array[Statement]
      model.add(statements.toArray)
      val count = statements.size
      dataset.commit()
      count
    catch
      case t: Throwable =>
        dataset.abort()
        throw t
    finally
      dataset.end()
      // Optional: compact storage after heavy ingest
//      try dataset.asDatasetGraph().getStorage().compact()
//      catch case _: Throwable => ()
      dataset.close()

  // -------------- entrypoint --------------
  private def run(): Unit =
    val stmts: List[Statement] = readStatementsFromFile(TEMP_FILE, SUBJECT_IRI)
    val n:     Int             = insertStatements(GRAPH_IRI, stmts)
    println(s"Inserted $n statements into graph <$GRAPH_IRI> at $STORE_DIR")

  run()

}
