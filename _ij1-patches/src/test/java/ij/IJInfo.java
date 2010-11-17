package ij;

// class to hold various info about underlying IJ.
public class IJInfo {
	// BDZ: I had an issue where I was generating tests that failed with the original IJ. Rather than comment out the
	//   tests I have created this constant for runtime checking so that more rigorous tests can be exercised after
	//   the refactor and when those changed systems have been hardened. Could become a boolean set from the command line.
	public static final boolean RUN_ENHANCED_TESTS = false;
	
	// BDZ: some methods interact with the GUI. Enable the following boolean to run those tests interactively. Could become
	//   a boolean set from the command line.
	public static final boolean RUN_GUI_TESTS = false;
}
