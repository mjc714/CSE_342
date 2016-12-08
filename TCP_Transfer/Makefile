#define compiler and compiler flags
JFLAGS = -g
JC = javac

#clear any default targets for building
.SUFFIXES: .java .class

#target to create .class files from .java
.java.class:
	$(JC) $(JFLAGS) $*.java

#classes macro that contains .java files
CLASSES = TCPClient.java TCPServer.java

#default make target
default: classes

#suffix replacement for .class and .java
classes: $(CLASSES:.java=.class)

#clean up build
clean:
	$(RM) *.class