ImageJ2 is a new version of ImageJ seeking to strengthen both the software and
its community. Internally, it is a total redesign of ImageJ, but it is
backwards compatible with ImageJ 1.x via a "legacy layer" and features a user
interface closely modeled after the original.

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

For more details on the project, see the
[ImageJ2 web site](http://developer.imagej.net/).


LICENSING
---------

ImageJ2 is distributed under a
[Simplified BSD License](http://en.wikipedia.org/wiki/BSD_licenses);
for the full text of the license, see
[LICENSE.txt](https://github.com/imagej/imagej/blob/master/LICENSE.txt).

For the list of developers and contributors, see
[pom.xml](https://github.com/imagej/imagej/blob/master/pom.xml).


BUGS
----

For a list of known issues, see the
[issue tracking system](http://trac.imagej.net/report/1).

Please report any bugs by following the
[instructions online](http://developer.imagej.net/reporting-bugs).


OTHER NOTES
-----------

Menus: IJ2 is currently made up of both IJ1 & IJ2 commands. IJ1 commands are
distinguished with a small microscope icon next to their name in the menus,
while IJ2 commands have either a green puzzle piece or a custom icon. Many IJ1
commands work, but some do not, for various reasons. We are still working to
improve the number of working IJ1 commands, and/or update them to pure IJ2
commands.

Macros and scripts: It is possible to execute IJ1 macros in IJ2, though you may
experience mixed results. You can even include calls to overridden IJ2 commands
but they will not record correctly.
