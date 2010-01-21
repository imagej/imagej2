package ij.io;

// class to hold various info about underlying IJ
public class IJInfo {
	// BDZ: I had an issue where I was generating tests that failed with the original IJ. Rather than comment out the
	//   tests I have created this constant for runtime checking so that more rigorous tests can be exercised after
	//   the refactor and when those changed systems have been hardened.
	public static final boolean runEnhancedTests = false;
	
	// BDZ: some methods interact with the GUI. Enable the following boolean to run those tests interaactively.
	public static final boolean runGuiTests = false;
}
