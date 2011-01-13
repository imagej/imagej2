package com.tomwheeler.example.dogfilesupport;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;

/**
 * <p>
 * This class models a dog as a JavaBean. It is the thing that gets edited in
 * the editor component and viewed in the viewer component.
 * </p>
 *
 * @author Tom Wheeler
 */
public class Dog implements Serializable {

    private static final long serialVersionUID = -4013756838967894258L;

    private transient PropertyChangeSupport pcs;

    /** Enum of options used for the sex property of the dog object (can select only one) */
    public static enum Sex { MALE, FEMALE };

    /** values allowed for the breeds (can select only one of these) */
    public static enum Breed {
        Beagle, Bulldog, Chihuahua, Collie, Dachshund, Mutt, Poodle
    };

    public final static String NAME_PROP = "name";
    private String name;

    public final static String AGE_PROP = "age";
    private int age;

    public final static String PLAYS_FETCH_PROP = "playsFetch";
    private boolean playsFetch;

    public final static String SEX_PROP = "sex";
    private Sex sex;

    public final static String BREED_PROP = "breed";
    private Breed breed;

    /**
     * <p>
     * Creates a new dog instance using all default settings.
     * </p>
     */
    public Dog() {
        this(null);
    }

    /**
     * <p>
     * Creates a new dog instance using the specified name.
     * </p>
     *
     * @param name the dog's name
     */
    public Dog(String name) {
        pcs = new PropertyChangeSupport(this);
        setName(name);
    }

    /**
     * @return the age of the dog
     */
    public int getAge() {
        return age;
    }

    /**
     * <p>
     * Changes the dog's age property and fires an event to notify
     * registered listeners.
     * </p>
     *
     * @param age the dog's new age
     */
    public void setAge(int age) {
        int oldValue = this.age;
        this.age = age;
        pcs.firePropertyChange(AGE_PROP, oldValue, this.age);
    }

    /**
     * @return the name of the dog
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Changes the dog's name property and fires an event to notify
     * registered listeners.
     * </p>
     *
     * @param name the dog's new name
     */
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        pcs.firePropertyChange(NAME_PROP, oldValue, this.name);
    }


    /**
     * @return whether or not the dog plays fetch
     */
    public boolean getPlaysFetch() {
        return playsFetch;
    }

    /**
     * <p>
     * Changes the dog's playsFetch property and fires an event to notify
     * registered listeners.

     * </p>
     *
     * @param playsFetch whether or not the dog plays fetch
     */
    public void setPlaysFetch(boolean playsFetch) {
        boolean oldValue = this.playsFetch;
        this.playsFetch = playsFetch;
        pcs.firePropertyChange(PLAYS_FETCH_PROP, oldValue, this.playsFetch);
    }


    /**
     * @return whether the dog is male or female
     */
    public Sex getSex() {
        return sex;
    }

    /**
     * <p>
     * Changes the dog's sex property and fires an event to notify
     * registered listeners.

     * </p>
     *
     * @param sex the sex of the dog (whether male or female)
     */
    public void setSex(Sex sex) {
        Sex oldValue = this.sex;
        this.sex = sex;
        pcs.firePropertyChange(SEX_PROP, oldValue, this.sex);
    }

    /**
     * @return the Breed of the dog.
     */
    public Breed getBreed() {
        return breed;
    }

    /**
     * <p>
     * Changes the dog's breed property and fires an event to notify
     * registered listeners.
     * </p>
     *
     * @param breed the dog's new breed
     */
    public void setBreed(Breed breed) {
        Breed oldValue = this.breed;
        this.breed = breed;
        pcs.firePropertyChange(BREED_PROP, oldValue, this.breed);
    }


    /**
     * <p>
     * Removes the specified PropertyChangeListener from the listener list.
     * </p>
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * <p>
     * Add a PropertyChangeListener to the listener list.
     * </p>
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * <p>
     * It's necessary to implement this because derialization does not use the
     * constructor and we have to ensure that the property change support
     * instance gets initialized.
     * </p>
     *
     * @param in the input stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        in.defaultReadObject();
    }
}
