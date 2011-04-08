/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.help;

import javax.help.JHelp;
import org.my.MyHelpTopComponent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

@ConvertAsProperties(dtd = "-//org.my//Help//EN", autostore = false)
@TopComponent.Description(preferredID = "HelpTopComponent", persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "org.my.HelpTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(displayName = "#CTL_HelpAction", preferredID = "HelpTopComponent")
public class MyHelpTopComponent extends TopComponent {

    private static JHelp helpViewer = null;

    public MyHelpTopComponent() {
        setDisplayName("Help");
        setLayout(new BorderLayout());
        //You can use the Lookup to find the HelpSet,
        //because help sets are registered in the Services folder,
        //automatically (if you're using the JavaHelp Wizard in the IDE),
        //which is accessible from the default Lookup:
        HelpSet help = Lookup.getDefault().lookup(HelpSet.class);
        helpViewer = new JHelp(help);
        add(helpViewer, BorderLayout.CENTER);
    }

    public static JHelp getHelpViewer() {
        return helpViewer;
    }

//    ...
  

}