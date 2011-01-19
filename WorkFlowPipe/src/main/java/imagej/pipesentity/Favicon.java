package imagej.pipesentity;

public class Favicon {

	private URL url;
	
	public Favicon( URL url )
	{
		this.url = url;
	}
	
	public String getValue()
	{
		return url.getValue();
	}
}
