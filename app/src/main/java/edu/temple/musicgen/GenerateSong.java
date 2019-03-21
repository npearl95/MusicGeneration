package edu.temple.musicgen;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.in;

public class GenerateSong extends AppCompatActivity {

    //private static final String TAG = "SignInActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_song);


        Intent myIntent = getIntent();
        // Get user from the previous activity
        TextView user_name = findViewById(R.id.user_name);
        String email = myIntent.getStringExtra("email");
        user_name.setText(email);
        //Spinner Genre
        Spinner spinnergenre = findViewById(R.id.genre_spinner);
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(this, R.array.genre, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnergenre.setAdapter(genreAdapter);
        //Spinner Tempo
        Spinner spinnerTempo = findViewById(R.id.tempo_spinner);
        ArrayAdapter<CharSequence> tempoAdapter = ArrayAdapter.createFromResource(this, R.array.tempo, android.R.layout.simple_spinner_item);
        tempoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTempo.setAdapter(tempoAdapter);
        //Spinner Duration
        Spinner spinnerDuration = findViewById(R.id.duration_spinner);
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this, R.array.duration, android.R.layout.simple_spinner_item);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);


        Button generate_button = findViewById(R.id.generate_button);
        final TextView textView = findViewById(R.id.textView3);

        generate_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                new SendRequest().execute();

                //Start New Activity
                Intent song = new Intent(GenerateSong.this, MusicPlayer.class);
                startActivity(song);

            }
        });
    }


    public class SendRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {
            //Object
            JSONObject postDataParams = new JSONObject();
            try {
                postDataParams.put("genre", "jazz");
                postDataParams.put("tempo", "slow");
                postDataParams.put("duration", "medium");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log out the params
            Log.e("params", postDataParams.toString());
            //Log out the sending params
            //Log.e("params to String", getPostDataString(postDataParams));

            try {
                URL url = new URL("http://3.16.26.98:1337/echo");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                CookieManager cookieManager = new CookieManager();
                CookieHandler.setDefault(cookieManager);

                //Start customize the connection
                // Set Headers
                conn.setRequestProperty("CustomHeader", "someValue");
                conn.setRequestProperty("accept", "application/json");

                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                //conn.setDoInput(true);
                conn.setDoOutput(true);
                
                //conn.setChunkedStreamingMode(0);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                conn.setFixedLengthStreamingMode(getPostDataString(postDataParams).getBytes().length);
                conn.setChunkedStreamingMode(0);
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();



                /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                outputStreamWriter.write(getPostDataString(postDataParams));
                outputStreamWriter.flush();*/

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();

                    //close the connect
                    conn.disconnect();
                    Log.e("Return", sb.toString());

                    return sb.toString();
                } else {
                    return new String("false : " + responseCode);
                }
            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
        //Action take after execute
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            final TextView textView = findViewById(R.id.textView3);
            textView.setText(result);


        }
    }
    //Convert the params object to a string format: genre=jazz&tempo=slow&duration=medium
    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first) {
                first = false;
            } else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}
