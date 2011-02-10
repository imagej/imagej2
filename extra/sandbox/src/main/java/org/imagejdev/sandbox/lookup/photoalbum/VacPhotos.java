package org.imagejdev.sandbox.lookup.photoalbum;


import javax.swing.Icon;
import javax.swing.ImageIcon;
//import photoalbumapp.Photo;

public class VacPhotos implements Photo {

    ImageIcon icon1 = new ImageIcon(getClass().getResource("/com/example/vacphotos/demo1.png"));
    ImageIcon icon2 = new ImageIcon(getClass().getResource("/com/example/vacphotos/demo2.png"));

    public VacPhotos() {
    }

    @Override
    public Icon[] getPhoto() {
        Icon[] icons = new Icon[]{icon1, icon2};
        return icons;
    }

    @Override
    public String[] getDescription() {
        String[] descs = new String[]{"pic 1", "pic2"};
        return descs;
    }

}