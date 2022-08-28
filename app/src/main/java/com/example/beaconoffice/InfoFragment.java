package com.example.beaconoffice;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * The InfoFragment class creates the "Info" page, which presents to the user
 * some of the most important characteristics of the Android device and the WiRa initiator,
 * that the application reads data from.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @since 31/8/2022
 */
public class InfoFragment extends Fragment {

    private TableLayout deviceInfo;
    private Context context;

    /**
     * Default constructor
     */
    public InfoFragment() {  }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the basic visual layout of the "Info" fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    /**
     * Creates two tables. The first one informs the user about the current device's characteristics,
     * like the Android version. The second table shows some specific information about the WiRa AltBeacon initiator
     * used by the application.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        deviceInfo = view.findViewById(R.id.infoTable);

        /* --------------------------------------------------------------------------------------------------- */

        TableRow androidTitle = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_title, null);
        ((TextView) androidTitle.findViewById(R.id.table_title)).setText(R.string.android_table);
        deviceInfo.addView(androidTitle);

        TableRow row = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row.findViewById(R.id.table_title)).setText("Version Release");
        ((TextView) row.findViewById(R.id.attribute_value)).setText(Build.VERSION.RELEASE);
        deviceInfo.addView(row);

        TableRow row1 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row1.findViewById(R.id.table_title)).setText("Version Incremental");
        ((TextView) row1.findViewById(R.id.attribute_value)).setText(Build.VERSION.INCREMENTAL);
        deviceInfo.addView(row1);

        TableRow row2 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row2.findViewById(R.id.table_title)).setText("Version SDK number");
        ((TextView) row2.findViewById(R.id.attribute_value)).setText(Integer.toString(Build.VERSION.SDK_INT));
        deviceInfo.addView(row2);

        TableRow row3 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row3.findViewById(R.id.table_title)).setText("Brand and Model");
        ((TextView) row3.findViewById(R.id.attribute_value)).setText(Build.BRAND + " " + Build.MODEL);
        deviceInfo.addView(row3);

        TableRow row4 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row4.findViewById(R.id.table_title)).setText("Board");
        ((TextView) row4.findViewById(R.id.attribute_value)).setText(Build.BOARD);
        deviceInfo.addView(row4);

        TableRow row5 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row5.findViewById(R.id.table_title)).setText("Device");
        ((TextView) row5.findViewById(R.id.attribute_value)).setText(Build.DEVICE);
        deviceInfo.addView(row5);

        /* --------------------------------------------------------------------------------------------------- */

        TableRow beaconTitle = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_title, null);
        ((TextView) beaconTitle.findViewById(R.id.table_title)).setText("WiRa Beacon");
        beaconTitle.setPadding(0, 100, 0, 0);
        deviceInfo.addView(beaconTitle);

        TableRow row6 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row6.findViewById(R.id.table_title)).setText("Board Product Family");
        ((TextView) row6.findViewById(R.id.attribute_value)).setText("DA1469x-00");
        deviceInfo.addView(row6);

        TableRow row7 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row7.findViewById(R.id.table_title)).setText("Device Address");
        ((TextView) row7.findViewById(R.id.attribute_value)).setText("48:23:35:00:00:AA");
        deviceInfo.addView(row7);

        TableRow row8 = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row, null);
        ((TextView) row8.findViewById(R.id.table_title)).setText("Number of Connections");
        ((TextView) row8.findViewById(R.id.attribute_value)).setText("3");
        deviceInfo.addView(row8);
    }
}