package pipesentity;

import org.json.JSONObject;

public class Layout {

	String id;
	String def;
	String name;
	String desc;
	
	public Layout( String id, String def, String name, String description )
	{
		this.id = id;
		this.def = def;
		this.name = name;
		this.desc = desc;
	}
	
	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDescription(String description) {
		this.desc = desc;
	}

	public String getID()
	{
		return id;
	}
	
	public String getLayout()
	{
		return def;
	}
	
	@Override
	public String toString() {
		return this.name + " " + this.id + " " + this.desc + this.def;
	}
}
