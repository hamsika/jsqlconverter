@ECHO OFF
java -Djava.util.logging.config.file=../logging.properties -cp "../lib/*" com.googlecode.jsqlconverter.frontend.cli.SQLConverterCLI %*