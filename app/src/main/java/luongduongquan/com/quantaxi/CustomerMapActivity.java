package luongduongquan.com.quantaxi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout, btnRequest;

    private LatLng pickupLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costumer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mLogout = (Button) findViewById(R.id.logout_customer);
        btnRequest = (Button) findViewById(R.id.request_customer);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

                btnRequest.setText("Getting your Driver....");
                // Sau khi customer bấm tìm xe.
                // Mình sẽ dùng GeoFire để lên FireBase Database server và tìm ở bảng "driversAvailable" để lấy ra những driver gần nhất.
                getClosestDriver();
            }
        });
    }


    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    private void getClosestDriver(){
        // Mình sẽ dùng đệ quy để mỗi lần tìm trong vòng tròn bán kính 1 met. Nếu ko có, đệ quy lại và tăng bán kính lên 1 met nữa rồi tìm tiếp. Đến khi có driver mới thôi.
        // Dụa vào bảng "driversAvailable" dùng GeoFireQuery để tìm driver gần nhất.
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);

        // Tạo ra 1 đường tròn bán kính là radius (ở đây mình gán là 1 (km)). Tâm là điểm đứng hiện tại của user (pickupLocation).
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners(); // Xóa hết các listener để tránh bị lỗi trước đó.

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Call back này trả về khi có 1 driver nào đó trong bán kính tìm kiếm.
//                Log.d("QUAN123",  driverFound.toString() + " onKeyEntered - Radius = " + radius);
                if (!driverFound){
//                    Log.d("QUAN123", "onKeyEntered - Radius = " + radius);
//                    Log.d("QUAN123", "onKeyEntered - driverFoundID = " + key);
                    driverFound = true;
                    driverFoundID = key;

                    // Đẩy lên FireBase để xác định Driver này đang chở Customer với ID bao nhiu
//                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
//                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    HashMap map = new HashMap();
//                    map.put("customerRideId", customerId);
//                    driverRef.updateChildren(map);

//                    getDriverLocation();
//                    mRequest.setText("Looking for Driver Location....");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                // Call back trả về sau khi query trên FireBase đã chạy xong. Dù query có tìm thấy hay ko cũng sẽ nhảy vào callBack này.
//                Log.d("QUAN123",  driverFound.toString() + " onGeoQueryReady - Radius = " + radius);
                if (!driverFound)
                {
//                    Log.d("QUAN123", "onGeoQueryReady - Radius = " + radius);
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }



    @Override
    protected void onStop() {
        super.onStop();
    }
}
