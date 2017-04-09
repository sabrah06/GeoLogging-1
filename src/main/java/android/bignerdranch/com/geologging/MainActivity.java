package android.bignerdranch.com.geologging;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.firebase.client.Firebase;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Firebase reference;
    private Firebase mFirebaseRef;
    private DatabaseReference mDatabase;
    EditText mMessageEditText;

    private TextView mLog;
    private static final String TAG = "MainActivity";

    //set the Venue Id the floor map fo third floor from IndoorAtlas API
    private String Venue_Id="de97dcc9-da2a-4634-aa8b-e0ca8054d256";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    // blue dot radius in meters
    private static final float dotRadius = 1.0f;
    // Set the Point of interest to a constant to the coordinates of the door of 353 - floor 3
    private static Location PointofInterest = new Location("PointofInterest");

    private FirebaseAuth mAuth;

    private IALocationManager mIALocationManager;
    private IAResourceManager mFloorPlanManager;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    private IAFloorPlan mFloorPlan;
    private PositionDotView mImageView;
    private long mDownloadId;
    private DownloadManager mDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_map_view);

        //set the firebase context to the activity
        Firebase.setAndroidContext(this);

        //set the point of interest to the door of room 352 - floor 3
        PointofInterest.setLatitude(51.52225261374409);
        PointofInterest.setLongitude(-0.13083979995587666);

        // Reference to the Firebase storage
        mFirebaseRef = new Firebase("https://mybirkbeck-4ca74.firebaseio.com/");

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        // Gets the image view of the blue dot position tracking
        mImageView = (PositionDotView) findViewById(R.id.imageView);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mIALocationManager = IALocationManager.create(this);
        mFloorPlanManager = IAResourceManager.create(this);

        /* floor plan id
           if setLocation is not called, then location manager tries to find
           location automatically */
        final String floorPlanId = getString(R.string.indooratlas_floor_plan_id);
        if (!TextUtils.isEmpty(floorPlanId)) {
            final IALocation location = IALocation.from(IARegion.floorPlan(floorPlanId));
            mIALocationManager.setLocation(location);
        }

    }

    /*
    Gets the distance in meters between two geographical coordinates for the mokito unit test runner
     */
    public float GetCoords(Location location){
        IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                PointofInterest.getLatitude(), PointofInterest.getLongitude(), results);
        float distance = results[0];
        return distance;
    }

    /* Calculates the distance in meters between two geographical coordinates providing
    the latitude and longtitude
     */
    public float GetDistance(double pointofInterestLat, double pointofInterestLong, double myLocLat, double myLocLong){
        IALatLng MyLoclatLng = new IALatLng(myLocLat, myLocLong);
        IALatLng POIlatLng = new IALatLng(pointofInterestLat, pointofInterestLong);
        float[] results = new float[1];
        Location.distanceBetween(MyLoclatLng.latitude, MyLoclatLng.longitude,
                POIlatLng.latitude, POIlatLng.longitude, results);
        float distance = results[0];
        return distance;
    }

    /*
    Ideas and life cycle code taken from Indoor Atlas Examples
     Location changed code to track the coordinates
  */
    private IALocationListener mLocationListener = new IALocationListenerSupport() {
        @Override
        public void onLocationChanged(IALocation location) {
            // set the location coordinates along with time stamp for Firebase logging
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude() + ", time:" + currentDateandTime);

            Map<String,Object> values = new HashMap<>();
            values.put("time", currentDateandTime);
            values.put("latitutde", location.getLatitude());
            values.put("longtitue", location.getLongitude());
            mFirebaseRef.push().setValue(values);

            if (mImageView != null && mImageView.isReady()) {
                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
                PointF point = mFloorPlan.coordinateToPoint(latLng);
                mImageView.setDotCenter(point);
                mImageView.postInvalidate();
            }
            // calculate the distance from the chnaged location and Point of Interest
            Location checkLocation = new Location("ChangedLocation");
            checkLocation.setLatitude(location.getLatitude());
            checkLocation.setLongitude(location.getLongitude());
            float distance = GetCoords(checkLocation);
            // if distancce in metres is within 3 meters make a toast.
            if (distance <= 3){
                Toast.makeText(MainActivity.this, "You are within " + distance + " meters close to the point of interest", Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensurePermissions();
        // starts receiving location updates
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mLocationListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIALocationManager.removeLocationUpdates(mLocationListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        unregisterReceiver(onComplete);
    }

    private void ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                String id = region.getId();
                Log.d(TAG, "floorPlan changed to " + id);
                Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();
                fetchFloorPlan(id);
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            // leaving a previously entered region
        }

    };

    /*
    Set the radius of the image blue dot image tracking the position
     */
    private void showFloorPlanImage(String filePath) {
        Log.w(TAG, "showFloorPlanImage: " + filePath);
        mImageView.setRadius(mFloorPlan.getMetersToPixels() * dotRadius);
        mImageView.setImage(ImageSource.uri(filePath));
    }

    /*  Code from IndoorAtalas example
    * Broadcast receiver for floor plan image download
    * */
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                Log.w(TAG, "Ignore unrelated download");
                return;
            }
            Log.w(TAG, "Image download completed");
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = mDownloadManager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // process download
                    String filePath = c.getString(c.getColumnIndex(
                            DownloadManager.COLUMN_LOCAL_FILENAME));
                    showFloorPlanImage(filePath);
                }
            }
            c.close();
        }
    };

    /**
     * Code from IndoorAtlas example
     * Fetches floor plan data from IndoorAtlas server.
     * The third floor - BBK building
     */
    private void fetchFloorPlan(String id) {
        cancelPendingNetworkCalls();
        final IATask<IAFloorPlan> asyncResult = mFloorPlanManager.fetchFloorPlanWithId(id);
        mPendingAsyncResult = asyncResult;
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "fetch floor plan result:" + result);
                    if (result.isSuccess() && result.getResult() != null) {
                        mFloorPlan = result.getResult();
                        String fileName = mFloorPlan.getId() + ".img";
                        String filePath = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
                        File file = new File(filePath);
                        if (!file.exists()) {
                            DownloadManager.Request request =
                                    new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
                            request.setDescription("IndoorAtlas floor plan");
                            request.setTitle("Floor plan");
                            // requires android 3.2 or later to compile
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.
                                        Request.VISIBILITY_HIDDEN);
                            }
                            request.setDestinationInExternalPublicDir(Environment.
                                    DIRECTORY_DOWNLOADS, fileName);

                            mDownloadId = mDownloadManager.enqueue(request);
                        } else {
                            showFloorPlanImage(filePath);
                        }
                    } else {
                        // do something with error
                        if (!asyncResult.isCancelled()) {
                            Toast.makeText(MainActivity.this,
                                    (result.getError() != null
                                            ? "error loading floor plan: " + result.getError()
                                            : "access to floor plan denied"), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }

    private void cancelPendingNetworkCalls() {
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }
    }
}
