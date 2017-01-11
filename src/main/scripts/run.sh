#! /bin/sh

# Your java runtime
if [ -z "${JAVA_HOME}" ]
then
	JAVA="/usr/bin/java"
else
	JAVA="$JAVA_HOME/bin/java"
fi

# Change this line and make the right amount of memory for Java if you have a "Java Heap Space" error (big ontologies)
JAVA="$JAVA -Xmx2048m"

$JAVA -jar onagui*.jar
