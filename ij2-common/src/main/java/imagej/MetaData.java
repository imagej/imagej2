package imagej;

// NOTES - this is a class that is really a placeholder. Need to flesh out exactly what metadata we want to handle. As is all of this
//   functionality can change.

public class MetaData
{
	private String[] axisLabels;
	private int directAccessSize;
	private String label;
	
	public MetaData()
	{
		this.label = "";
		this.axisLabels = new String[0];
		this.directAccessSize = 0;
	}
	
    // returns the order of the axes.
	public String[] getAxisLabels() { return this.axisLabels; }
	
	// set the order of the axes.
	public void setAxisLabels(String[] axisLabels) { this.axisLabels = axisLabels; }
	
    // returns the number of dimensions in primitive access Datasets. So XYZ could be org 2 (x&y).
	public int getDirectAccessDimensionCount() { return this.directAccessSize; }
	
    // returns the number of dimensions in primitive access Datasets. So XYZ could be org 2 (x&y).
	public void setDirectAccessDimensionCount(int size) { this.directAccessSize = size; }
	
    // get name of dataset - for ImageStack case a 2d plane within larger dataset could have a label
	public String getLabel() { return this.label; }
	
    // set name of dataset
	public void setLabel(String label) { this.label = label; }
}

/* TODO
 * since we travel down a Dataset heirarchy we need to minimize memory footprint by taking axisLabels ("x,y,z,c,t") and generate
 *   ("x,y,z,c"), ("x,y,z"), ("x,y"), & ("y") and refer to these inside the appropriate axis labels for each MetaData object.
 * I did this for dimensions in PlanarDatasetFactory. Adapt to this case when needed later.
 */