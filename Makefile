JAVAS=$(wildcard ij/*.java ij/*/*.java ij/*/*/*.java)
CLASSES=$(patsubst %.java,%.class,$(JAVAS))
ALLCLASSES=ij/*.class ij/*/*.class ij/*/*/*.class
COPYFILES=icon.gif aboutja.jpg plugins/*.class
TEXTFILES=IJ_Props.txt $(wildcard macros/*.txt)

ifeq ($(JAVA_HOME),)
	JAVA_HOME=$(shell ../fiji --print-java-home)/..
endif

ifeq ($(shell javac > /dev/null 2>&1; echo $$?),127)
	PATH:=$(PATH):$(JAVA_HOME)/bin
endif

uname_O := $(shell sh -c 'uname -o 2>/dev/null || echo not')
ifeq ($(uname_O),Cygwin)
PLUGINSHOME=$(shell cygpath --mixed "$(shell pwd)")
CPSEP=\;
TOOLSCP=$(shell cygpath --mixed "$(JAVA_HOME)")/lib/tools.jar
else
PLUGINSHOME=$(shell pwd)
CPSEP=:
TOOLSCP=$(JAVA_HOME)/lib/tools.jar
endif
CLASSPATH=../jars/javac.jar$(CPSEP)$(TOOLSCP)$(CPSEP)$(PLUGINSHOME)/../ImageJ/ij.jar$(CPSEP)$(PLUGINSHOME)/jzlib-1.0.7.jar$(CPSEP).
JAVACOPTS=-O -classpath "$(CLASSPATH)" -source 1.3 -target 1.3

ij.jar: $(COPYFILES) $(CLASSES) $(TEXTFILES)
	jar cvmf MANIFEST.MF ij.jar $(COPYFILES) $(ALLCLASSES) $(TEXTFILES)

signed-ij.jar: ij.jar
	jarsigner -signedjar signed-ij.jar $(shell cat .jarsignerrc) ij.jar dscho

icon.gif aboutja.jpg: %: images/%
	cp $< $@

%.class: %.java
	javac $(JAVACOPTS) $(JAVAS)

clean:
	rm -f $(COPYFILES) $(ALLCLASSES)

