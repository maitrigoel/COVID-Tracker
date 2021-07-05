package com.example.covidtrack;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.JsonReader;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MyTask  extends AsyncTask <Void, Void,String> {


    Context context;
    TextView textView;
    Button button;
    ProgressDialog progressDialog;

    int act = 0;
    int conf = 0;
    int dec = 0;
    int rec = 0;


    MyTask(Context context, TextView textView, Button button)
    {
        this.context= context;
        this.textView= textView;
        this.button=button;
    }


    @Override
    protected String doInBackground(Void... voids) {
        String s="";

        int i=0;
        synchronized (this)
        {
            URL githubEndpoint = null;
            try {
                githubEndpoint = new URL("https://api.covid19india.org/v2/state_district_wise.json");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpsURLConnection myConnection=null ;
            try {
                myConnection = (HttpsURLConnection) githubEndpoint.openConnection();

            } catch (IOException e) {
                e.printStackTrace();
            }
            // myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");

            try {


                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody= myConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);


                    jsonReader.beginArray();// Start processing the JSON array
                    int flag=0;
                    while (jsonReader.hasNext()) { // Loop through all states
                        jsonReader.beginObject();
                        //Toast.makeText(this, "fine", Toast.LENGTH_LONG).show();

                        while(jsonReader.hasNext())
                        {
                            String key = jsonReader.nextName();
                            if(key.equals("state"))
                            {
                                String svalue=jsonReader.nextString();
                                if(svalue.contains("Uttar Pradesh"))
                                {
                                    flag=1;
                                }
                            }
                            else if(key.equals("districtData")&&flag==1)
                            {
                                jsonReader.beginArray();
                                while(jsonReader.hasNext()) {
                                    jsonReader.beginObject();
                                    while(jsonReader.hasNext()) {
                                        String name=jsonReader.nextName();


                                        if (name.contains("Lucknow")) {

                                            //jsonReader.beginArray();
                                            jsonReader.nextName();

                                            act = jsonReader.nextInt();
                                            jsonReader.nextName();
                                            conf = jsonReader.nextInt();
                                            jsonReader.nextName();
                                            dec = jsonReader.nextInt();
                                            jsonReader.nextName();
                                            rec = jsonReader.nextInt();
                                            // CharSequence t=act;
                                            //Toast.makeText(this, act, Toast.LENGTH_LONG).show();

                                            //jsonReader.endArray();
                                        } else {
                                            jsonReader.skipValue();

                                        }


                                    }
                                    jsonReader.endObject();

                                }
                                jsonReader.endArray();
                            }
                            else
                            {
                                jsonReader.skipValue();
                            }
                        }


                    }
                    jsonReader.endArray();
                    jsonReader.close();



                    // Further processing here
                } else {
                    //Toast.makeText(this, "not fine", Toast.LENGTH_LONG).show();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            myConnection.disconnect();
        }
        return s;
    }

    @Override
    protected void onPreExecute() {
        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        // progressDialog.setMax(10);
        // progressDialog.setProgress(0);
        // progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {

        textView.setText(result);
        button.setEnabled(true);
        progressDialog.hide();



    }



    @Override
    protected void onProgressUpdate(Void... values) {
        //int progress=values[0];
        //progressDialog.setProgress(progress);
        textView.setText("Loading");
    }
}
