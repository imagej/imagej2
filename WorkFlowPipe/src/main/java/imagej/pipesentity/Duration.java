package imagej.pipesentity;

public class Duration {
	private double duration;
	
	public Duration( double duration )
	{
		this.duration = duration;
	}
	
	public String getValue()
	{
		return new Double(duration).toString();
	}
}
