## Project Aims ##
This project aims to create a simple application and framework for converting between various formats that can be represented as database objects. This is done through the concept of parsers and producers.

### Parser ###
Parsers take a source and convert it into intermediate Java objects. Anything that can be represented as a database object (Table, Data, Index, etc) can be implemented as a parser.

For example a CSV file is the equivalent of a table and data.

The following parsers are available:

  * JDBC / ODBC Connection
  * Delimited file (CSV, TAB, etc)
  * MS Access 2000-2007 MDB (Via [Jackcess](http://jackcess.sourceforge.net))
  * XML Format
    * [Apache Turbine](http://turbine.apache.org)
    * [SQL Fairy](http://sqlfairy.sourceforge.net)
    * MySQL Workbench
    * DB Designer
    * LiquiBase
  * Generator (Generates random schemas)

### Producer ###
Producers take the structured Java objects and convert them into the target format (CSV, XML, SQL, etc)

The following producers are available:

  * SQL (Access, MySQL, Oracle, PostgreSQL, SQL Server)
  * Access 2000-2007 MDB
  * Entity Relationship Diagram ([DOT language](http://en.wikipedia.org/wiki/DOT_language))
    * [GraphViz](http://www.graphviz.org), [ZGRViewer](http://zvtm.sourceforge.net/zgrviewer.html)
  * XML Format
    * Apache Turbine
    * Hibernate
    * SQL Fairy
  * XHTML