package com.example.beaconoffice;

import android.content.Context;
import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;


/**
 * ScanBeacons class is responsible for reading the advertised data from a specific WiRa initiator.
 * When the application notices a packet with fresh content, it retrieves this data and applies
 * the mathematical algorithm to compute the initiator's position inside the building.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see MainActivity
 * @see HomeFragment
 * @since 31/8/2022
 */
public class ScanBeacons {

    private final double[][] ALTBEACONS = {
            {0, 0}, {6, 0}, {5, 4}, {13.8, 0}, {17.9, 4.3}, {5, 10.8}, {5, 19}, {0.6, 14.5}, {11, 19.8}, {0, 21.9}, {5, 26.9}, {10.6, 32.6}
    };
    private final MainActivity mainActivity;
    private double[] result;
    private Long oldCounter;
    private BeaconManager beaconManager;
    private DataList dataList = new DataList();
    private Region region;
    private boolean isPaused = false;

    /**
     * Class constructor that sets up the Beacon scanner by defining the region that will be scanned
     * and by stating that the scanner will look only for ALtBeacons.
     */
    public ScanBeacons(MainActivity mainActivity, Context context) {
        this.mainActivity = mainActivity;
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser("AltBeacon").
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //This was the Beacon layout for AltBeacons
        region = new Region("all-beacons-region", null, null, null);
    }

