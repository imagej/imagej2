package ij.plugin;
import ij.*;
import ij.text.TextPanel;
import ij.measure.ResultsTable;
import ij.io.*;
import java.io.*;

/** Writes measurements to a tab-delimited text file. */
public class MeasurementsWriter implements PlugIn {

	public void run(String path) {
		save(path);
	}
	
	public boolean save(String path) {
		if (IJ.isResultsWindow()) {
			TextPanel tp = IJ.getTextPanel();
			if (tp!=null) {
				if (!tp.saveAs(path))
					return false;
			}
		} else {
            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt==null || rt.getCounter()==0)
                return false;
            if (path.equals("")) {
                SaveDialog sd = new SaveDialog("Save as Text", "Results", Prefs.get("options.ext", ".xls"));
                String file = sd.getFileName();
                if (file == null) return false;
                path = sd.getDirectory() + file;
            }
            PrintWriter pw = null;
            try {
                FileOutputStream fos = new FileOutputStream(path);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                pw = new PrintWriter(bos);
            }
            catch (IOException e) {
                IJ.log("MeasurementsWriter: "+e);
                return false;
            }
            int n = rt.getCounter();
            for (int i=0; i<n; i++) {
                pw.println(rt.getRowAsString(i));
            }
            pw.close();
        }
		return true;
	}

}

