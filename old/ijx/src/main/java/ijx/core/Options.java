package ijx.core;

import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.ImageConverter;
import ijx.process.ColorProcessor;
import ijx.process.FloatBlitter;
import ijx.gui.IjxToolbar;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Line;
import ijx.roi.Roi;
import ijx.io.FileSaver;
import ijx.Menus;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;


import ijx.plugin.frame.LineWidthAdjuster;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.gui.IjxImageWindow;
import ijx.sezpoz.ActionIjx;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** This plugin implements most of the commands
in the Edit/Options sub-menu. */
public class Options implements PlugIn {
    /*
    options01="Line Width...",ij.plugin.Options("line")
    options02="Input/Output...",ij.plugin.Options("io")
    options09="Appearance...",ij.plugin.Options("display")
    options10="Conversions...",ij.plugin.Options("conv")
    options14="DICOM...",ij.plugin.Options("dicom")
    options15="Misc...",ij.plugin.Options("misc")
     */
    @ActionIjx(label = "Line Width...",
              commandKey = "option.linewidth",
              menu = "Edit>Options")
    public static final ActionListener LINE = callWithArg("option.lineWidth", "line");
    //
    @ActionIjx(label = "Input/Output...",
              commandKey = "option.io",
              menu = "Edit>Options")
    public static final ActionListener IO = callWithArg("option.io", "io");
    @ActionIjx(label = "Appearance...",
              commandKey = "option.display",
              menu = "Edit>Options")
    public static final ActionListener DISP = callWithArg("option.", "display");
    @ActionIjx(label = "Conversions...",
              commandKey = "option.conv",
              menu = "Edit>Options")
    public static final ActionListener CONV = callWithArg("option.conv", "conv");
    @ActionIjx(label = "DICOM...",
              commandKey = "option.dicom",
              menu = "Edit>Options")
    public static final ActionListener DICOM = callWithArg("option.dicom", "dicom");
    @ActionIjx(label = "Misc...",
              commandKey = "option.misc",
              menu = "Edit>Options")
    public static final ActionListener MISC = callWithArg("option.misc", "misc");

