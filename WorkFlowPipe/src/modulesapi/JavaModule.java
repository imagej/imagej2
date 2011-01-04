package modulesapi;

import pipesentity.Conf;
import pipesentity.Module;
import pipesentity.Preview;

public interface JavaModule {

	public Module getModule();
	public Preview getPreview( Conf[] confs );
	
}
