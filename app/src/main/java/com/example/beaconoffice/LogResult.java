package com.example.beaconoffice;

import java.util.function.Predicate;

/**
 * LogResult class represents one data entry in "Measurement Results" in Logs page.
 * This data consists of the timestamp of the measurement, the calculated x and y coordinates of the initiator
 * and the three distances that were sent from the initiator to the application.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see LogsAdapter
 * @since 31/8/2022
 */
public class LogResult implements Predicate<LogResult> {

    private String timestamp, xCoord, yCoord, distance1, distance2, distance3;

    public String getTimestamp() {
        return timestamp;
    }

    public String getxCoord() {
        return xCoord;
    }

    public String getyCoord() {
        return yCoord;
    }

    public String getDistance1() {
        return distance1;
    }

    public String getDistance2() {
        return distance2;
    }

    public String getDistance3() {
        return distance3;
    }

    public LogResult (String timestamp, String xCoord, String yCoord, String distance1, String distance2, String distance3) {
        this.timestamp = timestamp;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.distance3 = distance3;
    }

    @Override
    public boolean test(LogResult logResult) {
        return false;
    }
}
