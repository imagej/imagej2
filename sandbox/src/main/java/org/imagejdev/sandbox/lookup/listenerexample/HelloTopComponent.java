package org.imagejdev.sandbox.lookup.listenerexample;

import java.awt.BorderLayout;
import java.util.Collection;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/*
 * http://blogs.sun.com/geertjan/entry/lookup_example
 * Consumer module, with dependency on the provider module:
*/

final class HelloTopComponent extends JFrame implements LookupListener {

    private static HelloTopComponent instance;
    private static final String PREFERRED_ID = "HelloTopComponent";
    private Result result;
    private JTextArea jTextArea1 = new JTextArea();

    private HelloTopComponent() {
        super();
        this.add(jTextArea1, BorderLayout.CENTER);
        jTextArea1.setSize(300, 300);
        this.pack();
        this.setVisible(true);
        // ...
        //We have a dependency on the provider module,
        //where we can access Selection.getSelection():
        Lookup lookup = Selection.getSelection();

        //Get the HelloProvider from the result:
        result = lookup.lookupResult(HelloProvider.class);

        //Add LookupListener on the result:
        result.addLookupListener(this);

        //Call result changed:
        resultChanged(null);

    }
    StringBuilder sb = new StringBuilder();
    int i;

    @Override
    public void resultChanged(LookupEvent arg0) {
        long mills = System.currentTimeMillis();
        Collection<? extends HelloProvider> instances = result.allInstances();
        for (HelloProvider helloProvider : instances) {
            String hello = helloProvider.sayHello();
            System.out.println(mills + ": " + hello + "\n");
            sb.append(mills + ": " + hello + "\n");
            jTextArea1.setText(sb.toString());
        }
    }

    public static void main(String[] args) {
        new HelloTopComponent();
    }
}
