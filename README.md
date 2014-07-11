ImageJ2 is a new version of [ImageJ](http://imagej.net/) seeking to strengthen
both the software and its community. Internally, it is a total redesign of
ImageJ, but it is backwards compatible with ImageJ 1.x via a "legacy layer" and
features a user interface closely modeled after the original.

Under the hood, ImageJ2 completely isolates the image processing logic from the
graphical user interface (UI), allowing ImageJ2 commands to be used in many
contexts, including headless in the cloud or on a server such as
[OMERO](http://openmicroscopy.org/site/support/omero4), or from within another
application such as [KNIME](http://knime.org/),
[Icy](http://icy.bioimageanalysis.org/) or
[CellProfiler](http://cellprofiler.org/) (a Python application).

ImageJ2 has an N-dimensional data model driven by the powerful
[ImgLib2](http://imglib2.net/) library, which supports image data expressed in
an extensible set of numeric and non-numeric types, and accessed from an
extensible set of data sources. ImageJ2 is driven by a state-of-the-art,
collaborative development process, including version control, unit testing,
automated builds via a continuous integration system, a bug tracker and more.

We are collaborating closely with related projects including
[Fiji](http://fiji.sc/), [SCIFIO](http://scif.io/) and
[OME](http://openmicroscopy.org/), and are striving to deliver a coherent
software stack reusable throughout the life sciences community and beyond. For
more details, see the [SciJava web site](http://scijava.org/).

ImageJ2 is currently in the "beta" stage, meaning the code is not finished. It
is being released for early community feedback and testing. Comments, questions
and bug reports are much appreciated!

To maintain ImageJ's continuity of development, we have modeled the application
after ImageJ v1.x as much as is reasonable. However, please be aware that
ImageJ2 is essentially a total rewrite of ImageJ from the ground up. It
provides backward compatibility with older versions of ImageJ by bundling the
latest v1.x code and translating between "legacy" and "modern" image
structures.

For more details on the project, see the [ImageJ web site](http://imagej.net/).


LICENSING
---------

ImageJ2 is distributed under a
[Simplified BSD License](http://en.wikipedia.org/wiki/BSD_licenses);
for the full text of the license, see
[LICENSE.txt](https://github.com/imagej/imagej/blob/master/LICENSE.txt).

For the list of developers and contributors, see
[pom.xml](https://github.com/imagej/imagej/blob/master/pom.xml).


IMAGEJ AS A LIBRARY
-------------------

This repository is the master ImageJ application, which brings together all of
ImageJ under the artifact
[net.imagej:imagej](http://maven.imagej.net/index.html#nexus-search;gav~net.imagej~imagej~~~~kw,versionexpand).
It is the easiest entry point if you are looking to use ImageJ as a library from
your own software. E.g., in your Maven `pom.xml`:

```
<parent>
  <groupId>net.imagej</groupId>
  <artifactId>pom-imagej</artifactId>
  <version>2.35</version>
</parent>
...
<dependency>
  <groupId>net.imagej</groupId>
  <artifactId>imagej</artifactId>
</dependency>
```

We recommend inheriting from the
[pom-imagej](https://github.com/imagej/pom-imagej) parent, although it is not
required. (If you do not, you will need to include the `<version>` of ImageJ in
your `<dependency>` declaration.)


DEPENDENCIES
------------

This component depends on other, lower level components, each of which lives in
its own repository:

* [ImageJ Common](https://github.com/imagej/imagej-common)
* [ImageJ Legacy](https://github.com/imagej/imagej-legacy)
* [ImageJ OPS](https://github.com/imagej/imagej-ops)
* [ImageJ Updater](https://github.com/imagej/imagej-updater)
* [ImgLib2](https://github.com/imglib/imglib)
* [SCIFIO](https://github.com/scifio/scifio)
* [SciJava Common](https://github.com/scijava/scijava-common)

It also includes uses various "plugin" components at runtime:

* [Imagej Plugins: Commands](https://github.com/imagej/imagej-plugins-commands)
* [Imagej Plugins: Tools](https://github.com/imagej/imagej-plugins-tools)
* [Imagej Plugins: Uploader: SSH](https://github.com/imagej/imagej-plugins-uploader-ssh)
* [Imagej Plugins: Uploader: WebDAV](https://github.com/imagej/imagej-plugins-uploader-webdav)
* [SciJava Plugins: Platforms](https://github.com/scijava/scijava-plugins-platforms)
* [SciJava Plugins: Text: Markdown](https://github.com/scijava/scijava-plugins-text-markdown)
* [SciJava Plugins: Text: Plain](https://github.com/scijava/scijava-plugins-text-plain)
* [Scripting: Beanshell](https://github.com/scijava/scripting-beanshell)
* [Scripting: Clojure](https://github.com/scijava/scripting-clojure)
* [Scripting: Java](https://github.com/scijava/scripting-java)
* [Scripting: JavaScript](https://github.com/scijava/scripting-javascript)
* [Scripting: JRuby](https://github.com/scijava/scripting-jruby)
* [Scripting: Jython](https://github.com/scijava/scripting-jython)

See the [pom.xml](pom.xml) for a complete list of dependencies.


BUGS
----

For a list of known issues, see the
[issue tracking system](http://trac.imagej.net/report/1).

Please report any bugs by following the [instructions
online](http://imagej.net/Bugs).
