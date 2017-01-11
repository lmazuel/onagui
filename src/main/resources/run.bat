@echo off



:: java command line, modern Oracle allows to use juste "java"
set JAVA=java


:: Change this line and make the right amount of memory for Java if you have a "Java Heap Space" error (big ontologies)

set JAVA=%JAVA% -Xmx1024m


%JAVA% -jar OnAGUI.jar
