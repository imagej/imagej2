Thanks for trying ImageJ 2.0.0-alpha2!

This is an "alpha"-quality release, meaning the code is not finished, nor is
the design fully stabilized. We are releasing it for early community feedback,
and to demonstrate project directions and progress.

DO NOT USE THIS RELEASE FOR ANYTHING IMPORTANT.

For this release, like 2.0.0-alpha1, we have tried to model the application
after ImageJ v1.x as much as is reasonable. However, please be aware that
version 2.0.0 is essentially a total rewrite of ImageJ from the ground up. It
provides backward compatibility with older versions of ImageJ by bundling the
latest v1.x code and translating between "legacy" and "modern" image
structures.

The most significant advancement for 2.0.0-alpha2 is its use of the ImgLib2
image processing library, developed at MPI-CBG by Stephan Saalfeld, Stephan
Priebisch, Tobias Pietzsch and others. Development of ImgLib2 received a major
boost at the recent Madison Fiji hackathon, and the library is now at a point
where it is usable within ImageJ2. Use of ImgLib2 has enabled ImageJ2 to
display composite color images with individual color lookup tables, similar to
ImageJ1's CompositeImage but without the seven-channel limit.

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

1) The image displays are better than in 2.0.0-alpha1, but still have
   limitations:
     * Display scaling is hardcoded to 0-255; i.e., no autoscaling is done
       based on actual sample value ranges. For example, 16-bit images may
       appear washed out.

2) The memory allocated to ImageJ is fixed at 512 MB.

3) Many more bugs and limitations not listed here.


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

Many IJ1 menu commands work, but many do not, for various reasons. We are
still working to improve the number of working IJ1 commands, and/or update them
to pure IJ2 plugins.


MACROS AND SCRIPTS
------------------

It is possible to execute IJ1 macros in IJ2, though you will likely experience
mixed results. You can even include calls to overridden IJ2 commands but they
will not record correctly.
