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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout;

    private String customerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedCustomer();

    }

    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                	if (dataSnapshot.hasChild("customerRideId")){
						Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
						if(map.get("customerRideId") != null){
							customerId = map.get("customerRideId").toString();
							getAssignedCustomerPickupLocation();
						}
					}
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedCustomerPickupLocation(){
        DatabaseReference assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    mMap.addMarker(new MarkerOptions().position(driverLatLng).title("pickup location"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
    	// Cứ mỗi 10000ms nó sẽ nhảy vô đây 1 lần. (do  mLocationRequest.setInterval(10000); quy định)

		if(getApplicationContext()!=null){
			mLastLocation = location;
			LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

			String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
			DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable"); // driversAvailable : driver đang rãnh
			DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking"); // driver đang chở khách
			GeoFire geoFireAvailable = new GeoFire(refAvailable);
			GeoFire geoFireWorking = new GeoFire(refWorking);

			switch (customerId){
				case "":
					// Khi driver ko có khách
					geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
						@Override
						public void onComplete(String key, DatabaseError error) {

						}
					});
					geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
						@Override
						public void onComplete(String key, DatabaseError error) {

						}
					});
					break;

				default:
					// Khi driver đang bận chở khách
					geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
						@Override
						public void onComplete(String key, DatabaseError error) {

						}
					});
					geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
						@Override
						public void onComplete(String key, DatabaseError error) {

						}
					});
					break;
			}
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
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
        if(mGoogleApiClient != null){
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		}
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
			@Override
			public void onComplete(String key, DatabaseError error) {

			}
		});
    }
}
