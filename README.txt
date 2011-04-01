Thanks for trying ImageJ 2.0.0-alpha1!

This is an "alpha"-quality release, meaning the code is not finished, nor is
the design fully stabilized. We are releasing it for early community feedback,
and to demonstrate project directions and progress.

DO NOT USE THIS RELEASE FOR ANYTHING IMPORTANT.

For this release, we have tried to model the application after ImageJ v1.x as
much as is reasonable. However, please be aware that version 2.0.0 is
essentially a total rewrite of ImageJ from the ground up. It provides backward
compatibility with older versions of ImageJ by bundling the latest v1.x code
and translating between "legacy" and "modern" image structures.

For more details on the project, see the ImageJDev web site at:
  http://imagejdev.org/


LICENSING
---------

While our intention is to license ImageJ under a BSD-style license (see
LICENSE.txt), it is currently distributed with GPL-licensed software, meaning
this alpha version is technically also provided under terms of the GPL.


KNOWN ISSUES
------------

There are many known issues with this release:

1) The image displays are still very simple. Color displays and LUTs are
   not yet supported:
     * All images display in grayscale.
     * RGB color images are separated into images with 3 channels.
     * Scaling is limited to 0-255; e.g., 16-bit images may appear black.

2) The memory allocated to ImageJ is fixed at 512 MB.

3) Thanks to ImgLib (http://imagejdev.org/imglib), there are many new pixel
   types. You can generate images with these types using the Process > Gradient
   command, but some types (e.g., uint1 and uint12) are not yet working.

4) Many more bugs and limitations not listed here.


TOOLBAR
-------

Most toolbar tools are not yet implemented, and hence grayed out.
The following tools are working:

  * Pan (hand icon)
  * Zoom (magnifying glass icon)
  * Probe (crosshairs icon)


MENUS
-----

IJ2 is currently made up of both IJ1 & IJ2 plugins. IJ1 plugins are
distinguished with a small microscope icon next to their name in the menus.
Commands implemented purely in IJ2 do not have this marker.

The following menu commands are working. Unlisted commands do not work.

  * File menu:
    - File > New
    - File > Open
    - File > Open Samples
    - File > Quit

  * Edit menu:
    - Edit > Undo
      + works for ImageJ 1.x commands
    - Edit > Fill
    - Edit > Invert
    - Edit > Options
      + option values can be set but their values are ignored internally

  * Image menu:
    - Image > Duplicate
    - Image > Stacks > Make Montage
    - Image > Stacks > Reslice
    - Image > Stacks > Z Project
    - Image > Stacks > 3d Project
    - Image > Stacks > Plot Z Axis Profile
    - Image > Transform > Flip Horizontally
    - Image > Transform > Flip Vertically
    - Image > Transform > Rotate 90 Degrees Left
      + sometimes window shape is not updated
    - Image > Transform > Rotate 90 Degrees Right
      + sometimes window shape is not updated
    - Image > Transform > Rotate
    - Image > Transform > Translate

  * Process menu:
    - Process > Smooth
    - Process > Sharpen
    - Process > Find Edges
    - Process > Noise > Add Noise
    - Process > Noise > Add Specific Noise
    - Process > Noise > Salt and Pepper
    - Process > Shadows
      + all commands except Shadows Demo
    - Process > Binary
      + all commands except:
        * Convert to Mask
        * Ultimate Points
    - Process > Math
    - Process > FFT > FFT
    - Process > Filters
      + all commands
    - Process > Image Calculator
    - Process > Gradient

  * Analyze menu:
    - Analyze > Measure
    - Analyze > Analyze Particles
    - Analyze > Summarize
    - Analyze > Distribution
    - Analyze > Clear Results
    - Analyze > Histogram
    - Analyze > Surface Plot
    - Analyze > Tools > Save XY Coordinates
    - Analyze > Tools > Fractal Box Count
    - Analyze > Tools > Curve Fitting
    - Analyze > Tools > Scale Bar
    - Analyze > Tools > Calibration Bar

  * Plugins menu:
    - Plugins > Macros > Run
    - Plugins > Macros > Edit
    - Plugins > Shortcuts > List Shortcuts
    - Plugins > Utilities > Control Panel
    - Plugins > Utilities > ImageJ Properties
    - Plugins > Utilities > Threads
    - Plugins > Utilities > Capture Image
    - Plugins > Utilities > Capture Screen


MACROS AND SCRIPTS
------------------

It is possible to execute IJ1 macros in IJ2, though you will likely experience
mixed results. You can even include calls to overridden IJ2 commands but they
will not record correctly.
