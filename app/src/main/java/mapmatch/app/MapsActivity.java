package mapmatch.app;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private ArrayList<Marker> markers;
    int boundary;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment.getMapAsync(this);
        markers = new ArrayList<Marker>();
        boundary = 3;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        System.out.println("Started");

        if (!checkPermissions()) {
            System.out.println("Requesting permissions");
            requestPermissions();
        } else {
            System.out.println("Getting user location...");
            getUserLocation();
            System.out.println("Got user location");
        }

    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            System.out.println("Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });

        } else {
            System.out.println("Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnMapLongClickListener(this);
            mMap.setOnInfoWindowClickListener(this);
        }


    }

    public void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            System.out.println("latitude is " + location.getLatitude() + " and longitude is " + location.getLongitude());
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("User is here!"));
                            markers.add(marker);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                            // Logic to handle location object
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed to get location");
                    }
                });
    }

    public int isInBoundary(LatLng latLng) {
        for (int i = 0; i < markers.size(); i++) { //check if any of the markers are within the boundary
            System.out.println("(latLng.latitude - markers.get(i).getPosition().latitude) is " + (latLng.latitude - markers.get(i).getPosition().latitude));
            System.out.println("(latLng.longitude - markers.get(i).getPosition().longitude) is " + (latLng.longitude - markers.get(i).getPosition().longitude));

            if ((latLng.latitude - markers.get(i).getPosition().latitude) < boundary && //if within boundary
                    (latLng.latitude - markers.get(i).getPosition().latitude) > -boundary &&
                    (latLng.longitude - markers.get(i).getPosition().longitude) < boundary &&
                    (latLng.longitude - markers.get(i).getPosition().longitude) > -boundary) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        System.out.println("LongClicked");
        int markerIndex = isInBoundary(latLng);

        if (markerIndex != -1) {
            System.out.println("About to remove");
            markers.get(markerIndex).remove();
            markers.remove(markerIndex);
            Toast.makeText(MapsActivity.this, "Removed Marker", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(MapsActivity.this, "Latitude: " + marker.getPosition().latitude + ", Longitude: " + marker.getPosition().longitude, Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
}
