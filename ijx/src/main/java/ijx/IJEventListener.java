package ijx;

	/** Plugins that implement this interface are notified when the user
	     changes the foreground color, changes the background color,
	     closes the color picker, closes the Log window or switches to
	     another tool.
	*/
	public interface IJEventListener {
		public static final int FOREGROUND_COLOR_CHANGED = 0;
		public static final int BACKGROUND_COLOR_CHANGED = 1;
		public static final int COLOR_PICKER_CLOSED= 2;
		public static final int LOG_WINDOW_CLOSED= 3;
		public static final int TOOL_CHANGED= 4;

	public void eventOccurred(int eventID);

}
