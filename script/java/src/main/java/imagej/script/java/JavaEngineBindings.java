package imagej.script.java;

import imagej.script.AbstractScriptEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;


public class JavaEngineBindings implements Bindings {

	private Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Object put(String name, Object value) {
		if (!name.equals(ScriptEngine.FILENAME) && !name.equals("IJ")) {
			throw new UnsupportedOperationException();
		}
		return map.put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		for (final String key : toMerge.keySet()) {
			put(key, toMerge.get(key));
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

}
