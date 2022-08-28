package com.example.beaconoffice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private View popupViewCoords;
    private FrameLayout mapFrameLayout;
    private PopupWindow popupWindowCoords, popupWindowInfo;
    private TextView popupCoordsTextView, popupInfoTextView;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        mapFrameLayout = findViewById(R.id.map_frame);
        mapFrameLayout.getForeground().setAlpha(0);

        popupViewCoords = getLayoutInflater().inflate(R.layout.pop_up, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindowCoords = new PopupWindow(popupViewCoords, width, height, true);
        popupCoordsTextView = popupViewCoords.findViewById(R.id.popup_text);

        toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gmap);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.map_info)  {
            String infoText = "Your position has not been estimated\nby the device's GPS location access.\n\nBeacOnOffice uses only data\ncollected from WiRa AltBeacons.";
            setPopUp(infoText);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i("*********************************************************", "//////////////////////////////////////  " + HomeFragment.currentCoordinates.equals(null));
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        if (HomeFragment.currentCoordinates == null) {
            Log.i("*********************************************************", "--------------------------------");
            latitude = 5.558562;
            longitude = -0.200923;
            LatLng accra = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(accra));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(1));

            String message = "There is not any\navailable location yet";
            setPopUp(message);
            return ;
        }


        latitude = HomeFragment.currentCoordinates[0];
        longitude = HomeFragment.currentCoordinates[1];
        LatLng currentPosistion = new LatLng(latitude, longitude);
        String marketText = "Latitude: " + latitude + "\nLongitude: " + longitude;
        googleMap.addMarker(new MarkerOptions().position(currentPosistion));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosistion));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                popupWindowCoords.update();
                popupWindowCoords.setElevation(20);
                popupWindowCoords.showAtLocation(findViewById(R.id.map_frame), Gravity.NO_GRAVITY, findViewById(R.id.map_frame).getLeft() + 50, findViewById(R.id.map_frame).getBottom() - 150);
                popupCoordsTextView.setText(marketText);
                return false;
            }
        });
    }

    public void setPopUp(String text) {

        View popupViewInfo = getLayoutInflater().inflate(R.layout.info_pop_up, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        popupWindowInfo = new PopupWindow(popupViewInfo, width, height, true);
        popupInfoTextView = popupViewInfo.findViewById(R.id.info_popup_text);
        popupWindowInfo.setElevation(20);
        popupWindowInfo.setAnimationStyle(R.style.Animation);
        popupInfoTextView.setText(text);

        popupWindowInfo.showAtLocation(findViewById(R.id.map_frame), Gravity.CENTER, 0, 0);
        mapFrameLayout.getForeground().setAlpha(150);

        popupViewInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindowInfo.dismiss();
                return true;
            }
        });

        popupWindowInfo.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mapFrameLayout.getForeground().setAlpha(0);
            }
        });
    }
}