/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.debug;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import imagej.workflow.plugin.ItemWrapper;

/**
 * Maintains a list of debugging information as a workflow executes.  During and
 * after workflow execution provides a list of preview information.  The preview
 * information is used to display the debugging information on a web page.  In
 * particular, images are saved to the file system to be displayed on the web
 * page.
 *
 * @author Aivar Grislis
 */
public class WorkflowDebugger {
    private static final String FORMAT = "jpg";
    private static final String WEB_DIR = "web";
    private static final String PREVIEW_DIR = "preview";
    private static final String IMAGE = "image";
    private static final String PREVIEW_FILE_DIR = WEB_DIR + "/" + PREVIEW_DIR;
    private static final String PREVIEW_FILE_NAME = PREVIEW_FILE_DIR + "/" + IMAGE;
    private static final String PREVIEW_WEB_NAME = PREVIEW_DIR + '/' + IMAGE;
    private Object m_synchObject = new Object();
    private List<DebugInfo> m_debugInfoList = new ArrayList<DebugInfo>();
    private List<PreviewInfo> m_previewInfoList = new ArrayList<PreviewInfo>();
    private List<File> m_previewFileList = new ArrayList<File>();
    private int m_ordinal = 0;

    /**
     * Creates a debugger.
     */
    public WorkflowDebugger() {
        (new File(PREVIEW_FILE_DIR)).mkdir();
    }

    /**
     * Adds debugging information.
     * <p>
     * Called during workflow execution.
     *
     * @param debugInfo debugging information
     */
    public void addDebugInfo(DebugInfo debugInfo) {
        synchronized (m_synchObject) {
            m_debugInfoList.add(debugInfo);
        }
    }

    /**
     * Gets a snapshot of the preview information list.  Processes the debugging
     * information list.
     * <p>
     * Called during or after workflow execution.
     *
     * @return list of preview information.
     */
    public List<PreviewInfo> getPreviewInfoList() {
        List<PreviewInfo> previewInfoList = new ArrayList<PreviewInfo>();
        synchronized (m_synchObject) {
            // are we up to date?
            if (!m_debugInfoList.isEmpty()) {
                for (DebugInfo debugInfo : m_debugInfoList) {
                    System.out.println("debugInfo " + debugInfo.getDesc() + " " + debugInfo.getInstanceId());
                    ItemWrapper itemWrapper = debugInfo.getItemWrapper();
                    Object item = itemWrapper.getItem();
                    if (null != item) {
                        String content = "";
                        if (item instanceof RenderedImage) {
                            // save images to local file system web site for preview
                            String fileSuffix = m_ordinal++ + "." + FORMAT;
                            String fileName = PREVIEW_FILE_NAME + fileSuffix;
                            String webName = PREVIEW_WEB_NAME + fileSuffix;
                            File file = makePreviewFile(
                                    (RenderedImage) item,
                                    fileName,
                                    FORMAT);
                            m_previewFileList.add(file);
                            content = "<html><body>"
                                    +   "<a href='" + webName
                                    +       "' target='_blank'>"    // open link in new window
                                    +     "<img src='" + webName + "'/>"
                                    +   "</a>"
                                    + "</body></html>";
                        }
                        else {
                            content = item.toString();
                        }
                        m_previewInfoList.add(
                                new PreviewInfo(
                                        debugInfo.getInstanceId(),
                                        debugInfo.getDesc(),
                                        content));
                    }
                }

                // processed this debugging information
                m_debugInfoList.clear();

                // return snapshot of preview list
                previewInfoList.addAll(m_previewInfoList);
            }
            return previewInfoList;
        }
    }

    /**
     * Gets a snapshot of the preview information list for a given instance.
     * <p>
     * Called during or after workflow execution.  Can be called repeatedly as
     * workflow progresses.
     *
     * @param instanceId identifies the instance
     * @return list of preview information
     */
    public List<PreviewInfo> getPreviewInfoList(String instanceId) {
        List<PreviewInfo> previewInfoList = new ArrayList<PreviewInfo>();
        for (PreviewInfo previewInfo : getPreviewInfoList()) {
            if (instanceId.equals(previewInfo.getInstanceId())) {
                previewInfoList.add(previewInfo);
            }
        }
        return previewInfoList;
    }

    /**
     * Clears all lists and deletes all preview files.
     * <p>
     * Called before workflow run.  May be called after debugging is over.
     *
     */
    public void clear() {
        synchronized (m_synchObject) {
            for (File file : m_previewFileList) {
                file.delete();
            }
            m_previewFileList.clear();
            m_ordinal = 0;
        }
    }

    /**
     * Creates a preview file version of an image.
     *
     * @param image the image
     * @param fileName file name to use
     * @return the file
     */
    private File makePreviewFile(RenderedImage image, String fileName, String format) {
        File file = null;
        try {
            file = new File(fileName);
            ImageIO.write(image, "png", file);
        }
        catch (IOException e) {
        }
        return file;
    }
}
