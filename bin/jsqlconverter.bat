@ECHO OFF
java -Djava.util.logging.config.file=../logging.properties -cp "../lib/jsqlconverter-lib.jar;../lib/jsqlconverter-cli.jar" com.googlecode.jsqlconverter.frontend.cli.SQLConverterCLI %*
