package imagedisplay.zoom;


//Test.java, another version
import imagedisplay.zoom.util.MouseSensitiveZSP;
import imagedisplay.zoom.util.ctrlbar.ClassicBar;
import java.awt.BorderLayout;
import javax.swing.JFrame;



public class NewTest extends JFrame {
    MouseSensitiveZSP zsp = null;
    //JPanel panel = new JPanel();

    public NewTest() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());
        zsp = new MouseSensitiveZSP(new MyJPanel());
        ClassicBar zbar = new ClassicBar();
        zbar.addZoomScrollPane(zsp);
        this.getContentPane().add(zsp, BorderLayout.CENTER);
        this.getContentPane().add(zbar, BorderLayout.SOUTH);

        this.setSize(400, 300);
        //this.pack();
        this.setVisible(true);
    }

    public static void main(String[] argv) {
        new NewTest();
    }
}
