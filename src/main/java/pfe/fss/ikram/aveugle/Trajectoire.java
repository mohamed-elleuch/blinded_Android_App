package pfe.fss.ikram.aveugle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trajectoire extends FragmentActivity implements OnMapReadyCallback {
    LatLng PARIS =null;
    double lat1,lon1;
    LatLng Tunis = null;
    private LatLngBounds latlngBounds;
    private GoogleMap mMap;
    private int width,height;
    private Polyline newPolyline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectoire);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(34.78, 10.9125);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "manque permission");
        } else {
            try {
                LocationManager lo = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location l = lo.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.i("lat", String.valueOf(l.getLatitude()));
                Log.i("lon", String.valueOf(l.getLongitude()));

                final double lat1 = l.getLatitude();
                final double lon1 = l.getLongitude();
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat1,lon1)).title("Ma position"));

                StringRequest sr = new StringRequest(Request.Method.POST, "http://192.168.43.48/locationaveugle.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String la = js.getString("lat");
                            String lo = js.getString("lon");
                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(la),Double.parseDouble(lo))).title("Position Aveugle"));
                            findDirections(lat1, lon1, Double.parseDouble(la), Double.parseDouble(lo), GMapV2Direction.MODE_WALKING);

                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return null;
                    }
                };
                RequestQueue rq1 = Volley.newRequestQueue(getApplicationContext());
                rq1.add(sr);

            } catch (Exception e) {

            }

        }
    }
    //chercher la possibilité des directions ainsi que le plus court chemin
    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
        final String USER_CURRENT_LAT = "user_current_lat";
        final String USER_CURRENT_LONG = "user_current_long";
        final String DESTINATION_LAT = "destination_lat";
        final String DESTINATION_LONG = "destination_long";
        final String DIRECTIONS_MODE = "directions_mode";
        final Map<String, String> map = new HashMap<String, String>();
        map.put(USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(DIRECTIONS_MODE, mode);

        new AsyncTask<Map<String, String>, Object, ArrayList<LatLng>>() {


            @Override
            public void onPostExecute(ArrayList result) {

                handleGetDirectionsResult(result);

            }

            @Override
            protected ArrayList<LatLng> doInBackground(Map<String, String>... params) {
                Map<String, String> paramMap = map;
                try {
                    LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)), Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
                    LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)), Double.valueOf(paramMap.get(DESTINATION_LONG)));
                    GMapV2Direction md = new GMapV2Direction();
                    Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
                    ArrayList<LatLng> directionPoints = md.getDirection(doc);
                    return directionPoints;
                } catch (Exception e) {

                    return null;
                }
            }


        }.execute();
    }

    //tracer les segments du trajectoire
    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.BLUE);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }
        newPolyline = mMap.addPolyline(rectLine);

        latlngBounds = createLatLngBoundsObject(Tunis, PARIS);
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, 150));


    }


    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

}
