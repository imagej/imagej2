# Welcome to ImageJ2!

[ImageJ2](https://imagej.net/software/imagej2) is an effort to broaden
ImageJ's functionality beyond the limitations of the original ImageJ
application, to better support multidimensional scientific imaging.

To ensure backwards compatibility, we designed ImageJ2 to fully integrate into
the existing ImageJ user interface. This allows you to keep using ImageJ in
familiar ways, while providing the ability to migrate toward more powerful new
features as needed.

If you use the [Fiji](https://imagej.net/software/fiji) distribution of ImageJ
and ImageJ2, you may already be familiar with some of ImageJ2's features, such
as the [Updater](https://imagej.net/plugins/updater),
[Launcher](https://imagej.net/learn/launcher), and
[Script Editor](https://imagej.net/scripting/script-editor), which were
originally developed for Fiji.

## Features of ImageJ2

ImageJ2 provides a wealth of new features and capabilities:

* The ImageJ Updater makes it simple to stay up to date, and to add new plugins
  by enabling additional [Update Sites](https://imagej.net/update-sites).
* New and enhanced file format support via the
  [SCIFIO library](https://imagej.net/libs/scifio) (see below).
* More powerful [Script Editor](https://imagej.net/scripting/script-editor)
  with support for several scripting languages.
* New commands:
    * `Plugins > Debug > Dump Stack` for debugging when things
      [hang](https://en.wikipedia.org/wiki/Hang_(computing)).
    * `Plugins > Debug > System Information` for reporting on versions of
      installed plugins and libraries.
* Use ImageJ2's N-dimensional [ImgLib2](https://imagej.net/libs/imglib2)-based
  data structures (still in beta).
* Write [parameterized commands and scripts](https://imagej.net/scripting/parameters):
    * Typed inputs and outputs with no dependence on AWT user interface.
    * Mix and match original ImageJ and ImageJ2 data structures.
    * Plugins appear in the menu automatically without plugins.config files.
    * Reusable in many contexts: KNIME, CellProfiler, OMERO, headless...

## ImageJ2 is more than just an application

ImageJ2 is also a collection of reusable software libraries built on the
[SciJava](https://imagej.net/libs/scijava) software stack, using a powerful
plugin framework to facilitate rapid development and user customization.

The following software component libraries form the core of ImageJ2:

* [ImageJ Common](https://github.com/imagej/imagej-common) -
  The core image data model, using ImgLib2.
* [ImageJ Ops](https://github.com/imagej/imagej-ops) -
  An extensible framework for reusable image processing algorithms.
* [ImageJ Updater](https://github.com/imagej/imagej-updater) -
  A mechanism to update individual plugins and libraries within ImageJ2.
* [ImageJ Legacy](https://github.com/imagej/imagej-legacy) -
  Provides complete backwards compatibility with the original ImageJ.
* [SciJava Common](https://github.com/scijava/scijava-common) -
  The core frameworks for plugins, modules and the application itself.

## Improved image I/O with the SCIFIO library

ImageJ2 uses the [SCIFIO](https://imagej.net/libs/scifio) library
(**SC**ientific **I**mage **F**ormat **I**nput and **O**utput) by default
for most image input tasks. You can change this behavior at any time by
running `Edit > Options > ImageJ2` and modifying the
`Use SCIFIO when opening files` option.

### Benefits of using SCIFIO

SCIFIO is focused on robust and extensible support for reading and writing
image file formats. Using it with ImageJ provides many advantages:

* There is no need to call a special SCIFIO plugin; it works with commands like
  `File > Open` automatically.
* There are additional import options available via the
  `File > Import > Image...` command.
* There is a [Bio-Formats](https://imagej.net/formats/bio-formats)
  plugin for SCIFIO, included with the [Fiji](https://imagej.net/software/fiji)
  distribution of ImageJ, that adds automatic support for over a hundred life
  sciences file formats.
* Additional SCIFIO file format plugins can be dropped into ImageJ2 and will
  also work automatically.
* Unlike the original ImageJ's TIFF implementation, SCIFIO's support for TIFF
  adheres to the specification, allowing to successfully read many more sorts
  of TIFFs.
* Similarly, SCIFIO supports more sorts of JPEG files since it uses its own
  JPEG decoder.
* SCIFIO also ships with support for several QuickTime codecs, allowing reading
  of QuickTime MOV files even in 64-bit mode without QuickTime for Java.
* SCIFIO supports many additional open file formats out of the box:
    * animated GIF
    * animated PNG
    * encapsulated postscript (EPS)
    * JPEG-2000
    * Micro-Manager datasets
    * Multi-image Network Graphics (MNG)
    * Nearly Raw Raster Data (NRRD)
    * Imspector OBF
    * OME-TIFF (multidimensional rich metadata TIFF)
    * OME-XML
    * PCX
    * PICT (even in 64-bit mode and/or without QuickTime for Java installed)
* If SCIFIO cannot handle the image file, it falls back to the original
  ImageJ's I/O logic.
* You can save to SCIFIO-supported file formats using the
  `File > Export > Image...` command. Supported formats for export include:
    * APNG
    * AVI
    * EPS
    * ICS
    * JPEG
    * JPEG2000
    * QuickTime
    * TIFF

### Current limitations of SCIFIO

* SCIFIO is still in beta, so there is likely to be a higher incidence of bugs.
  Issues can be reported on the
  [SCIFIO issue tracker](https://github.com/scifio/scifio/issues).
* Although we strive for full backwards compatibility, some files may appear
  slightly different when opened.
* Opening files with SCIFIO is not fully macro recordable yet.
