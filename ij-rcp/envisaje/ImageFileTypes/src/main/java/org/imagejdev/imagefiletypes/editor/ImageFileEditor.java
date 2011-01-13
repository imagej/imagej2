package org.imagejdev.imagefiletypes.editor;

import java.awt.Component;
import javax.swing.*;
import org.imagejdev.imagefiletypes.ImageFile;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

//import com.jgoodies.binding.PresentationModel;
//import com.jgoodies.binding.adapter.BasicComponentFactory;
//import com.jgoodies.binding.list.SelectionInList;
//import com.jgoodies.binding.value.ValueModel;

/**
 * <p>
 * This class represents a Swing GUI component that uses the JGoodies databinding
 * framework to edit a ImageFile object.  This component should be instantiated and
 * attached to a TopComponent (that passes in a ImageFile instance) to be used in the
 * NetBeans framework.
 * </p>
 *
 * @author Tom Wheeler
 */
public class ImageFileEditor extends JPanel {

    private static final long serialVersionUID = -1631526746126348253L;

    private ImageFile ImageFile;

    private JLabel nameLabel, ageLabel, sexLabel;
    private JTextField nameField, ageField;
    private JRadioButton radioSexMale, radioSexFemale;

    private JLabel breedLabel;
    private JCheckBox playsFetch;
    private JComboBox breedCombo;        

    /**
     * <p>
     * Creates a new component to edit the specified ImageFile.
     * </p>
     * 
     * @param ImageFile the (non-null) ImageFile to be edited)
     */
    public ImageFileEditor(ImageFile ImageFile) {
        super();
        
        if (ImageFile == null) {
            throw new IllegalArgumentException("Supplied ImageFile instance is null.");
        }

        this.ImageFile = ImageFile;

        initComponents();
    }
    
    /**
     * <p>
     * This method initializes the components and hooks them up the the 
     * JGoodies data binding framework.  This is the only place in which
     * the JGoodies data binding framework is connected, so it provides a 
     * good example of how to do that in your components.
     * </p>  
     * 
     * <p>
     * See the inline comments within the source code of this method to 
     * see exactly how this is done.
     * </p>
     */
    private void initComponents() {
        // JGoodies data binding setup for the ImageFileBean
//        PresentationModel presentationModel = new PresentationModel(ImageFile);
//
//        // you should create the components using the factory, where possible; doing so
//        // hooks up the listeners as needed.
//
//        // create a simple text field tied to a string property
//        nameLabel = new JLabel("Name:");
//        nameField = BasicComponentFactory.createTextField(
//                presentationModel.getModel(ImageFile.NAME_PROP), false);
//        nameLabel.setLabelFor(nameField);
//
//        // create an field for entering an int/Integer value
//        ageLabel = new JLabel("Age:");
//        ageField = BasicComponentFactory.createIntegerField(presentationModel.getModel(ImageFile.AGE_PROP), 0);
//        ageLabel.setLabelFor(ageField);
//
//        // create a checkbox tied to a boolean property
//        playsFetch = BasicComponentFactory.createCheckBox(presentationModel.getModel(ImageFile.PLAYS_FETCH_PROP), "Plays Fetch");
//
//        // create a radio button group tied to an enum property
//        sexLabel = new JLabel("Sex:");
//        radioSexMale = BasicComponentFactory.createRadioButton(presentationModel.getModel(ImageFile.SEX_PROP), ImageFile.Sex.MALE ,"Male");
//        radioSexFemale = BasicComponentFactory.createRadioButton(presentationModel.getModel(ImageFile.SEX_PROP), ImageFile.Sex.FEMALE ,"Female");
//
//        // create a combo box, which allows the selection of a single item from an enum
//        breedLabel = new JLabel("Breed:");
//        ValueModel breedsModel = presentationModel.getModel(ImageFile.BREED_PROP);
//        SelectionInList breedsOptions = new SelectionInList(ImageFile.Breed.values(), breedsModel);
//        breedCombo = BasicComponentFactory.createComboBox(breedsOptions);
//
//        layoutComponents();        
    }

    /** 
     * <p>
     * this method just positions the labels, textfields, and other 
     * editor components.  It was created using the NetBeans GUI form 
     * editor and was modified thereafter to take advantage of JGoodies.  
     * There is nothing important in this method.
     * </p>
     */ 
    private void layoutComponents() {
        
        JLabel padding1 = new JLabel("");
        JLabel padding2 = new JLabel("");
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.LEADING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(padding1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(GroupLayout.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(padding2))))
                    .add(GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(ageLabel)
                            .add(sexLabel)
                            .add(breedLabel))
                        .add(13, 13, 13)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(breedCombo, GroupLayout.PREFERRED_SIZE, 313, GroupLayout.PREFERRED_SIZE)
                            .add(GroupLayout.LEADING, layout.createParallelGroup(GroupLayout.LEADING, false)
                                .add(GroupLayout.TRAILING, nameField, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
                                .add(GroupLayout.LEADING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(GroupLayout.LEADING)
                                        .add(GroupLayout.LEADING, layout.createSequentialGroup()
                                            .add(radioSexMale)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(radioSexFemale))
                                        .add(ageField, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.RELATED, 34, Short.MAX_VALUE)
                                    .add(playsFetch))))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(ageLabel)
                    .add(playsFetch)
                    .add(ageField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(sexLabel)
                    .add(radioSexMale)
                    .add(radioSexFemale))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(breedLabel)
                        .add(30, 30, 30)
                        .add(padding2))
                    .add(breedCombo, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12)
                .add(padding1, GroupLayout.PREFERRED_SIZE, 276, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }

}
