@ECHO OFF
java -Djava.util.logging.config.file=../logging.properties -cp "../lib/*;../plugins/*" com.googlecode.jsqlconverter.frontend.cli.SQLConverterCLI %*