package ijx.etc;

import ijx.CentralLookup;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFrame;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author GBH <imagejdev.org>
 */
final class TryCentralLookup extends JFrame {
    private static TryCentralLookup instance;

    private javax.swing.JTextField name;
    private javax.swing.JTextField pwd;
    private javax.swing.JTextField token;
    private javax.swing.JTextField uid;

    private static class DefaultUserInformation  extends UserInformation {
        public DefaultUserInformation() {
        }
                public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return pwd;
        }

        public void setPassword(String pwd) {
            this.pwd = pwd;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUserID() {
            return uid;
        }

        public void setUserID(String uid) {
            this.uid = uid;
        }
        String name;
        String pwd;
        String token;
        String uid;
    }

    private static class UserInformation {

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return pwd;
        }

        public void setPassword(String pwd) {
            this.pwd = pwd;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUserID() {
            return uid;
        }

        public void setUserID(String uid) {
            this.uid = uid;
        }
        String name;
        String pwd;
        String token;
        String uid;

        public UserInformation() {
        }
    }

    private Lookup.Result userInfoResult = null;

    private TryCentralLookup() {

        Lookup.Template template = new Lookup.Template(UserInformation.class);

        CentralLookup cl = CentralLookup.getDefault();
        userInfoResult = cl.lookup(template);
        userInfoResult.addLookupListener(new UserInformationListener());
    }


    private class SetterRunnable implements Runnable {
        UserInformation ui = null;

        public SetterRunnable(UserInformation ui) {
            this.ui = ui;
        }

        public void run() {
            name.setText(ui.getName());
            pwd.setText(ui.getPassword());
            uid.setText(ui.getUserID());
            token.setText(ui.getToken());
        }

        private void buttonActionPerformed(java.awt.event.ActionEvent evt) {
        //ok we need to remove any user information from the central lookup
        //and then we need to add this new one
            CentralLookup cl = CentralLookup.getDefault();
            Collection infos = cl.lookupAll(UserInformation.class);

            if (!infos.isEmpty()) {
                Iterator it = infos.iterator();
                while (it.hasNext()) {
                    UserInformation info = (UserInformation)it.next();
                    cl.remove(info);
                }
            }

            DefaultUserInformation info = new DefaultUserInformation();
            info.setName(name.getText());
            info.setPassword(pwd.getText());
            info.setUserID(uid.getText());
            info.setToken(token.getText());
            cl.add(info);

        }
    }

    private class UserInformationListener implements LookupListener {
        public void resultChanged(LookupEvent evt) {
            Object o = evt.getSource();
            if (o != null) {
                Lookup.Result r = (Lookup.Result) o;
                Collection infos = r.allInstances();
                if (infos.isEmpty()) {
                    EventQueue.invokeLater(new SetterRunnable(new DefaultUserInformation()));
                } else {
                    Iterator it = infos.iterator();
                    while (it.hasNext()) {
                        UserInformation info = (UserInformation) it.next();
                        EventQueue.invokeLater(new SetterRunnable(info));
                    }
                }
            }
        }
    }
}