    private static ActionListener callWithArg(final String commandKey, final String arg) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IJ.runPlugIn(commandKey, "ijx.core.Options", arg);
            }
        };
    }

    public void run(String arg) {
        if (arg.equals("misc")) {
            miscOptions();
            return;
        } else if (arg.equals("line")) {
            lineWidth();
            return;
        } else if (arg.equals("io")) {
            io();
            return;
        } else if (arg.equals("conv")) {
            conversions();
            return;
        } else if (arg.equals("display")) {
            appearance();
            return;
        } else if (arg.equals("dicom")) {
            dicom();
            return;
        }
    }

    // Miscellaneous Options
    void miscOptions() {
        String key = IJ.isMacintosh() ? "command" : "control";
        GenericDialog gd = new GenericDialog("Miscellaneous Options", IJ.getTopComponentFrame());
        gd.addStringField("Divide by zero value:", "" + FloatBlitter.divideByZeroValue, 10);
        gd.addCheckbox("Use pointer cursor", Prefs.usePointerCursor);
        gd.addCheckbox("Hide \"Process Stack?\" dialog", IJ.hideProcessStackDialog);
        //gd.addCheckbox("Antialiased_Text", Prefs.antialiasedText);
        gd.addCheckbox("Require " + key + " key for shortcuts", Prefs.requireControlKey);
        gd.addCheckbox("Move isolated plugins to Misc. menu", Prefs.moveToMisc);
        gd.addCheckbox("Run single instance listener", Prefs.enableRMIListener != 0);
        gd.addCheckbox("Debug mode", IJ.debugMode);
        gd.addHelp(IJ.URL + "/docs/menus/edit.html#misc");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        String divValue = gd.getNextString();
        if (divValue.equalsIgnoreCase("infinity") || divValue.equalsIgnoreCase("infinite")) {
            FloatBlitter.divideByZeroValue = Float.POSITIVE_INFINITY;
        } else if (divValue.equalsIgnoreCase("NaN")) {
            FloatBlitter.divideByZeroValue = Float.NaN;
        } else if (divValue.equalsIgnoreCase("max")) {
            FloatBlitter.divideByZeroValue = Float.MAX_VALUE;
        } else {
            Float f;
            try {
                f = new Float(divValue);
            } catch (NumberFormatException e) {
                f = null;
            }
            if (f != null) {
                FloatBlitter.divideByZeroValue = f.floatValue();
            }
        }
        IJ.register(FloatBlitter.class);

        Prefs.usePointerCursor = gd.getNextBoolean();
        IJ.hideProcessStackDialog = gd.getNextBoolean();
        //Prefs.antialiasedText = gd.getNextBoolean();
        Prefs.requireControlKey = gd.getNextBoolean();
        Prefs.moveToMisc = gd.getNextBoolean();
        Prefs.enableRMIListener = gd.getNextBoolean() ? 1 : 0;
        IJ.debugMode = gd.getNextBoolean();
    }

    void lineWidth() {
        int width = (int) IJ.getNumber("Line Width:", Line.getWidth());
        if (width == IJ.CANCELED) {
            return;
        }
        Line.setWidth(width);
        LineWidthAdjuster.update();
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null && imp.isProcessor()) {
            ImageProcessor ip = imp.getProcessor();
            ip.setLineWidth(Line.getWidth());
            Roi roi = imp.getRoi();
            if (roi != null && roi.isLine()) {
                imp.draw();
            }
        }
    }

    // Input/Output options
    void io() {
        GenericDialog gd = new GenericDialog("I/O Options");
        gd.addNumericField("JPEG quality (0-100):", FileSaver.getJpegQuality(), 0, 3, "");
        gd.addNumericField("GIF and PNG transparent index:", Prefs.getTransparentIndex(), 0, 3, "");
        gd.addStringField("File extension for tables:", Prefs.get("options.ext", ".txt"), 4);
        gd.addCheckbox("Use JFileChooser to open/save", Prefs.useJFileChooser);
        gd.addCheckbox("Save TIFF and raw in Intel byte order", Prefs.intelByteOrder);

        gd.setInsets(15, 20, 0);
        gd.addMessage("Results Table Options");
        gd.setInsets(3, 40, 0);
        gd.addCheckbox("Copy_column headers", Prefs.copyColumnHeaders);
        gd.setInsets(0, 40, 0);
        gd.addCheckbox("Copy_row numbers", !Prefs.noRowNumbers);
        gd.setInsets(0, 40, 0);
        gd.addCheckbox("Save_column headers", !Prefs.dontSaveHeaders);
        gd.setInsets(0, 40, 0);
        gd.addCheckbox("Save_row numbers", !Prefs.dontSaveRowNumbers);

        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        int quality = (int) gd.getNextNumber();
        if (quality < 0) {
            quality = 0;
        }
        if (quality > 100) {
            quality = 100;
        }
        FileSaver.setJpegQuality(quality);
        int transparentIndex = (int) gd.getNextNumber();
        Prefs.setTransparentIndex(transparentIndex);
        String extension = gd.getNextString();
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        Prefs.set("options.ext", extension);
        Prefs.useJFileChooser = gd.getNextBoolean();
        Prefs.intelByteOrder = gd.getNextBoolean();
        Prefs.copyColumnHeaders = gd.getNextBoolean();
        Prefs.noRowNumbers = !gd.getNextBoolean();
        Prefs.dontSaveHeaders = !gd.getNextBoolean();
        Prefs.dontSaveRowNumbers = !gd.getNextBoolean();
        return;
    }

    // Conversion Options
    void conversions() {
        double[] weights = ColorProcessor.getWeightingFactors();
        boolean weighted = !(weights[0] == 1d / 3d && weights[1] == 1d / 3d && weights[2] == 1d / 3d);
        //boolean weighted = !(Math.abs(weights[0]-1d/3d)<0.0001 && Math.abs(weights[1]-1d/3d)<0.0001 && Math.abs(weights[2]-1d/3d)<0.0001);
        GenericDialog gd = new GenericDialog("Conversion Options");
        gd.addCheckbox("Scale When Converting", ImageConverter.getDoScaling());
        String prompt = "Weighted RGB Conversions";
        if (weighted) {
            prompt += " (" + IJ.d2s(weights[0]) + "," + IJ.d2s(weights[1]) + "," + IJ.d2s(weights[2]) + ")";
        }
        gd.addCheckbox(prompt, weighted);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        ImageConverter.setDoScaling(gd.getNextBoolean());
        Prefs.weightedColor = gd.getNextBoolean();
        if (!Prefs.weightedColor) {
            ColorProcessor.setWeightingFactors(1d / 3d, 1d / 3d, 1d / 3d);
        } else if (Prefs.weightedColor && !weighted) {
            ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
        }
        return;
    }

    void appearance() {
        GenericDialog gd = new GenericDialog("Appearance", IJ.getTopComponentFrame());
        gd.addCheckbox("Interpolate zoomed images", Prefs.interpolateScaledImages);
        gd.addCheckbox("Open images at 100%", Prefs.open100Percent);
        gd.addCheckbox("Black canvas", Prefs.blackCanvas);
        gd.addCheckbox("No image border", Prefs.noBorder);
        gd.addCheckbox("Use inverting lookup table", Prefs.useInvertingLut);
        gd.addCheckbox("Antialiased tool icons", Prefs.antialiasedTools);
        gd.addNumericField("Menu font size:", Menus.getFontSize(), 0, 3, "points");
        gd.addHelp(IJ.URL + "/docs/menus/edit.html#appearance");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        boolean interpolate = gd.getNextBoolean();
        Prefs.open100Percent = gd.getNextBoolean();
        boolean blackCanvas = gd.getNextBoolean();
        boolean noBorder = gd.getNextBoolean();
        boolean useInvertingLut = gd.getNextBoolean();
        boolean antialiasedTools = gd.getNextBoolean();
        boolean change = antialiasedTools != Prefs.antialiasedTools;
        Prefs.antialiasedTools = antialiasedTools;
        if (change) {
            ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).repaint();
        }
        int menuSize = (int) gd.getNextNumber();
        if (interpolate != Prefs.interpolateScaledImages) {
            Prefs.interpolateScaledImages = interpolate;
            IjxImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.draw();
            }
        }
        if (blackCanvas != Prefs.blackCanvas) {
            Prefs.blackCanvas = blackCanvas;
            IjxImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                IjxImageWindow win = imp.getWindow();
                if (win != null) {
                    if (Prefs.blackCanvas) {
                        win.setForeground(Color.white);
                        win.setBackground(Color.black);
                    } else {
                        win.setForeground(Color.black);
                        win.setBackground(Color.white);
                    }
                    imp.repaintWindow();
                }
            }
        }
        if (noBorder != Prefs.noBorder) {
            Prefs.noBorder = noBorder;
            IjxImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.repaintWindow();
            }
        }
        if (useInvertingLut != Prefs.useInvertingLut) {
            invertLuts(useInvertingLut);
            Prefs.useInvertingLut = useInvertingLut;
        }
        if (menuSize != Menus.getFontSize() && !IJ.isMacintosh()) {
            Menus.setFontSize(menuSize);
            IJ.showMessage("Appearance", "Restart ImageJ to use the new font size");
        }
    }

    void invertLuts(boolean useInvertingLut) {
        int[] list = WindowManager.getIDList();
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.length; i++) {
            IjxImagePlus imp = WindowManager.getImage(list[i]);
            if (imp == null) {
                return;
            }
            ImageProcessor ip = imp.getProcessor();
            if (useInvertingLut != ip.isInvertedLut() && !ip.isColorLut()) {
                ip.invertLut();
                int nImages = imp.getStackSize();
                if (nImages == 1) {
                    ip.invert();
                } else {
                    IjxImageStack stack2 = imp.getStack();
                    for (int slice = 1; slice <= nImages; slice++) {
                        stack2.getProcessor(slice).invert();
                    }
                    stack2.setColorModel(ip.getColorModel());
                }
            }
        }
    }

    // DICOM options
    void dicom() {
        GenericDialog gd = new GenericDialog("DICOM Options");
        gd.addCheckbox("Open as 32-bit float", Prefs.openDicomsAsFloat);
        gd.addMessage("Orthogonal Views");
        gd.setInsets(5, 40, 0);
        gd.addCheckbox("Rotate YZ", Prefs.rotateYZ);
        gd.setInsets(0, 40, 0);
        gd.addCheckbox("Flip XZ", Prefs.flipXZ);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        Prefs.openDicomsAsFloat = gd.getNextBoolean();
        Prefs.rotateYZ = gd.getNextBoolean();
        Prefs.flipXZ = gd.getNextBoolean();
    }
} // class Options

