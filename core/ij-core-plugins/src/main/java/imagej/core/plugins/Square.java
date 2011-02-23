package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.SqrFunction;

@Plugin(
	menuPath = "Process>ZippityDooDah"
)
@SuppressWarnings("rawtypes")
public class Square extends NAryOperation {

	public Square()
	{
		super(new SqrFunction());
	}

}
