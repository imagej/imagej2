package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.io.SaveDialog;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ijx.text.*;
import ijx.measure.ResultsTable;
import ijx.gui.IjxWindow;
import java.io.*;

/** Writes measurements to a csv or tab-delimited text file. */
public class MeasurementsWriter implements PlugIn {

	public void run(String path) {
		save(path);
	}
	
	public boolean save(String path) {
		IjxWindow frame = WindowManager.getFrontWindow();
		if (frame!=null && (frame instanceof TextWindow)) {
			TextWindow tw = (TextWindow)frame;
			if (tw.getTextPanel().getResultsTable()==null) {
				IJ.error("Save As>Results", "\""+tw.getTitle()+"\" is not a results table");
				return false;
			}
			return tw.getTextPanel().saveAs(path);
		} else if (IJ.isResultsWindow()) {
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

