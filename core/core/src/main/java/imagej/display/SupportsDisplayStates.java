package imagej.display;


public interface SupportsDisplayStates {
	DisplayState getCurrentState();
	void setCurrentState(DisplayState state);
}
