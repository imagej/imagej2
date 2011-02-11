package imagej.types;

public abstract class AbstractType implements Type {

	@Override
	public String toString() {
		return getName();
	}

}
