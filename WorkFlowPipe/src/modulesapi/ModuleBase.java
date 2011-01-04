package modulesapi;

import java.util.ArrayList;

import pipesentity.Conf;
import pipesentity.Count;
import pipesentity.Description;
import pipesentity.Duration;
import pipesentity.ID;
import pipesentity.Item;
import pipesentity.ItemCount;
import pipesentity.Module;
import pipesentity.Name;
import pipesentity.Start;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.Type;
import pipesentity.UI;

public abstract class ModuleBase {

	protected Count count = new Count();
	protected ItemCount item_count = new ItemCount();
	protected ID id;
	protected Duration duration;
	protected Start start;
	protected Module module;
	protected Item[] items;
	protected ArrayList<Terminal> terminals;
	protected UI ui;
	protected Name name;
	protected Type type;
	protected Description description;
	protected ArrayList<Tag> tags;
	protected Conf[] confs;
	
	/*
	 * "count": "0",
	 
    "item_count": "0",
    "id": "sw-61",
    "duration": "0.000103",
    "start": "1294179746",
    "module": "WFP::Internal::FetchPage",
    "items": [];
	
	
*/
	
}
