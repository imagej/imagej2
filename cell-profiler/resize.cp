CellProfiler Pipeline: http://www.cellprofiler.org
Version:1
SVNRevision:9877

InputExternal:[module_num:1|svn_version:\'9877\'|variable_revision_number:1|show_window:True|notes:\x5B\x5D]
    Give this image a name:original

Resize:[module_num:2|svn_version:\'9572\'|variable_revision_number:2|show_window:True|notes:\x5B\x5D]
    Select the input image:original
    Name the output image:resized
    Select resizing method:Resize by a fraction or multiple of the original size
    Resizing factor:0.25
    Width of the final image, in pixels:100
    Height of the final image, in pixels:100
    Interpolation method:Bicubic

OutputExternal:[module_num:3|svn_version:\'9877\'|variable_revision_number:1|show_window:True|notes:\x5B\x5D]
    Select an image a name to export:resized
