package luongduongquan.com.quantaxi;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import luongduongquan.com.quantaxi.Utils.Common;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	private GoogleMap mMap;
	private GoogleApiClient googleApiClient;
	private Location lastLocation;
	LocationRequest locationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driver_map);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		buildGoogleApiClient();
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		mMap.setMyLocationEnabled(true);

//		// Add a marker in Sydney and move the camera
//		LatLng sydney = new LatLng(-34, 151);
//		mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {

		locationRequest = new LocationRequest();
		locationRequest.setInterval(1000);
		locationRequest.setFastestInterval(1000);
		locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
		LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference driverAvailebleReference = FirebaseDatabase.getInstance().getReference().child(Common.DRIVER_AVAILABLE_TAG);

		GeoFire geoFire = new GeoFire(driverAvailebleReference);
		geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
			@Override
			public void onComplete(String key, DatabaseError error) {
				// nothing to do
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();

		String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		DatabaseReference driverAvailebleReference = FirebaseDatabase.getInstance().getReference().child(Common.DRIVER_AVAILABLE_TAG);

		GeoFire geoFire = new GeoFire(driverAvailebleReference);
		geoFire.removeLocation(userID);
	}

	protected synchronized void buildGoogleApiClient(){
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		googleApiClient.connect();
	}

}
