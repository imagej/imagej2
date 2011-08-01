Thanks for trying ImageJ 2.0.0-alpha4!

This is an "alpha"-quality release, meaning the code is not finished, nor is
the design fully stabilized. We are releasing it for early community feedback,
and to demonstrate project directions and progress.

DO NOT USE THIS RELEASE FOR ANYTHING IMPORTANT.

For this release, like previous alpha releases, we have tried to model the
application after ImageJ v1.x as much as is reasonable. However, please be
aware that version 2.0.0 is essentially a total rewrite of ImageJ from the
ground up. It provides backward compatibility with older versions of ImageJ by
bundling the latest v1.x code and translating between "legacy" and "modern"
image structures.

ImageJ v2.0.0-alpha4 has made substantial progress in many areas:
  * Better image displays:
    + Autoscaling based on minimum and maximum pixel values.
    + Bounds checking when compositing multiple channels.
  * Many bugfixes and improvements to overlay support:
    + Better translation between IJ1 and IJ2 ROIs.
    + IJ2 Crop plugin now operates on the selected region.
  * Better services architecture:
    + Initial support for multiple simultaneous instances of ImageJ.
  * Lots of improvements to the extensibility framework:
    + New "Dynamic Plugins" can customize their inputs and outputs at runtime.
    + Eliminated use of plugin-specific code wherever possible in favor of more
      general module interfaces.
  * New Command Finder plugin (Ctrl+L).
  * Several new debugging plugins (in Plugins > Debug).

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

1) Some tools still have bugs:
     * The pixel probe tool is not always accurate.
     * The zoom tool does not center properly on the mouse cursor.

2) The memory allocated to ImageJ is fixed at 512 MB.

3) Many more bugs and limitations not listed here.


TOOLBAR
-------

Many toolbar tools are not yet implemented, and hence grayed out.


MENUS
-----

IJ2 is currently made up of both IJ1 & IJ2 plugins. IJ1 plugins are
distinguished with a small microscope icon next to their name in the menus,
while IJ2 plugins have either a green puzzle piece or a custom icon.

Many IJ1 menu commands work, but many do not, for various reasons. We are
still working to improve the number of working IJ1 commands, and/or update them
to pure IJ2 plugins.


MACROS AND SCRIPTS
------------------

It is possible to execute IJ1 macros in IJ2, though you will likely experience
mixed results. You can even include calls to overridden IJ2 commands but they
will not record correctly.
