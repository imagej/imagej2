package ij.macro;

class Variable implements MacroConstants, Cloneable {
	static final int VALUE=0, ARRAY=1, STRING=2;
    int symTabIndex;
    private double value;
    private String str;
    private Variable[] array;

    Variable() {
    }

    Variable(double value) {
        this.value = value;
    }

    Variable(int symTabIndex, double value, String str) {
        this.symTabIndex = symTabIndex;
        this.value = value;
        this.str = str;
    }

    Variable(int symTabIndex, double value, String str, Variable[] array) {
        this.symTabIndex = symTabIndex;
        this.value = value;
        this.str = str;
        this.array = array;
    }

    Variable(byte[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]&255);
    }

    Variable(int[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]);
    }

    Variable(double[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]);
    }

    double getValue() {
    	if (str!=null)
    			return convertToDouble();  // string to number conversions
    	else
        	return value;
    }

	double convertToDouble() {
		try {
			Double d = new Double(str);
			return d.doubleValue();
		} catch (NumberFormatException e){
			return Double.NaN;
		}
	}

    void setValue(double value) {
        this.value = value;
        str = null;
        array = null;
    }

    String getString() {
        return str;
    }

    void setString(String str) {
        this.str = str;
        value = 0.0;
        array = null;
    }

    Variable[] getArray() {
        return array;
    }

    void setArray(Variable[] array) {
        this.array = array;
        value = 0.0;
        str = null;
    }
    
    int getType() {
    	if (array!=null)
    		return ARRAY;
    	else if (str!=null)
    		return STRING;
    	else
    		return VALUE;
    }

	public String toString() {
		String s = "";
		if (array!=null)
			s += "array["+array.length+"]";
		else if (str!=null) {
			s = str;
			if (s.length()>80)
				s = s.substring(0, 80)+"...";
			s = s.replaceAll("\n", " | ");
			s = "\""+s+"\"";
		} else {
			if (value==(int)value)
				s += (int)value;
			else
				s += ij.IJ.d2s(value,4);
		}
		return s;
	}
    
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}

} // class Variable
