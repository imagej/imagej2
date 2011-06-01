Thanks for trying ImageJ 2.0.0-alpha3!

This is an "alpha"-quality release, meaning the code is not finished, nor is
the design fully stabilized. We are releasing it for early community feedback,
and to demonstrate project directions and progress.

DO NOT USE THIS RELEASE FOR ANYTHING IMPORTANT.

For this release, like 2.0.0-alpha1 and 2.0.0-alpha2, we have tried to model
the application after ImageJ v1.x as much as is reasonable. However, please be
aware that version 2.0.0 is essentially a total rewrite of ImageJ from the
ground up. It provides backward compatibility with older versions of ImageJ by
bundling the latest v1.x code and translating between "legacy" and "modern"
image structures.

The most significant advancement for 2.0.0-alpha3 is partial support for
regions of interest (ROIs), via the JHotDraw library. It is now possible to
draw rectangles, ellipses and polygons in an ImageJ2 display.

We have also improved the legacy layer so that many ImageJ1 plugins work
better, although it is still far from perfect. There is partial support for
preserving both ROIs and color lookup tables (LUTs) between IJ1 and IJ2 plugin
executions, meaning that you can draw a ROI and then execute an IJ1 plugin, and
it will operate on only the pixels within the ROI.

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

1) For the most part, the image displays are better than in previous alphas,
   but still have limitations:
     * Display scaling is hardcoded to 0-255; i.e., no autoscaling is done
       based on actual sample value ranges. For example, 16-bit images may
       appear washed out. You can manually adjust the min/max using the
       "Set Display Scale" plugin in the Image menu.
     * The pixel probe tool is not always accurate.
     * The zoom tool does not center properly on the mouse cursor.

2) The memory allocated to ImageJ is fixed at 512 MB.

3) Many more bugs and limitations not listed here.


TOOLBAR
-------

Many toolbar tools are not yet implemented, and hence grayed out. ImageJ
v2.0.0-alpha3 adds support for some ROI tools (rectangle, ellipse and polygon),
with more to come.


MENUS
-----

IJ2 is currently made up of both IJ1 & IJ2 plugins. IJ1 plugins are
distinguished with a small microscope icon next to their name in the menus.
Commands implemented purely in IJ2 do not have this marker.

Many IJ1 menu commands work, but many do not, for various reasons. We are
still working to improve the number of working IJ1 commands, and/or update them
to pure IJ2 plugins.


MACROS AND SCRIPTS
------------------

It is possible to execute IJ1 macros in IJ2, though you will likely experience
mixed results. You can even include calls to overridden IJ2 commands but they
will not record correctly.
