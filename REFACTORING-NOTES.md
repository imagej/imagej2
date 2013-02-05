CURRENT STRUCTURE
=================

The current ImgLib2 + ImageJ2 data & display hierarchy is as follows, from
"low-level" to "high-level" classes:

Core ImgLib2 interfaces
-----------------------

The following interfaces define the baseline ImgLib2 concepts of
N-dimensional spaces and intervals (i.e., hyperrectangular regions within
those spaces):

                          EuclideanSpace
                                |
            /-------------------|
            |                   |
            |           /----------------\
            |           |                |
            |       Dimensions      RealInterval             Iterable
            |           |                |                      |
            |           |                |----------------------/
            |           |                |           |
            |           \----------------/           |
            |                   |                    |
    RandomAccessible         Interval       IterableRealInterval
            |                   |                    |
            |                   |--------------------/
            |                   |          |
            \-------------------/          |
                      |                    |
                      |                    |
          RandomAccessibleInterval   IterableInterval
                      |                    |
                      \--------------------/
                                 |
                                Img

ImgLib2 ROI interfaces
----------------------

The following hierarchy defines ImgLib2 regions of interest (ROIs):

                               EuclideanSpace
                                     |
                            /------------------\
                            |                  |
                   RealRandomAccessible   RealInterval
                            |                  |
                            \------------------/
                                     |
                      RealRandomAccessibleRealInterval
                                     |
                              RegionOfInterest
                                     |
                          IterableRegionOfInterest
                                     |
               /-------------------------------------------\
               |                          |                |
    RectangleRegionOfInterest   EllipseRegionOfInterest   etc.

Extended ImgLib2 hierachy
-------------------------

The following class hiearchy provides support for attaching certain metadata
to an Img; in particular: a name, axis labels, and calibration values:

       EuclideanSpace
             |
     /----------------\
     .                |
     .          CalibratedSpace   Named   Sourced   ImageMetadata
     .                |             |        |            |
     .                \-----------------------------------/
     .                                  |
    Img                             Metadata
     |                                  |
     \----------------------------------/
                       |
                    ImgPlus

Core ImageJ2 hierarchy
----------------------

The following interface hierarchies are for ImageJ2:

            Data                        DataView
              |                            |
       /--------------\             /-------------\
       |              |             |             |
    Dataset        Overlay     DatasetView   OverlayView

TODO: Display
TODO: ImageDisplay
TODO: DisplayViewer


PROPOSED CHANGES
================

TODO: Finish and clean up this writeup!

Right now, a Display is a list of Objects, and an ImageDisplay is a list of
DataViews.

There are problems with a threshold overlay being associated with
ImageDisplay. Like other overlays/ROIs, we want them to be applicable to any
subset of data objects in a tree structure.

- We need a CompositeData that is the union of datas
 -- and implements CalibratedInterval or whatever interface
- Then the Display architecture could be simplified to take only
  a single object, rather than a List
- We could also eliminate the Viewer/Display dichotomy, since we
  wouldn't need the agnostic 'display' anymore for anything
 -- we only had it because DatasetView was insufficient to encapsulate
    the visualization settings for the entire display
 -- instead, let's have a CompositeDataView
  --- wraps a CompositeData
  --- Internally has a shadow tree of DataViews, one per constituent
  --- ImageDisplay is simply a display for CompositeDataView. All uses
      of ImageDisplay would be replaced with CompositeDataView (but
      think of a better name?).
 -- we may need a TableView that wraps Table for storing viz settings
    (e.g., which column is currently sorted?)
 -- we may need a TextView that wraps String too... but why?

ImgPlus / Dataset dichotomy:
- Migrate ImgPlus into ImageJ2 and merge with Dataset
- Units stuff can live in ij-data
- Move AxisType, Metadata, etc., into ImageJ2
- problems with ImgLib2 OPS and ImgPlus dependencies...
- this is harder than the rest:
 -- imglib2-io uses ImgPlus! What to do...
 -- imglib2-ops uses it a LOT
 -- imglib2-examples interactive viewer uses CalibratedSpace
 -- a few (more minor) references, probably easily remedied

Why do this refactoring:
- simplifies the API, which is good for all developers
- will go really far toward realizing "multiple datasets in the same
  display" functionality
- fits in with beta7 goals (though will need several more weeks of
  development to achieve it)

Other comments, from discussion with other developers:

- Perhaps we can eliminate the Data interface (just use ImgLib2's
  Interval or RandomAccessibleInterval instead?). We can make DataView
  support wrapping of any Java Object, not just Datas. There would still
  need to be more specific subinterfaces of DataView that implement
  ImgLib2 Internal and other interfaces, since one common piece of
  metadata that Interval Views store is the position in N-space.
- We can have an imglib2-data module that contains (the artist formerly
  known as) ImgPlus, work currently on axes branch, and related metadata
  stuff. Data, Dataset, Overlay... these things could be pushed down
  into imglib2-data and/or merged with ImgPlus / RegionOfInterest / etc.
  This has a nice parallel to the existing ij-data, which would house
  the View subifaces corresponding to those constructs.
