JAR entries in this location are discovered by the system LUTFinder. This allows
delivery of color lookup tables via an update site inside .jar files so that
user modifications will not mark the files as "locally modified". It also helps
ImageJ applications that cannot access a file system at all to have a means of
providing a set of lookup tables.

Lookup tables discovered by the LUTFinder are subsequently installed in the
Lookup Tables submenu.

The lookup tables in this JAR can be overridden by end users by placing a .lut
file of the same name in the application's luts/ directory on the file system.
