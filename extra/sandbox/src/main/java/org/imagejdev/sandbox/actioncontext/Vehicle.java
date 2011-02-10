
package org.imagejdev.sandbox.actioncontext;


public class Vehicle {

    String vehicleName;

    boolean available;

    boolean standby;

    public Vehicle(String vehicleName, boolean available, boolean standby) {
        this.vehicleName = vehicleName;
        this.available = available;
        this.standby = standby;
    }
    
    public boolean isStandby() {
        return standby;
    }

    public void setStandby(boolean standby) {
        this.standby = standby;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String name) {
        this.vehicleName = name;
    }

}
