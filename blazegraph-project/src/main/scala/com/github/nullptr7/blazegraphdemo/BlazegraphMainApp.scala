package com.github.nullptr7.blazegraphdemo

import java.util.Properties
import org.openrdf.OpenRDFException
import org.openrdf.model.Literal
import org.openrdf.model.Statement
import org.openrdf.model.impl.LiteralImpl
import org.openrdf.model.impl.StatementImpl
import org.openrdf.model.impl.URIImpl
import org.openrdf.query.BindingSet
import org.openrdf.query.QueryLanguage
import org.openrdf.query.TupleQuery
import org.openrdf.query.TupleQueryResult
import org.openrdf.repository.Repository
import org.openrdf.repository.RepositoryConnection
import com.bigdata.journal.Options
import com.bigdata.rdf.sail.BigdataSail
import com.bigdata.rdf.sail.BigdataSailRepository

object BlazegraphMainApp extends App {
  val props = new Properties
  props.put(Options.BUFFER_MODE, "DiskRW") // persistent file system located journal

  props.put(Options.FILE, "D:\\softwares\\blazegraph\\database.jnl") // journal file location

  val sail = new BigdataSail(props) // instantiate a sail

  val repo = new BigdataSailRepository(sail) // create a Sesame repository

  val subject   = new URIImpl("http://blazegraph.com/Blazegraph")
  val predicate = new URIImpl("http://blazegraph.com/says")
  val `object`  = new LiteralImpl("hello")
  val stmt      = new StatementImpl(subject, predicate, `object`)

  // open repository connection
  val cxn = repo.getConnection

  try {
    cxn.begin
    cxn.add(stmt)
    cxn.commit
  }
  catch {
    case ex: OpenRDFException =>
      cxn.rollback
      throw ex
  }
  finally
    cxn.close

  // open connection
//  if (repo.isInstanceOf[BigdataSailRepository]) cxn = repo.asInstanceOf[BigdataSailRepository].getReadOnlyConnection
//  else cxn                                          = repo.getConnection



  println("Hello Blazegraph!")

}
