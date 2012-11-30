package imagej.undo;


public interface SupportsDisplayStates {
	DisplayState getCurrentState();
	void setCurrentState(DisplayState state);
}
