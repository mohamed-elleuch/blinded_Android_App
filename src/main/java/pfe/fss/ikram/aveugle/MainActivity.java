package pfe.fss.ikram.aveugle;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final double dd,dg,da;
        da=2;
        dd=150;
        dg=140;
        t1=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.FRENCH);
                }
            }
        });
        Thread sp = new Thread() {
            @Override
            public void run() {

                synchronized (this) {
                    t1.speak("Présence d'obstacle devant vous, tourner à droite", TextToSpeech.QUEUE_ADD, null);
                    if(da>0.5)
                    {
                        t1.speak("Plus d'obstacle, continuer à marcher", TextToSpeech.QUEUE_ADD, null);
                    }
                    else
                    {
                        if(dg>0.5)
                        {
                            if(dd>0.5)
                            {
                                t1.speak("Obstacle, Tourner à droite ou à gauche", TextToSpeech.QUEUE_ADD, null);
                                Toast.makeText(MainActivity.this, "Bonjour", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                t1.speak("Obstacle, Tourner à gauche", TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        else
                        {
                            if(dd>0.5)
                            {
                                t1.speak("Obstacle, Tourner à droite", TextToSpeech.QUEUE_ADD, null);
                            }
                            else
                            {
                                t1.speak("Obstacle partout, Stop et patientez un peu", TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                    }

                }

            }
        };

        sp.start();


       // Intent intent = new Intent(MainActivity.this, MyService.class);
       // startService(intent);


    }
}
