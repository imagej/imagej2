// "Close All Windows"
// This macro closes all image windows.
// Add it to the StartupMacros file to create
// a "Close All Windows" command, or drop it
// in the ImageJ/plugins/Macros folder.
// Note that some ImageJ 1.37 has a bug that
// causes this macro to run very slowly.

  macro "Close All Windows" { 
      while (nImages>0) {
          selectImage(nImages);
          close(); 
      } 
  } 
