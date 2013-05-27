#!/bin/sh

#
# eclipse-clue-bat.sh
#

# Use this script to add the files necessary to force Eclipse to use the sezpoz
# annotator (it would not be necessary if Eclipse's compiler would follow the
# rules set out by the Java specification).

for path in $(find . -name pom.xml)
do
	case $path in
	./envisaje/*|./sandbox/*)
		continue
		;;
	esac

	# skip aggregator modules
	grep -e '<modules>' $path > /dev/null && continue

	directory=${path%/pom.xml}
	factorypath=$directory/.factorypath
	test -f $factorypath || cat > $factorypath << EOF
<factorypath>
    <factorypathentry kind="VARJAR" id="M2_REPO/net/java/sezpoz/sezpoz/1.9-imagej/sezpoz-1.9-imagej.jar" enabled="true" runInBatchMode="true"/>
</factorypath>
EOF
	prefs=$directory/.settings/org.eclipse.jdt.core.prefs
	test -f $prefs || {
		mkdir -p $directory/.settings
		cat > $prefs << EOF
#$(date)
eclipse.preferences.version=1
org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6
org.eclipse.jdt.core.compiler.compliance=1.6
org.eclipse.jdt.core.compiler.problem.forbiddenReference=warning
org.eclipse.jdt.core.compiler.processAnnotations=enabled
org.eclipse.jdt.core.compiler.source=1.6
EOF
		echo "Do not forget to commit $prefs even if it is ignored by Git"
	}
	prefs2=$directory/.settings/org.eclipse.jdt.apt.core.prefs
	test -f $prefs2 || {
		cat > $prefs2 << EOF
#$(date)
eclipse.preferences.version=1
org.eclipse.jdt.apt.aptEnabled=true
org.eclipse.jdt.apt.genSrcDir=target/classes
org.eclipse.jdt.apt.reconcileEnabled=false
EOF
		echo "Do not forget to commit $prefs2 even if it is ignored by Git"
	}
done
