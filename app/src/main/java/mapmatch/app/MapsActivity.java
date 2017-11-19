package mapmatch.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private Map<Marker, User> markers;
    int boundary;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    User currUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment.getMapAsync(this);
        markers = new HashMap<Marker, User>();
        boundary = 3;
        currUser = LoginActivity.getCurrUser();

        new FindPeople().execute(currUser); //email, gender, movies, music, interests



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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                seeProfile();
                return (true);
            default:
                return super.onOptionsItemSelected(item);
            }
    }

    private void seeProfile() {
        Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
        startActivity(intent);
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
        System.out.println("OnMapReady now");
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
                            //Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("User is here!"));
                            //markers.add(marker);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.5f));
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

//    public int isInBoundary(LatLng latLng) {
//        for (int i = 0; i < markers.size(); i++) { //check if any of the markers are within the boundary
//            System.out.println("(latLng.latitude - markers.get(i).getPosition().latitude) is " + (latLng.latitude - markers.get(i).getPosition().latitude));
//            System.out.println("(latLng.longitude - markers.get(i).getPosition().longitude) is " + (latLng.longitude - markers.get(i).getPosition().longitude));
//
//            if ((latLng.latitude - markers.get(i).getPosition().latitude) < boundary && //if within boundary
//                    (latLng.latitude - markers.get(i).getPosition().latitude) > -boundary &&
//                    (latLng.longitude - markers.get(i).getPosition().longitude) < boundary &&
//                    (latLng.longitude - markers.get(i).getPosition().longitude) > -boundary) {
//                return i;
//            }
//        }
//        return -1;
//    }

    @Override
    public void onMapLongClick(LatLng latLng) {
//        System.out.println("LongClicked");
//        int markerIndex = isInBoundary(latLng);
//
//        if (markerIndex != -1) {
//            System.out.println("About to remove");
//            markers.get(markerIndex).remove();
//            markers.remove(markerIndex);
//            Toast.makeText(MapsActivity.this, "Removed Marker", Toast.LENGTH_SHORT).show();
//            return;
//        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        User userClicked = markers.get(marker);
        Toast.makeText(MapsActivity.this, "Firstname: " + userClicked.firstname  + ", Lastname: " + userClicked.lastname, Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
//        Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
//        markers.get(marker);
//        Bundle b = new Bundle();
//        b.putParcelable("User", markers.get(marker));
//        startActivity(intent);
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



    private class FindPeople extends AsyncTask<User,Void,ArrayList<User>> {

        @Override
        protected ArrayList<User> doInBackground(User... params) {
            // Get user defined values
            User paramUser = params[0];
            String Email = paramUser.email;
            String Gender = paramUser.gender;
            String[] Movies = paramUser.movies;
            String[] Music = paramUser.music;
            String[] Interests = paramUser.interests;

            // Create data variable for sent values to server
            String data = "";

            try {
                data += URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(Email, "UTF-8");
                data += "&" + URLEncoder.encode("gender", "UTF-8")
                        + "=" + URLEncoder.encode(Gender, "UTF-8");
                data += "&" + URLEncoder.encode("movies", "UTF-8")
                        + "=" + URLEncoder.encode(java.util.Arrays.toString( Movies ) , "UTF-8");
                data += "&" + URLEncoder.encode("music", "UTF-8")
                        + "=" + URLEncoder.encode(java.util.Arrays.toString( Music ) , "UTF-8");
                data += "&" + URLEncoder.encode("interests", "UTF-8")
                        + "=" + URLEncoder.encode(java.util.Arrays.toString( Interests ) , "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            String jsonString = "";
            BufferedReader reader = null;

            // Send data
            try {
                System.out.println("Sending data which is " + data);
                // Defined URL  where to send data
                URL url = new URL("http://34.239.117.6:9000/users");

                // Send POST data request

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONObject jsonReader = new JSONObject(jsonString);

                System.out.println("JsonReader is " + jsonReader);

                JSONArray jsonUserArray = jsonReader.getJSONArray("users");
                ArrayList<User> userArray = new ArrayList<User>();

                for (int i = 0; i < jsonUserArray.length(); i++) {
                    JSONObject jsonobject = (JSONObject) jsonUserArray.get(i);
                    System.out.println("jsonobject is " + jsonobject.toString());
                    User user = new User(jsonobject);

                    userArray.add(user);
                }

                System.out.println("Returns userArray successfully");

                return userArray;
//
//                for (int i = 0; i < jsonReader.length(); i++) {
//                    userArray.add(jsonReader.get(Integer.toString(i)));
//                }
//
//                for (JSONObject object: jsonReader) {
//
//                }


//                String firstname = jsonReader.getString("firstname");
//                String lastname = jsonReader.getString("lastname");
//                String email = jsonReader.getString("email");
//                String gender = jsonReader.getString("gender");
//                double latitude = jsonReader.getDouble("lat");
//                double longitude = jsonReader.getDouble("long");



               // System.out.println("Got result which is " + );

            } catch (Exception ex) {
                System.out.println("Got exception: ");
                ex.printStackTrace();
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<User> result) {
            // The results of the above method
            // Processing the results here

            for (User user: result) {
                LatLng latLng = new LatLng(user.latitude, user.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(user.firstname + " " + user.lastname));
                markers.put(marker, user);
            }

//            System.out.println("Result is " + finalResult);
//            new android.os.Handler().postDelayed(
//                    new Runnable() {
//                        public void run() {
//                            // On complete call either onLoginSuccess or onLoginFailed
//                            //
//                            if (finalResult) {
//                                onLoginSuccess();
//                            } else {
//                                onLoginFailed();
//                            }
//                        }
//                    }, 1);
        }
    }
}
