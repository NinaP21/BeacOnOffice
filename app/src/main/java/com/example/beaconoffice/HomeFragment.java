package com.example.beaconoffice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;

import org.altbeacon.beacon.Beacon;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * HomeFragment class creates the Home page that is the most important page of the application.
 * This page consists of a canvas (rectangle 21m x 38m) that represents a part of the company's office inside which the
 * WiRa initiator position will be tracked.
 * In order to calculate this position, the application reads the data from the advertised packets
 * of a specific AltBeacon; the WiRa initiator. Then, it implements a mathematical algorithm that
 * extracts the Beacon's position inside the company's office.
 * The small circles inside the building canvas are the fixed positions of some AltBeacons that the
 * initiator uses in order to construct its advertisement packet.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @since 31/8/2022
 */
public class HomeFragment extends Fragment {

    private final double[][] ALTBEACONS =  {
        {0, 0}, {6, 0}, {5, 4}, {13.8, 0}, {17.9, 4.3}, {5, 10.8}, {5, 19}, {0.6, 14.5}, {11, 19.8}, {0, 21.9}, {5, 26.9}, {10.6, 32.6}
    };
    private final double[] fixedCoordinates = {38.260258, 21.748722};
    public static Double[] currentCoordinates = {null, null};

    private final float xTotalMeters = (float) 21;
    private final float yTotalMeters = (float) 38;
    private static float xCoordMax, yCoordMax;
    private static float xPixelsPerMeter, yPixelsPerMeter;
    private static float xMetersPerPixel, yMetersPerPixel;
    private String wholeBody;

    private final int delay = 3000;

    private final String TAG = "homefragment DEBUG";

