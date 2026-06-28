# Welcome to ImageJ2!

[ImageJ2](https://imagej.net/software/imagej2) extends ImageJ beyond
the limitations of the original application, with stronger support
for multidimensional scientific image data.

It is designed to integrate fully into the original ImageJ user interface, so
you can keep working in familiar ways and adopt any of ImageJ2's new features
whenever you find them useful.

If you are reading this in [Fiji](https://imagej.net/software/fiji), you are
already running ImageJ2. Some tools you may know from Fiji—such as the
[Updater](https://imagej.net/plugins/updater),
[Launcher](https://imagej.net/learn/launcher), and
[Script Editor](https://imagej.net/scripting/script-editor)—were first
developed for Fiji and are now part of ImageJ2.

## Key features of ImageJ2

* The [Updater](https://imagej.net/plugins/updater), for keeping
  your installation current and adding plugins via additional
  [update sites](https://imagej.net/update-sites).

* N-dimensional data structures based on the
  [ImgLib2](https://imagej.net/libs/imglib2) library.

* Extensible image file format support via the
  [SCIFIO](https://imagej.net/libs/scifio) library (see below).

* A capable [Script Editor](https://imagej.net/scripting/script-editor)
  supporting many scripting languages.

* [Parameterized commands and scripts](https://imagej.net/scripting/parameters)
  with typed inputs and outputs that:
    * have no dependence on the AWT user interface;
    * mix and match original ImageJ and ImageJ2 data structures;
    * appear in the menus automatically, with no `plugins.config` file; and
    * run in many contexts, including KNIME, CellProfiler, OMERO, and headless.

## ImageJ2 as a library suite

ImageJ2 is also a collection of reusable software libraries built on the
[SciJava](https://imagej.net/libs/scijava) software stack, with a plugin
framework that makes it straightforward to extend and customize. If you write
your own plugins or scripts, these are the components you are building on:

* [ImageJ Common](https://github.com/imagej/imagej-common) -
  the core image data model, built on ImgLib2.
* [ImageJ Ops](https://github.com/imagej/imagej-ops) -
  an extensible framework for reusable image processing algorithms.
* [ImageJ Updater](https://github.com/imagej/imagej-updater) -
  the mechanism for updating plugins and libraries.
* [ImageJ Legacy](https://github.com/imagej/imagej-legacy) -
  backwards compatibility with the original ImageJ.
* [SciJava Common](https://github.com/scijava/scijava-common) -
  the core frameworks for plugins, modules, and the application itself.

## Image I/O with the SCIFIO library

ImageJ2 ships with the [SCIFIO](https://imagej.net/libs/scifio) library
(**SC**ientific **I**mage **F**ormat **I**nput and **O**utput), which provides
extensible support for reading and writing image file formats.

SCIFIO is still in beta and is off by default. To try it, run
`Edit > Options > ImageJ2` and enable
`Use SCIFIO when opening files`. Once enabled, it applies automatically to
commands like `File > Open`, with no separate plugin to invoke.

### What SCIFIO offers

* Extra import and export options via `File > Import > Image...` and
  `File > Export > Image...` (usable regardless of the above setting).
* The [Bio-Formats](https://imagej.net/formats/bio-formats) library—included
  with Fiji—plugs into SCIFIO to add support for over a hundred life sciences
  file formats.
* New formats can be added simply by dropping additional SCIFIO plugins into
  your installation.
* Spec-compliant TIFF reading, which handles many TIFFs that the original
  ImageJ cannot.
* Built-in support for additional open formats, including animated GIF,
  animated PNG, JPEG-2000, Micro-Manager datasets, NRRD, OME-TIFF, and OME-XML.
* If SCIFIO cannot read a file, it falls back to the original ImageJ's I/O.

### Current limitations

* As beta software, SCIFIO may have a higher incidence of bugs. Please report
  problems on the [SCIFIO issue tracker](https://github.com/scifio/scifio/issues).
* Although we aim for full backwards compatibility, some files may appear
  slightly different when opened.

---

For more about ImageJ2, including tutorials and documentation,
visit the [ImageJ wiki](https://imagej.net/).
