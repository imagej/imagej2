# Welcome to ImageJ2!

[ImageJ2](http://developer.imagej.net/about) is an effort to broaden the
functionality of ImageJ beyond the limitations of ImageJ 1.x, to support the
next generation of multidimensional scientific imaging.

To ensure backwards compatibility, we designed ImageJ2 to fully integrate into
the existing ImageJ user interface. This allows you to keep using ImageJ in
familiar ways, while providing the ability to migrate toward more powerful new
features as needed.

The [Fiji](http://fiji.sc/) distribution of ImageJ has shipped with beta
versions of ImageJ2 for quite some time, so you may already be familiar with
some of ImageJ2's features -- some of which, such as the
[Updater](http://wiki.imagej.net/Updater) and
[Launcher](http://wiki.imagej.net/Launcher), were originally developed as part
of Fiji.

## Features of ImageJ2

ImageJ2 provides a wealth of new features and capabilities:

* The ImageJ Updater makes it simple to keep your ImageJ up to date, and to
  add new plugins by enabling additional
  [Update Sites](http://wiki.imagej.net/Update_Sites).
* New and enhanced file format support via the SCIFIO library (see below).
* More powerful [Script Editor](http://wiki.imagej.net/Script_Editor) with
  support for several scripting languages.
* New commands:
    * `Plugins > Debug > Dump Stack` for debugging when things
      [hang](https://en.wikipedia.org/wiki/Hang_(computing)).
    * `Plugins > Debug > System Information` for reporting on versions of
      installed plugins and libraries.
* Use ImageJ2's N-dimensional [ImgLib2](http://wiki.imagej.net/ImgLib2)-based
  data structures (still in beta).
* Write parameterized commands and scripts:
    * Typed inputs and outputs with no dependence on AWT user interface.
    * Mix and match ImageJ 1.x and ImageJ2 data structures.
    * Plugins appear in the menu automatically without plugins.config files.
    * Reusable in many contexts: KNIME, CellProfiler, OMERO, headless...

## ImageJ2 is more than just an application

ImageJ2 is also a collection of reusable software libraries built on the
[SciJava](http://www.scijava.org/) software stack, using a powerful plugin
framework to facilitate rapid development and painless user customization.

The following software component libraries form the core of ImageJ2:

* [ImageJ Common](https://github.com/imagej/imagej-common) -
  The core image data model, using ImgLib2.
* [ImageJ OPS](https://github.com/imagej/imagej-ops) -
  An extensible framework for reusable image processing algorithms.
* [ImageJ Updater](https://github.com/imagej/imagej-updater) -
  A mechanism to update individual plugins and libraries within ImageJ.
* [ImageJ Legacy](https://github.com/imagej/imagej-legacy) -
  Provides complete backwards compatibility with ImageJ 1.x.
* [SciJava Common](https://github.com/scijava/scijava-common) -
  The core frameworks for plugins, modules and the application itself.

## Improved image I/O with the SCIFIO library

ImageJ2 uses the [SCIFIO](http://wiki.imagej.net/SCIFIO) library (SCientific
Image Format Input and Output) by default for most image input tasks. You can
change this behavior at any time by running `Edit > Options > ImageJ2` and
modifying the `Use SCIFIO when opening files` option.

### Benefits of using SCIFIO

SCIFIO is focused on robust and extensible support for reading and writing
image file formats. Using it with ImageJ provides many advantages:

* There is no need to call a special SCIFIO plugin; it works with commands like
  `File > Open` automatically.
* There are additional import options available via the `File > Import >
  Image...` command.
* There is a [Bio-Formats](http://fiji.sc/Bio-Formats) plugin for SCIFIO,
  included with the [Fiji](http://fiji.sc/) distribution of ImageJ, that adds
  automatic support for over a hundred life sciences file formats.
* Additional SCIFIO file format plugins can be dropped into ImageJ and will
  also work automatically.
* Unlike the ImageJ 1.x TIFF implementation, SCIFIO's support for TIFF adheres
  to the specification, allowing to successfully read many more sorts of TIFFs.
* Similarly, SCIFIO supports more sorts of JPEG files since it uses its own
  JPEG decoder.
* SCIFIO also ships with support for several QuickTime codecs, allowing ImageJ
  to read QuickTime MOV files even in 64-bit mode without QuickTime for Java.
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
* If SCIFIO cannot handle the image file, it falls back to ImageJ 1.x.
* You can save to SCIFIO-supported file formats using the `File > Export >
  Image...` command. Supported formats for export include:
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
