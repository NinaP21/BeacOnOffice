package com.example.beaconoffice;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import org.altbeacon.bluetooth.BluetoothMedic;
import java.util.ArrayList;
import java.util.Collections;

/**
 * BeacOnOffice is an application implemented during Aikaterini-Maria's internship in Dialog Semiconductor.
 * In a few words, BeacOnOffice reads all the data advertised by a specific AltBeacon device (WiRa initiator).
 * Using this data, it tries to specify the initiator's position inside the building of the company.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see HomeFragment
 * @see LogsFragment
 * @see InfoFragment
 * @see LogResult
 * @see LogsAdapter
 * @see ScanBeacons
 * @since 31/8/2022
 */

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BLUETOOTH_CONNECT = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH_SCAN = 2;
    public static final int REQUEST_COARSE_LOC_ENABLE = 3;

    private ArrayList<LogResult> logResultsList = new ArrayList<>();
    public LogsAdapter logsAdapter = new LogsAdapter(logResultsList, this);

    private ScanBeacons scanBeacons;
    private LogsFragment logs;
    private HomeFragment home = new HomeFragment();
    private InfoFragment info = new InfoFragment();

    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private Menu overflowMenu;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothMedic bluetoothMedic;
    private Context context;

    private Bundle bundle = new Bundle();

    private boolean hasStarted = false;
    private boolean hasPaused = false;
    private boolean hasAskedToStart = false;

    /**
     * Informs the user that the device's Bluetooth has been turned OFF.
     */
    private final BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    showSnackBar("Bluetooth is OFF");
                }
            }
        }
    };

    /**
     * Gets executed when the device's Bluetooth is disabled so the application asks the user
     * to enable it. If the user agrees and they have previously chosen to start the measurements,
     * then the program begins the procedure to start location tracking.
     * However, if the user doesn't agree to enable Bluetooth, then the application informs them
     * that this is necessary for BeacOnOffice's function.
     *
     * @see #getBlePermissions()
     */
    private ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        showSnackBar("Bluetooth access is necessary for the application");
                    } else if (hasAskedToStart)
                        startMeasurements();
                }
            });

    /**
     * Sets up the visual layout of the application as well as some initial configuration parameters.
     * Makes sure that device's Bluetooth Stack works properly
     * Then, it sets up the Bluetooth environment and the e-mail sending feature.
     *
     * @see ViewPagerAdapter
     * @see ScanBeacons
     * @see #robustBleStack()
     * @see #initialiseBle()
     * @see #setUpEmail()
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        logs = new LogsFragment(context);

        toolbar = findViewById(R.id.toolbar_layout);
        toolbar.setOverflowIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.add(home, "Home");
        viewPagerAdapter.add(logs, "Logs");
        viewPagerAdapter.add(info, "Info");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2); //less work

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        info.setContext(context);

        scanBeacons = new ScanBeacons(this, context);

        robustBleStack();
        initialiseBle();
        setUpEmail();
    }

    /**
     * Make the options from the toolbar menu appear with their icons apart from their text titles.
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        overflowMenu = menu;
        return true;
    }

    /**
     * Makes sure that the Bluetooth stack will work properly.
     * The continuous use of Beacon scanning may cause problems in Bluetooth stack,
     * but now the application will recover automatically by setting the Bluetooth power cycled.
     */
    public void robustBleStack() {
        bluetoothMedic = BluetoothMedic.getInstance();
        bluetoothMedic.enablePowerCycleOnFailures(this);
        bluetoothMedic.enablePeriodicTests(this, BluetoothMedic.SCAN_TEST);
    }

    /**
     * Sets up Bluetooth API variables for device's Bluetooth management
     */
    public void initialiseBle() {
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bleReceiver, filter);
    }

    /**
     * Makes the application's main thread irresponsible for any network access, such as the e-mail sending.
     * This means that e-mail sending will be executed in background.
     */
    public void setUpEmail() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * Starts, Pauses or Resets the measurements according to user's choice, in the main Toolbar.
     * Moreover, it can redirect the user to a Google Map that shows the last tracked location
     * of the WiRa initiator.
     *
     * @see #startMeasurements()
     * @see #resetMeasurements()
     * @see #pauseMeasurements()
     * @see MapsActivity
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                hasAskedToStart = true;
                startMeasurements();
                return true;
            case R.id.reset:
                resetMeasurements();
                return true;
            case R.id.pause:
                if (hasStarted) {
                    if (hasPaused) {
                        hasPaused = false;
                        item.setTitle("Pause tracking");
                    }
                    else {
                        hasPaused = true;
                        item.setTitle("Unpause tracking");
                    }
                    pauseMeasurements();
                } else
                    showSnackBar("Measurements have not started yet.");
                return true;
            case R.id.map:
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public HomeFragment getHomeFragment() {
        return home;
    }

    /**
     * Starts the measurements by reading data from the AltBeacon initiator.
     * Before that, it makes sure that the device has all the required permissions granted.
     * @see #checkPermissions()
     * @see #getPermissions()
     * @see ScanBeacons#scanAltBeacons()
     */
    public void startMeasurements() {
        if (!checkPermissions()) {
            getPermissions();
        } else {
            hasPaused = false;
            overflowMenu.getItem(1).setTitle("Pause tracking");
            scanBeacons.scanAltBeacons();
            showSnackBar("Measurements have started");
            hasStarted = true;
            Log.i("Measurements State", "----------------Measurements started----------------");
        }
    }

    /**
     * Checks if the application has access to the device's approximate location and if the Bluetooth is enabled.
     * @return true if all permissions are granted
     * @see #startMeasurements()
     */
    public boolean checkPermissions() {
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (bluetoothAdapter.isEnabled()));
    }

    /**
     * Makes sure that firstly the user will be asked to give the application access to the device's location
     * and then to check whether the Bluetooth is ON or OFF.
     * @see #getLocationPermissions()
     * @see #getBlePermissions()
     */
    public void getPermissions() {
        if (getLocationPermissions()) {
            getBlePermissions();
        }
    }

    /**
     * Checks if the application has access to the device's location and if not, it asks the user for that permission.
     * @return true if location access is granted
     *          otherwise it returns false
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    public boolean getLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOC_ENABLE);
            return false;
        }
        return true;
    }

    /**
     * Checks if the user has accepted to provide the application with location access.
     * If this permission was not granted, it informs the user that this is a necessary feature.
     * Otherwise, it goes on and makes sure that Bluetooth is enabled.
     * If all requirements are granted, it starts measurements.
     * @see #getLocationPermissions()
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_LOC_ENABLE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showSnackBar("Location access is necessary for the application");
                    hasAskedToStart = false;
                } else if (!bluetoothAdapter.isEnabled() && hasAskedToStart) {
                    getBlePermissions();
                } else if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) && (hasAskedToStart))
                    startMeasurements();
                break;
            case REQUEST_ENABLE_BLUETOOTH_CONNECT:
            case REQUEST_ENABLE_BLUETOOTH_SCAN:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showSnackBar("Something wrong happened in Bluetooth access");
                }
                break;
        }
    }

    /**
     * Checks if device's Bluetooth is enabled and if not, it asks the user to do so.
     * @see #onRequestPermissionsResult(int, String[], int[])
     * @see #onActivityResult(int, int, Intent)
     */
    public void getBlePermissions() {
        boolean bluetoothConnectNotGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED);
        boolean bluetoothScanNotGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED);
        if (!bluetoothAdapter.isEnabled()) {
            if (bluetoothConnectNotGranted || bluetoothScanNotGranted) {
                if (bluetoothConnectNotGranted)
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BLUETOOTH_CONNECT);
                if (bluetoothScanNotGranted)
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_BLUETOOTH_SCAN);
            }
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestPermissionLauncher.launch(intent);
        } else {
            startMeasurements();
        }
    }

    /**
     * Pauses or unpauses the measurements.
     * @see ScanBeacons#pauseAltBeacons()
     */
    public void pauseMeasurements() {
        bundle.putString("pause", "do");
        home.setArguments(bundle);
        scanBeacons.pauseAltBeacons();
    }

    /**
     * Resets the measurements' parameters in all program's classes.
     * @see ScanBeacons#resetAltBeacons()
     */
    public void resetMeasurements() {
        bundle.putString("reset", "do");
        home.setArguments(bundle);
        home.resetApplication();
        scanBeacons.resetAltBeacons();
        showSnackBar("BeacOnOffice has been reset");
        Log.i("Measurements State", "----------------Reset----------------");
        hasStarted = false;
        hasPaused = false;
        hasAskedToStart = false;
        overflowMenu.getItem(1).setTitle("Pause tracking");
    }

    /**
     * Clears the table of Measurement Results in "Logs" fragment.
     * @see HomeFragment#resetApplication()
     */
    public void clearLogsAdapter() {
        logResultsList.clear();
        logsAdapter.notifyDataSetChanged();
        logs.setAdapter(logsAdapter);
    }

    /**
     * Refreshes the table of Measurement Results' when a new position gets calculated.
     * @param logResult information about a single position estimation, that is shown to the user
     */
    public void addLogList(LogResult logResult) {
        logResultsList.add(logResult);
        Collections.reverse(logResultsList);
        logsAdapter.notifyDataSetChanged();
        logs.setAdapter(logsAdapter);
    }

    /**
     * Informs the user when something important has happened.
     * @param message the message shown to the user
     */
    public void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.myContainer), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Checks if the device is connected to the internet via WiFi of mobile network.
     * @return true if the device is connected to the internet and false if it is not
     */
    public boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null )
            return false;
        else return (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) && (networkInfo.getState() == NetworkInfo.State.CONNECTED) ||
                (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) && (networkInfo.getState() == NetworkInfo.State.CONNECTED);
    }
}

