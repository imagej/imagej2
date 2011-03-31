Welcome to ImageJ 2.0 alpha 1

There are a number of known issues with this release:

The memory allocated to ImageJ is fixed at 512 mb

Color is currently unsupported.
  * All images display in grayscale
  * Color images are separated into images with 3 channels 

The following toolbar tools are working. Unlisted tools are not.

  * Pan (hand icon)
  * Zoom (magnifying glass icon)
  * Probe (crosshairs icon)

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
        all commands except Shadows Demo
    * Binary
        all commands except:
          Convert to Mask
          Close
          Ultimate Points
    * Math
    * FFT > FFT
    * Image Calculator
    * Gradient