    private DecimalFormat decimalFormat = new DecimalFormat();
    private MainActivity mainActivity;
    private View popupView;
    private PopupWindow popupWindow;
    private TextView popupTextView;
    private View.OnClickListener listener;
    private Handler handler = new Handler();
    private RelativeLayout relativeLayout;
    private ImageView person;
    private ImageView invalidPerson;
    private SimpleDateFormat simpleDateFormat;
    private String currentDateAndTime;
    private LogResult newLog;
    private MapView mapView;
    private RectF office1 = new RectF();
    private RectF office2 = new RectF();
    private RectF office3 = new RectF();
    private Double tmpX, tmpY;
    private Double xMeterCoord, yMeterCoord;
    private String reset;
    private Bundle logsBundle = new Bundle();
    public static boolean[] active = new boolean[12];
    private static String emailBody;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            popupWindow.dismiss();
        }
    };

    /**
     * Default constructor
     */
    public HomeFragment() { }

    /**
     * Creates the visual layout of the Home page that consists of the representation of the building,
     * the icon that corresponds to the initiator's position and a popup window that may appear when an AltBeacon
     * or the initiator's icon get touched, showing the coordinates of that particular object.
     *
     * @see MapView
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        popupView = inflater.inflate(R.layout.pop_up, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, true);
        popupTextView = popupView.findViewById(R.id.popup_text);

        relativeLayout = view.findViewById(R.id.ofice_canvas);
        mapView = new MapView(mainActivity);
        relativeLayout.addView(mapView);

        person = view.findViewById(R.id.person);
        person.setImageResource(R.drawable.aim2);
        person.bringToFront();

        invalidPerson = view.findViewById(R.id.invalid_person);
        invalidPerson.setImageResource(R.drawable.invalid);
        invalidPerson.bringToFront();
        invalidPerson.setVisibility(View.GONE);

        person.setX(50);
        person.setY(250);

        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.update();
                popupWindow.setElevation(20);
                popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, mapView.getLeft() + 50, mapView.getBottom() + 200);
                popupTextView.setText("x: " + decimalFormat.format(person.getX() * xMetersPerPixel) + "\n" + "y: " + decimalFormat.format(person.getY() * yMetersPerPixel));

                handler.postDelayed(runnable, delay);
            }
        };

        person.setOnClickListener(listener);
        invalidPerson.setOnClickListener(listener);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
    }

    /**
     * Checks if the physical coordinates (in meters) of the initiator are valid, which means that
     * they are inside the depicted part of the building and they are not inside the closed offices (in grey color).
     * Then, computes the pixel coordinates of the initiator based on its physical coordinates in meters.
     * If the coordinates are invalid, then the initiator's icon is a black one with an exclamation mark in it,
     * showing that the calculated position is practically wrong.
     * Then, another function is called to calculate the GPS coordinates of the WiRa device,
     * based on these relative cartesian ones.
     * This function also adds the newly generated data to the string of the potential e-mail message and
     * also to the list that holds the data of the "Measurement Results" table in Logs page.
     * After all this, the canvas that represents the building gets updated.
     *
     * @param coordinates the coordinates of the initiator in meters, calculated by the mathematical algorithm
     * @param distances the distances of the initiator from the three AltBeacons
     *                  which it used in order to compute the coordinates
     *
     * @see MapView
     * @see ScanBeacons#addBeaconValues(Beacon)
     * @see MapView#updateCanvas()
     * @see #calculateGpsCoordinates(double, double)
     */
    public void receiveCoords(double[] coordinates, ArrayList<String> distances) {

        float xPixelCoord, yPixelCoord;

        tmpX = coordinates[0];
        tmpY = coordinates[1];

        xMeterCoord = tmpX;
        yMeterCoord = tmpY;

        xPixelCoord = xMeterCoord.floatValue() * xPixelsPerMeter - 20;
        yPixelCoord = yMeterCoord.floatValue() * yPixelsPerMeter - 20;

        boolean invalidCoords = (yMeterCoord < 0) || (xMeterCoord < 0) || (yMeterCoord > yTotalMeters) || (xMeterCoord > xTotalMeters) ||
                ((yMeterCoord >= 4.3) && (yMeterCoord <= (4.3 + 10.8)) && (xMeterCoord >= 10)) || // Meeting rooms
                ((yMeterCoord >= (4.3 + 10.8)) && (xMeterCoord >= (10 + 2.35))) || // Kitchen
                ((yMeterCoord >= 30) && (xMeterCoord <= 7.6)); // Hardware Lab

        if (invalidCoords) {
            if ((yMeterCoord >= (4.3 + 10.8)) && (xMeterCoord >= (10 + 2.35))) {
                xMeterCoord = 10 + 2.35;
            }
            if (yMeterCoord < 0) {
                yMeterCoord = 0.0;
            }
            else if (yMeterCoord > yTotalMeters) {
                yMeterCoord = (double) yTotalMeters ;
            }
            if (xMeterCoord < 0) {
                xMeterCoord = 0.0;
            }
            else if (xMeterCoord > xTotalMeters) {
                xMeterCoord = (double) xTotalMeters;
            }

            xPixelCoord = xMeterCoord.floatValue() * xPixelsPerMeter - 20;
            yPixelCoord = yMeterCoord.floatValue() * yPixelsPerMeter - 20;
            person.setVisibility(View.GONE);
            invalidPerson.setVisibility(View.VISIBLE);
            invalidPerson.setX(xPixelCoord);
            invalidPerson.setY(yPixelCoord);
        } else {
            person.setVisibility(View.VISIBLE);
            person.setX(xPixelCoord);
            person.setY(yPixelCoord);
            invalidPerson.setVisibility(View.GONE);
        }

        calculateGpsCoordinates(xMeterCoord, yMeterCoord);

        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        currentDateAndTime = simpleDateFormat.format(new Date());
        if (emailBody != null) {
            emailBody = emailBody.concat(currentDateAndTime + "    " + "x: " + decimalFormat.format(xMeterCoord) + "m    " + "y: " + decimalFormat.format(yMeterCoord) + "m\n");
        }
        else {
            emailBody = currentDateAndTime + "    " + "x: " + decimalFormat.format(xMeterCoord) + "m    " + "y: " + decimalFormat.format(yMeterCoord) + "m\n";
        }

        if (distances.size() == 3) {
            String distance1 = distances.get(0) + " m";
            String distance2 = distances.get(1) + " m";
            String distance3 = distances.get(2) + " m";
            newLog = new LogResult(currentDateAndTime, decimalFormat.format(xMeterCoord)+" m", decimalFormat.format(yMeterCoord)+" m", distance1, distance2, distance3);
            mainActivity.addLogList(newLog);
            emailBody = emailBody.concat(distance1 + "m   ").concat(distance2 + "m   ").concat(distance3 + "m\n\n");
            getParentFragmentManager().setFragmentResult("logResult", logsBundle);
            Log.i("Pixel - Coordinate X of person", "   " + decimalFormat.format(xPixelCoord));
            Log.i("Pixel - Coordinate Y of person", "   " + decimalFormat.format(yPixelCoord));
            mapView.updateCanvas();
        }
    }

    /**
     * According to the WiRa initiator's cartesian coordinates inside the office,
     * the function applies an algorithm to calculate the corresponding
     * GPS coordinates of the current position.
     * 
     * @param x the x-coordinate of the WiRa initiator inside the office
     * @param y the y-coordinate of the WiRa initiator inside the office
     *
     * @see #receiveCoords(double[], ArrayList) 
     * @see MapsActivity#onMapReady(GoogleMap)
     */
    public void calculateGpsCoordinates (double x, double y) {
        final double earthRadius = 6371010;
        final double epsilon = 0.000001;

        double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        Log.i("Distance between (0, 0) and current point ", "   " + distance);

        double theta = Math.atan2(y, x);
        theta = theta + Math.PI/2;
        Log.i("Bearing between (0, 0) and current point ", "   " + radToDeg(theta) + " degrees");

        double radLatitude = degToRad(fixedCoordinates[0]);
        double radLongitude = degToRad(fixedCoordinates[1]);
        double radDistance = distance/earthRadius;

        double latitude = Math.asin(Math.sin(radLatitude) * Math.cos(radDistance) + Math.cos(radLatitude) * Math.sin(radDistance) * Math.cos(theta));
        double longitude;

        if ( Math.cos(radLatitude) == 0 || Math.abs( Math.cos(radLatitude) ) < epsilon ) {
            longitude = radLongitude;
        } else {
            longitude = ( (radLongitude - Math.asin( Math.sin(theta) * Math.sin(radDistance) / Math.cos(latitude)) + Math.PI ) % (2 * Math.PI) - Math.PI );
        }

        latitude = radToDeg(latitude);
        longitude = radToDeg(longitude);

        currentCoordinates[0] = latitude;
        currentCoordinates[1] = longitude;

        Log.i("GPS latitude of new point ", "   " + latitude + " degrees");
        Log.i("GPS longitude of new point ", "   " + longitude + " degrees");
    }

    public double radToDeg(double angle) {
        return angle * 180 / Math.PI;
    }

    public double degToRad(double angle) {
        return angle * Math.PI / 180;
    }

    /**
     * Creates the complete text of the e-mail body by combining a salutation to the user,
     * the tracked data as shown in "Measurement Results" table and a greeting from the BeacOnOffice team.
     * If there is no data to show, the email informs the user about that.
     *
     * @return the complete string of the email body
     * @see LogsFragment#sendEmail()
     */
    public String getEmailBody() {
        wholeBody = "Dear user,\n\n";
        if (emailBody != null )
            wholeBody = wholeBody.concat("The Log results of BeacOnOffice measurements are:\n").concat(emailBody);
        else
            wholeBody = wholeBody.concat("There are no Log results to show");

        wholeBody = wholeBody.concat("\n\nRegards, \nthe BeacOnOffice Team");
        return wholeBody;
    }

    /**
     * Updates the scale used to match meters in pixels and reverse, according to
     * the real dimensions of the building and the total pixels of the current Android device's screen.
     *
     * @see MapView#setBeacons()
     */
    public void updateScale() {
        xPixelsPerMeter = xCoordMax/xTotalMeters;
        yPixelsPerMeter = yCoordMax/yTotalMeters;
        xMetersPerPixel = xTotalMeters/xCoordMax;
        yMetersPerPixel = yTotalMeters/yCoordMax;
    }

    /**
     * MapView class constructs the visual representation of the building, in which the experiment takes place.
     * The main objects that constitute the canvas are the closed offices, the AltBeacons that have fixed
     * positions inside the building and some bitmap pictures that depict the cubicles or
     * show that an area is prohibited, like the closed offices.
     */
    public class MapView extends View {

        private Paint officePaint = new Paint();
        private Paint officeLinesPaint = new Paint();
        private Bitmap workspace, prohibitionBitmap;
        private BitmapDrawable prohibition;
        private GradientDrawable[] beacon = new GradientDrawable[12];
        private int[] orangeColors = {Color.parseColor("#ffd700"), Color.parseColor("#da9100")};
        private int[] greenColors = {Color.parseColor("#AEF359"), Color.parseColor("#028A0F")};

        /**
         * Makes the current canvas invalid, so it gets recreated according to the most recent specifications.
         */
        public void updateCanvas() {
            invalidate();
        }

        /**
         * Sets the border and the internal color of the closed offices, that will be grayish.
         */
        public void setOffices() {
            officePaint.setColor(Color.parseColor("#dbdbdb"));
            officePaint.setStyle(Paint.Style.FILL);

            officeLinesPaint.setColor(Color.parseColor("#b5b5b5"));
            officeLinesPaint.setStyle(Paint.Style.STROKE);
            officeLinesPaint.setStrokeWidth(5);
        }

        /**
         * Updates the scale that it will use. Then, it declares the AltBeacon icons as
         * small circles with gradient color style. Each of these circles will have fixed position
         * according to the corresponding AltBeacon's coordinates.
         */
        public void setBeacons() {

            updateScale();

            for (int i = 0; i < 12; i++) {
                beacon[i] = (GradientDrawable) getResources().getDrawable(R.drawable.circle, null);
                beacon[i].setShape(GradientDrawable.OVAL);
                beacon[i].setGradientType(GradientDrawable.LINEAR_GRADIENT);
            }

            beacon[0].setBounds(getLeft() + 10, getTop() + 10, getLeft() + 10 + 40, getTop() + 10 + 40);
            beacon[1].setBounds((int) (ALTBEACONS[1][0] * xPixelsPerMeter) - 20, getTop() + 10, (int) (ALTBEACONS[1][0] * xPixelsPerMeter) - 20 + 40, getTop() + 10 + 40);
            beacon[2].setBounds((int) (ALTBEACONS[2][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[2][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[2][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[2][1] * yPixelsPerMeter) - 20 + 40);
            beacon[3].setBounds((int) (ALTBEACONS[3][0] * xPixelsPerMeter) - 20, getTop() + 10, (int) (ALTBEACONS[3][0] * xPixelsPerMeter) - 20 + 40, getTop() + 10 + 40);
            beacon[4].setBounds((int) (ALTBEACONS[4][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[4][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[4][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[4][1] * yPixelsPerMeter) - 20 + 40);
            beacon[5].setBounds((int) (ALTBEACONS[5][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[5][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[5][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[5][1] * yPixelsPerMeter) - 20 + 40);
            beacon[6].setBounds((int) (ALTBEACONS[6][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[6][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[6][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[6][1] * yPixelsPerMeter) - 20 + 40);
            beacon[7].setBounds((int) (ALTBEACONS[7][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[7][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[7][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[7][1] * yPixelsPerMeter) - 20 + 40);
            beacon[8].setBounds((int) (ALTBEACONS[8][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[8][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[8][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[8][1] * yPixelsPerMeter) - 20 + 40);
            beacon[9].setBounds(getLeft() + 10, (int) (ALTBEACONS[9][1] * yPixelsPerMeter) - 20, getLeft() + 10 + 40, (int) (ALTBEACONS[9][1] * yPixelsPerMeter) - 20 + 40);
            beacon[10].setBounds((int) (ALTBEACONS[10][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[10][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[10][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[10][1] * yPixelsPerMeter) - 20 + 40);
            beacon[11].setBounds((int) (ALTBEACONS[11][0] * xPixelsPerMeter) - 20, (int) (ALTBEACONS[11][1] * yPixelsPerMeter) - 20, (int) (ALTBEACONS[11][0] * xPixelsPerMeter) - 20 + 40, (int) (ALTBEACONS[11][1] * yPixelsPerMeter) - 20 + 40);

        }

        /**
         * Class constructor.
         * Initiates the process to set the form of the AltBeacons and the closed offices.
         * It also declares the bitmap pictures for the cubicles and the prohibited place sign.
         */
        public MapView(Context context) {
            super(context);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            setBeacons();
            setOffices();

            workspace = BitmapFactory.decodeResource(getResources(), R.drawable.workspace);
            prohibitionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.prohibition);
            prohibition = new BitmapDrawable(getResources(), prohibitionBitmap);
            prohibition.setAlpha(20);
        }

        /**
         * Displays a popup window when the user touches one of the auxiliary AltBeacons.
         * This popup window will be shown for 3 seconds and it will contain the number of the
         * AltBeacon and its coordinates in meters.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();

            popupWindow.setElevation(20);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < 12; i++) {
                    if (beacon[i].getBounds().contains((int) touchX, (int) touchY)) {
                        //Log.i("Beacon touched!!!", " beacon " + i + " was touched");
                        handler.removeCallbacks(runnable);
                        popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, getLeft() + 50, getBottom() + 200 );
                        int beaconNumber = i + 1;
                        double beaconX = ALTBEACONS[i][0];
                        double beaconY = ALTBEACONS[i][1];
                        popupTextView.setText("Beacon " + beaconNumber + "\n" + "(" + beaconX + ", " + beaconY + ")");
                        handler.postDelayed(runnable, delay);
                        break;
                    }
                }
            }
            return true;
        }

        /**
         * Draws the visual representation of the building.
         * First, it places the cubicle icons, then the closed offices and the prohibition icons.
         * Finally, it checks every fixed AltBeacon and if one contributes to the application's
         * calculation, then it gets drawn in green color. Otherwise, it will be yellowish.
         *
         * @see #setBeacons()
         * @see #setOffices()
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //----------------------------Desks----------------------------
            //Desk 1
            canvas.drawBitmap(workspace, 20, 10, null);
            canvas.drawBitmap(workspace, 150, 10, null);
            canvas.drawBitmap(workspace, 280, 10, null);
            canvas.drawBitmap(workspace, 410, 10, null);
            canvas.drawBitmap(workspace, 540, 10, null);
            canvas.drawBitmap(workspace, 670, 10, null);
            canvas.drawBitmap(workspace, 800, 10, null);
            //canvas.drawBitmap(workspace, 900, 10, null);

            //Desk 2
            canvas.drawBitmap(workspace, 20, (float) (ALTBEACONS[2][1] * yPixelsPerMeter - 40), null);
            canvas.drawBitmap(workspace, 150, (float) (ALTBEACONS[2][1] * yPixelsPerMeter - 40), null);

            //Desk 3
            canvas.drawBitmap(workspace, 20, (float) (ALTBEACONS[5][1] * yPixelsPerMeter - 40), null);
            canvas.drawBitmap(workspace, 150, (float) (ALTBEACONS[5][1] * yPixelsPerMeter - 40), null);

            //Desk 4
            canvas.drawBitmap(workspace, 20, (float) (ALTBEACONS[6][1] * yPixelsPerMeter - 40), null);
            canvas.drawBitmap(workspace, 150, (float) (ALTBEACONS[6][1] * yPixelsPerMeter - 40), null);

            //Desk 5
            canvas.drawBitmap(workspace, 20, (float) (ALTBEACONS[10][1] * yPixelsPerMeter - 40), null);
            canvas.drawBitmap(workspace, 150, (float) (ALTBEACONS[10][1] * yPixelsPerMeter - 40), null);

            //Desk 6
            canvas.drawBitmap(workspace, (float) (ALTBEACONS[8][0] * xPixelsPerMeter), (float) (ALTBEACONS[8][1] * yPixelsPerMeter - 40), null);

            //Desk 7
            canvas.drawBitmap(workspace, (float) (ALTBEACONS[11][0] * xPixelsPerMeter), (float) (ALTBEACONS[11][1] * yPixelsPerMeter - 40), null);

            //----------------------------Offices----------------------------

            office1.set(10 * xPixelsPerMeter, (float) (4.3 * yPixelsPerMeter), getRight() - 10, (float) (4.3 + 10.8) * yPixelsPerMeter);
            canvas.drawRect(office1, officePaint);
            canvas.drawRect(office1, officeLinesPaint);

            prohibition.setBounds((int) (getRight() - 10 - 11 * xPixelsPerMeter + 90), (int) ((4.3 + 5.4) * yPixelsPerMeter - 200), (int) (getRight() - 10 - 11 * xPixelsPerMeter + 400 + 90), (int) ((4.3 + 5.4) * yPixelsPerMeter) - 200 + 400);
            prohibition.draw(canvas);

            office2.set((float) (12.35 * xPixelsPerMeter), (float) (15.1 * yPixelsPerMeter), getRight() - 10, (float) (38 * yPixelsPerMeter) - 10);
            canvas.drawRect(office2, officePaint);
            canvas.drawRect(office2, officeLinesPaint);

            prohibition.setBounds((int) ((12.35 + 4.325) * xPixelsPerMeter - 200), (int)((15.1 + 11.45) * yPixelsPerMeter - 200), (int) ((12.35 + 4.325) * xPixelsPerMeter - 200 + 400), (int)((15.1 + 11.45) * yPixelsPerMeter - 200 + 400));
            prohibition.draw(canvas);

            office3.set((float) (getLeft() + 10), 30 * yPixelsPerMeter, (float) (getLeft() + 10 + 7.6 * xPixelsPerMeter), (float) (getBottom() - 10));
            canvas.drawRect(office3, officePaint);
            canvas.drawRect(office3, officeLinesPaint);

            prohibition.setBounds((int)(getLeft() + 10 + 3.8 * xPixelsPerMeter - 100), (int)(34 * yPixelsPerMeter - 100), (int) (getLeft() + 10 + 3.8 * xPixelsPerMeter + 100), (int)(34 * yPixelsPerMeter + 100));
            prohibition.draw(canvas);

            //----------------------------Beacons----------------------------
            for (int k = 0; k < active.length; k++) {
                if (active[k]) {
                    beacon[k].setColors(greenColors);
                } else {
                    beacon[k].setColors(orangeColors);
                }
                beacon[k].draw(canvas);
            }
        }

        /**
         * As soon as the layout parameters get acknowledged, set the maximum values
         * of pixels in both width and height. Then, the application is ready to
         * define the AltBeacons' positions and draw the building.
         *
         * @see #setBeacons()
         * @see #updateCanvas()
         */
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);

            xCoordMax = this.getWidth();
            yCoordMax = this.getHeight();
            setBeacons();
            updateCanvas();
        }
    }

    /**
     * Gets executed when the user has clicked the "reset" option in the main toolbar.
     * Then, the icon of the initiator gets back to its initial position with its default bitmap picture
     * and the "Measurement Results" table gets cleared. Moreover, the AltBeacons become
     * all yellow (inactive), as in the beginning.
     *
     * @see MainActivity#resetMeasurements()
     * @see MainActivity#clearLogsAdapter()
     * @see MapView#updateCanvas()
     */
    public void resetApplication() {
        reset = getArguments().getString("reset");
        if (reset.equals("do")) {
            if (person == null)
                Log.i(TAG, "Person object is null");
            else {
                wholeBody = null;
                if (person.getVisibility() == View.GONE) {
                    invalidPerson.setVisibility(View.GONE);
                    person.setVisibility(View.VISIBLE);
                }
                person.setX(50);
                person.setY(250);
            }
            mainActivity.clearLogsAdapter();
            Arrays.fill(active, false);
            mapView.updateCanvas();
            currentCoordinates = new Double[]{null, null};
        }
    }
}