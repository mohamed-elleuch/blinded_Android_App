package pfe.fss.ikram.aveugle;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SensorService extends Service {
    public int counter = 0;
    String text;
    TextToSpeech tts;
    String result1="";

    public SensorService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public SensorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 4000, 4000); //

    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
//recuperation de la position actuelle du aveugle

                if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                  Log.i("permission","manque permission");
                }
                else {
                    try {
                        LocationManager lo = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Location l = lo.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Log.i("lat", String.valueOf(l.getLatitude()));
                        Log.i("lon", String.valueOf(l.getLongitude()));

                        final double lat1 = l.getLatitude();
                        final double lon1 = l.getLongitude();
                        StringRequest sr = new StringRequest(Request.Method.POST, "http://192.168.43.48/location.php", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> para = new HashMap<>();
                                para.put("latitude", String.valueOf(lat1));
                                para.put("longitude", String.valueOf(lon1));
                                return para;
                            }
                        };
                        RequestQueue rq1 = Volley.newRequestQueue(getApplicationContext());
                        rq1.add(sr);
                    } catch (Exception e) {
                    }
                }
//fin de recuperation









                StringRequest sr1=new StringRequest(Request.Method.POST, "http://192.168.43.48/av.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            JSONObject js=new JSONObject(response);
                            final double dd=Double.parseDouble(js.getString("dd"));
                            final double da=Double.parseDouble(js.getString("da"));
                            final double dg=Double.parseDouble(js.getString("dg"));

                            if(da>130)
                            {
                                result1="Plus d'obstacle, continuer à marcher";
                            }
                            else
                            {
                                if(dg>130)
                                {
                                    if(dd>130)
                                    {
                                        result1="Obstacle, Tourner à droite ou à gauche";
                                    }
                                    else
                                    {
                                       result1="Obstacle, Tourner à gauche";
                                    }
                                }
                                else
                                {
                                    if(dd>130)
                                    {
                                        result1="Obstacle, Tourner à droite";
                                    }
                                    else
                                    {
                                       result1="Obstacle partout, Stop et patientez un peu";
                                    }
                                }
                            }
                            tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

                                @Override
                                public void onInit(int status) {
                                    // TODO Auto-generated method stub
                                    if(status == TextToSpeech.SUCCESS){
                                        int result=tts.setLanguage(Locale.FRENCH);
                                        if(result==TextToSpeech.LANG_MISSING_DATA ||
                                                result==TextToSpeech.LANG_NOT_SUPPORTED){
                                            Log.e("error", "This Language is not supported");
                                        }
                                        else{
                                            ConvertTextToSpeech(result1);
                                        }
                                    }
                                    else
                                        Log.e("error", "Initilization Failed!");
                                }
                            });
                        }
                        catch(Exception e)
                        {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                      return null;
                    }
                };
                RequestQueue rq= Volley.newRequestQueue(getApplicationContext());
                rq.add(sr1);
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void ConvertTextToSpeech(String t) {
        text=t;
        if(text==null||"".equals(text))
        {
            text = "Contenu indisponible";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else
           tts.speak(text+", s'il vous plait", TextToSpeech.QUEUE_FLUSH, null);

    }
}