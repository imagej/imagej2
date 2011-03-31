Welcome to ImageJ 2.0 alpha 1

IJ2 is currently made up of both IJ1 & IJ2 plugins. IJ1 plugins are distinguished
with a small microscope icon next to their name in the menus. Commands implemented
purely in IJ2 do not have this marker.

There are a number of known issues with this release:

The memory allocated to ImageJ is fixed at 512 mb

Color is currently unsupported.
  * All images display in grayscale
  * Color images are separated into images with 3 channels

The following toolbar tools are working. Unlisted tools are not.

  * Pan (hand icon)
  * Zoom (magnifying glass icon)
  * Probe (crosshairs icon)

The Window menu is nonfunctional 

The following menu entries are working. Unlisted commands do not work.

  File menu

    * File > New
    * File > Open
    * File > Open Samples
    * File > Quit

  Edit menu

    * Edit > Undo
        (works for ImageJ 1.x commands)
    * Edit > Fill
    * Edit > Invert
    * Edit > Options
        (note that option values can be set but their values are ignored internally)

  Image menu

    * Image > Transform > Flip Horizontally
    * Image > Transform > Flip Vertically
    * Image > Transform > Rotate 90 Degrees Left
    * Image > Transform > Rotate 90 Degrees Right
    * Image > Transform > Rotate
    * Image > Transform > Translate

  Process menu

    * Smooth
    * Sharpen
    * Find Edges
    * Noise > Add Noise
    * Noise > Add Specific Noise
    * Noise > Salt and Pepper
    * Shadows
        (all commands except Shadows Demo)
    * Binary
        (all commands except:
          Convert to Mask
          Close
          Ultimate Points)
    * Math
    * FFT > FFT
    * Filters
        (all commands except Show Circular Masks)
    * Image Calculator
    * Gradient

  Analyze menu

    * Measure
    * Analyze Particles
    * Summarize
    * Distribution
    * Clear Results
    * Histogram
    * Surface Plot
    * Tools > Save XY Coordinates
    * Tools > Fractal Box Count
    * Tools > Curve Fitting
    * Tools > Scale Bar
    * Tools > Calibration Bar

  Plugins menu

    * Macros > Run
    * Macros > Edit
    * Shortcuts > List Shortcuts
    * Utilities > Control Panel
    * Utilities > ImageJ Properties
    * Utilities > Threads
    * Utilities > Capture Image
    * Utilities > Capture Screen
