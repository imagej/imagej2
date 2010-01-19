JAVAVERSION=1.4
all <- ij.jar

MAINCLASS(ij.jar)=ij.ImageJ
ij.jar <- ij/**/*.java \
	icon.gif[images/icon.gif] aboutja.jpg[images/aboutja.jpg] plugins/*.java \
	plugins/JavaScriptEvaluator.class plugins/MacAdapter.class \
	IJ_Props.txt macros/*.txt

JARSIGNEROPTS=-signedjar signed-ij.jar ij.jar dscho
signed-ij.jar[jarsigner $JARSIGNEROPTS] <- ij.jar
