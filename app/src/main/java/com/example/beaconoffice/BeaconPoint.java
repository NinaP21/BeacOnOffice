package com.example.beaconoffice;

import org.altbeacon.beacon.Beacon;

/**
 * BeaconPoint class contains the (x, y) coordinates of the fixed positioned AltBeacon
 * and the distance between this and the WiRa initiator.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @since 5/8/2022
 * 
 * @see ScanBeacons#addBeaconValues(Beacon) 
 */
public class BeaconPoint {

    /**
     * Getter for the (x, y) coordinates of the fixed positioned AltBeacon
     *
     * @return the x any coordinates
     */
    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     * Getter for the distance between the AltBeacon and the WiRa initiator
     *
     * @return the requested distance in meters
     */
    public double getDistance() {
        return distance;
    }

    double[] coordinates;
    double distance;

    /**
     * Constructor of BeaconPoint Class
     *
     * @param coordinates the (x, y) coordinates of the fixed positioned AltBeacon
     * @param distance the distance between the AltBeacon and the WiRa initiator device, in meters
     */
    public BeaconPoint(double[] coordinates, double distance) {
        this.coordinates = coordinates;
        this.distance = distance;
    }
}
