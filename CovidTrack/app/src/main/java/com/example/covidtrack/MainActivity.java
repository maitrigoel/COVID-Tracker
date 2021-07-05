package com.example.covidtrack;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    Button button;
    TextView textView;
    LocationManager locationManager;
    String provider;
    double lat, lng;
    final int MY_PERMISSION_REQUEST_CODE = 7171;

    private final String CHANNEL_ID = "personal_notifications";
    private final int NOTIFICATION_ID = 001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getLocation();
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.btnGetAdd);
        textView = (TextView) findViewById(R.id.textVie);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {

            getLocation();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                @SuppressLint("MissingPermission") Location myLocation = locationManager.getLastKnownLocation(provider);
                lat= myLocation.getLatitude();
                lng=myLocation.getLongitude();
                new GetAddress().execute(String.format("%.4f,%.4f",lat,lng));


                MyTask myTask=new MyTask(MainActivity.this, textView, button);
                myTask.execute();

                int act= myTask.act;
                if(act>2000)
                {
                    createNotificationChannel();
                    //progressDialog.setContentView();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_notif)
                            .setContentTitle("ALERT")
                            .setContentText("RED ZONE AREA")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
                //.setEnabled(false);
            }
        });

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //CharSequence name = getString(R.String.channel_name);
            // String description = getString(R.String.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("RED ZONE AREA ALERT");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        final Location location = locationManager.getLastKnownLocation(provider);
        if (location == null)
            Log.e("ERROR", "Location is null");

    }


    private class GetAddress extends AsyncTask<String, String, String>
    {
        String address=null;

        ProgressDialog dialog=new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            dialog.setMessage("Please wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                double lat= Double.parseDouble(strings[0].split(",")[0]);
                double lng= Double.parseDouble(strings[0].split(",")[1]);
                String response;

                com.example.covidtrack.HttpDataHandler http=new com.example.covidtrack.HttpDataHandler();
                String url=String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&sensor=false",lat,lng);
                response=http.GetHTTPData(url);
                return response;
            }
            catch(Exception ex)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {

            try{
                JSONObject jsonObject=new JSONObject(s);

                address= ((JSONArray)jsonObject.get("results")).getJSONObject(0).get("formatted_address").toString();
                textView.setText("done");

            }
            catch(JSONException e)
            {
                e.printStackTrace();

            }
            if(dialog.isShowing())
                dialog.dismiss();

            //return address;
        }
    }

}
