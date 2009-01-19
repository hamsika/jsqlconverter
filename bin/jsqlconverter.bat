@ECHO OFF
java -Djava.util.logging.config.file=../logging.properties -cp "%CLASSPATH%;../lib/*" com.googlecode.jsqlconverter.frontend.cli.SQLConverterCLI %*