    /**
     * Starts scanning for AltBeacons with period 500ms.
     * When the scanner notices one or more Beacons, the application gets notified and
     * tries to collect data from those beacons.
     * 
     * @see MainActivity#startMeasurements() 
     * @see #addData(Collection)
     */
    public void scanAltBeacons() {

        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d("Beacon DEBUG", " I just saw a Beacon for the first time!!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d("Beacon DEBUG", " The Beacon is no longer available!!");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i("Beacon DEBUG", "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    addData(beacons);
                }
            }
        });

        beaconManager.startMonitoring(region);
        beaconManager.startRangingBeacons(region);

        beaconManager.setBackgroundScanPeriod(500l); // 500 ms
        beaconManager.setForegroundBetweenScanPeriod(500l);
    }

    /**
     * Checks every AltBeacon from the beaconCollection and keeps only the advertised
     * data from the specific WiRa initiator that the application is associated with.
     *
     * @param beaconCollection a collection of one or more AltBeacons,
     *                         that have just been scanned from the Beacon scanner.
     * @see #scanAltBeacons()
     * @see #addBeaconValues(Beacon)
     */
    public void addData(Collection<Beacon> beaconCollection) {
        for (Beacon currentBeacon : beaconCollection) {
            String currentInitiatorAddress = currentBeacon.getBluetoothAddress();
            if (currentInitiatorAddress.equals("48:23:35:00:00:AA")) {
                addBeaconValues(currentBeacon);
            }
        }
    }

    /**
     * Parses the initiator's advertised packet and retrieves three blocks of values:
     * First, the IDs of the three auxiliary Beacons that the initiator measures its distance from.
     * Then, the RSSI values of each auxiliary Beacon.
     * Finally, the distances of each Beacon from the initiator, in meters.
     * These values are used to calculate the (x, y) coordinates of the initiator and then send
     * the complete information to other classes in order to update the visual representation of
     * the newly tracked position as well as add data to "Measurement Results" in Logs page.
     *
     * @param beacon the object of the WiRa initiator Beacon that the application is associated with
     * @see #addData(Collection)
     * @see DataList#addDataElement(ArrayList, ArrayList, ArrayList) 
     * @see BeaconPoint
     * @see HomeFragment#receiveCoords(double[], ArrayList) 
     * @see #getPoint(ArrayList)
     */
    public void addBeaconValues(Beacon beacon) {

        String data = byteArrayToHexString(beacon.getId1().toByteArray()) + byteArrayToHexString(beacon.getId2().toByteArray());
        //Log.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", " "+data);
        Log.i("Counter ", "  " + beacon.getDataFields());

        ArrayList<String> peerID = new ArrayList<>();
        ArrayList<Integer> rssi = new ArrayList<>();
        ArrayList<Float> distance = new ArrayList<>();
        ArrayList<String> distances = new ArrayList<>();
        ArrayList<BeaconPoint> beaconPoint = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        double[] init = {0, 0};
        BeaconPoint newBeaconPoint = new BeaconPoint(init, 0);

        Long newCounter = beacon.getDataFields().get(0);
        if (!newCounter.equals(oldCounter)) {
            oldCounter = newCounter;

            if (dataList.getSize() >= 9) {
                dataList.clear();
            }

            for (int i = 0; i < 6; i += 2) {
                String hexnum = "" + data.charAt(i) + "" + data.charAt(i + 1);
                peerID.add(hexnum);
                Log.i("Peer ID ", "  " + hexnum);
            }

            for (int i = 6; i < 12; i += 2) {
                String hexnum = "" + data.charAt(i) + "" + data.charAt(i + 1);
                // First convert the Hex-number into a binary number:
                String bin = Integer.toString(Integer.parseInt(hexnum, 16), 2);

                // Now create the complement (make 1's to 0's and vice versa)
                String binCompl = bin.replace('0', 'X').replace('1', '0').replace('X', '1');

                // Now parse it back to an integer, add 1 and make it negative:
                int result = (Integer.parseInt(binCompl, 2) + 1) * -1;
                Log.i("RSSI value ", "  " + result + " db");
                rssi.add(result);
            }

            for (int i = 12; i < 35; i += 8) {
                String hexnum = "";
                for (int j = 7; j > 0; j -= 2)
                    hexnum = hexnum + "" + data.charAt(i + j - 1) + "" + data.charAt(i + j);

                Long l = Long.parseUnsignedLong(hexnum, 16);
                Float f = Float.intBitsToFloat(l.intValue());

                distance.add(f);
                Log.i("Distance ", "  " + f + " m");
            }

            dataList.addDataElement(peerID, rssi, distance);

            for (int i = 0; i < peerID.size(); i++) {
                if (peerID.get(i).equals("F1")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[0], distance.get(i));
                    indices.add(0);
                } else if (peerID.get(i).equals("F2")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[1], distance.get(i));
                    indices.add(1);
                } else if (peerID.get(i).equals("F3")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[2], distance.get(i));
                    indices.add(2);
                } else if (peerID.get(i).equals("F4")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[3], distance.get(i));
                    indices.add(3);
                } else if (peerID.get(i).equals("F5")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[4], distance.get(i));
                    indices.add(4);
                } else if (peerID.get(i).equals("F6")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[5], distance.get(i));
                    indices.add(5);
                } else if (peerID.get(i).equals("F7")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[6], distance.get(i));
                    indices.add(6);
                } else if (peerID.get(i).equals("F8")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[7], distance.get(i));
                    indices.add(7);
                } else if (peerID.get(i).equals("F9")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[8], distance.get(i));
                    indices.add(8);
                } else if (peerID.get(i).equals("FA")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[9], distance.get(i));
                    indices.add(9);
                } else if (peerID.get(i).equals("FB")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[10], distance.get(i));
                    indices.add(10);
                } else if (peerID.get(i).equals("FC")) {
                    newBeaconPoint = new BeaconPoint(ALTBEACONS[11], distance.get(i));
                    indices.add(11);
                }
                beaconPoint.add(newBeaconPoint);
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setMaximumFractionDigits(4);
                String currentDistance = peerID.get(i) + ":  " + decimalFormat.format(distance.get(i));
                distances.add(currentDistance);
            }

            for (int j = 0; j < 12; j++) {
                HomeFragment.active[j] = indices.contains(j);
            }

            result = getPoint(beaconPoint);
            Log.i("Coordinates of point are", "(x, y) =   (" + result[0] + ", " + result[1] + ")");

            mainActivity.getHomeFragment().receiveCoords(result, distances);
        }
    }

    /**
     * Implements the mathematical algorithm that calculates the position of the initiator,
     * given the distances from three fixed positioned AltBeacons.
     * This algorithm was designed by the intern student, George Giachnakis.
     *
     * @param beaconPoint contains a list of three triplets (Beacon ID, RSSI, Distance) according to
     *                    which the current initiator's position will be computed
     * @return the (x, y) coordinates of the initiator
     * @see #addBeaconValues(Beacon)
     * @author George Giachnakis
     */
    public double[] getPoint(ArrayList<BeaconPoint> beaconPoint) {
        double x1 = beaconPoint.get(0).getCoordinates()[0];
        double y1 = beaconPoint.get(0).getCoordinates()[1];
        double r1 = beaconPoint.get(0).getDistance();


        double x2 = beaconPoint.get(1).getCoordinates()[0];
        double y2 = beaconPoint.get(1).getCoordinates()[1];
        double r2 = beaconPoint.get(1).getDistance();

        double x3 = beaconPoint.get(2).getCoordinates()[0];
        double y3 = beaconPoint.get(2).getCoordinates()[1];
        double r3 = beaconPoint.get(2).getDistance();

        double x = 0;
        double y = 0;

        double A = x1 - x2;
        double B = y1 - y2;
        double D = x1 - x3;
        double E = y1 - y3;

        double T = (r1 * r1 - x1 * x1 - y1 * y1);
        double C = (r2 * r2 - x2 * x2 - y2 * y2) - T;
        double F = (r3 * r3 - x3 * x3 - y3 * y3) - T;

        double Mx = (C * E - B * F) / 2;
        double My = (A * F - D * C) / 2;
        double M = A * E - D * B;

        if (M != 0) {
            x = Mx / M;
            y = My / M;
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;
        }

        double[] coordinates = {x, y};
        return coordinates;
    }

    /**
     * Converts an array of bytes to an hexadecimal string,
     * where every byte consists of two hex digits
     * @param bytes the array of bytes
     * @return the string that contains the hexadecimal representation of bytes array
     *
     * @see #addBeaconValues(Beacon)
     */
    public String byteArrayToHexString(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff).toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * Pauses or unpauses the scanning of Beacons, according to the current state of the application.
     * The user gets informed in both cases.
     *
     * @see MainActivity#pauseMeasurements()
     */
    public void pauseAltBeacons() {
        if (!isPaused) {
            beaconManager.stopMonitoring(region);
            beaconManager.stopRangingBeacons(region);
            mainActivity.showSnackBar("Measurements paused");
            isPaused = true;
            Log.i("Measurements State", "----------------Paused----------------");
        } else {
            beaconManager.startMonitoring(region);
            beaconManager.startRangingBeacons(region);
            mainActivity.showSnackBar("Measurements unpaused");
            isPaused = false;
            Log.i("Measurements State", "----------------Unpaused----------------");
        }
    }

    /**
     * Resets the configuration of the Beacon scanner.
     *
     * @see MainActivity#resetMeasurements()
     */
    public void resetAltBeacons() {
        oldCounter = null;
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
    }
}
