package com.example.beaconoffice;

import org.altbeacon.beacon.Beacon;
import java.util.ArrayList;

/**
 * DataList class represents a list of up to three triplets (Beacon ID, RSSI, Distance)
 * according to which the application will compute the current initiator's position.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @since 5/8/2022
 */
public class DataList {

    private ArrayList<String> peerID = new ArrayList<>();
    private ArrayList<Integer> rssi = new ArrayList();
    private ArrayList<Float> distance = new ArrayList();

    /**
     * Default constructor
     */
    public DataList () { }

    /**
     * Adds a triplet (Beacon ID, RSSI, Distance) to this list.
     * @param peerID the ID of the AltBeacon
     * @param rssi the RSSI value received by the WiRa initiator
     * @param distance the Distance of this AltBeacon from the WiRa Initiator, in meters
     * @see ScanBeacons#addBeaconValues(Beacon)  
     */
    public void addDataElement (ArrayList<String> peerID, ArrayList<Integer> rssi, ArrayList<Float> distance) {
        this.peerID.addAll(peerID);
        this.rssi.addAll(rssi);
        this.distance.addAll(distance);
    }

    /**
     * Computes the count of triplets in this list
     * 
     * @return the requested count
     * @see ScanBeacons#addBeaconValues(Beacon) 
     */
    public int getSize() {
        return peerID.size();
    }

    /**
     * Clears all data from the list.
     * 
     * @see ScanBeacons#addBeaconValues(Beacon) 
     */
    public void clear() {
        peerID.clear();
        rssi.clear();
        distance.clear();
    }
}
