package edu.temple.musicgen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import static edu.temple.musicgen.SignIn.userInfo;

public class GenerateSong extends CustomMenuActivity {
    HashMap<String, String> songMap = new HashMap<>();
    String selectedDuration;
    String selectedGenre,selectedTempo;
    String userName, profileID, profileEmail, songId;

    private static final String TAG = "GenerateSong";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_song);





        //Intent myIntent = getIntent();
        // Get user from the previous activity
        //Sign In Intent
        //profileID: <string of the google profile id>,
        //profileEmail': <string of the google email>
        //userName
        TextView user_name = findViewById(R.id.user_name);
        //userName = myIntent.getStringExtra("userName");

        //userName= userInfo.getUserName();
        Log.w(TAG, "TEST HERE"+ userInfo.getUserName());
        userName =userInfo.getUserName();
        profileEmail =userInfo.getProfileEmail();
        profileID=userInfo.getProfileID();

        //profileEmail = myIntent.getStringExtra("profileEmail");
        //profileID = myIntent.getStringExtra("profileID");
        //profileID= userInfo.getProfileID();
        //profileEmail=userInfo.getProfileEmail();
        user_name.setText(" Hey there, " + userName);
        //Spinner Genre
        Spinner spinnergenre = findViewById(R.id.genre_spinner);
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(this, R.array.genre, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnergenre.setAdapter(genreAdapter);

        selectedGenre = spinnergenre.getSelectedItem().toString();
        //Log.w(TAG, "Genre"+ selectedGenre);


        //Spinner Tempo
        Spinner spinnerTempo = findViewById(R.id.tempo_spinner);
        ArrayAdapter<CharSequence> tempoAdapter = ArrayAdapter.createFromResource(this, R.array.tempo, android.R.layout.simple_spinner_item);
        tempoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTempo.setAdapter(tempoAdapter);

        selectedTempo = spinnerTempo.getSelectedItem().toString();
        //Spinner Duration
        Spinner spinnerDuration = findViewById(R.id.duration_spinner);
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this, R.array.duration, android.R.layout.simple_spinner_item);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);

        selectedDuration = spinnerDuration.getSelectedItem().toString();

        Button generate_button = findViewById(R.id.generate_button);
        generate_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Run the API POST request a new song
                new SendRequest().execute();
            }
        });
    }


    public class SendRequest extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(GenerateSong.this);
        protected void onPreExecute(){
            this.dialog.setMessage("Generating Music...");
            this.dialog.show();
        }

        protected String doInBackground(String... arg0) {
            //Object
            JSONObject postDataParams = new JSONObject();
            try {

                postDataParams.put("genre", selectedGenre);
                postDataParams.put("tempo", selectedTempo);
                postDataParams.put("duration", selectedDuration);
                postDataParams.put("profileID", profileID);
                postDataParams.put("profileEmail", profileEmail);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String request        = "http://18.191.144.92/generate_song";
                URL    url            = new URL( request );
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setDoOutput( true );
                conn.setInstanceFollowRedirects( false );
                conn.setRequestMethod( "POST" );
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setUseCaches( false );
                OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
                wr.write(postDataParams.toString());
                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line="";
                    while((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();
                    //close the connect
                    conn.disconnect();
                    Log.e("Return", sb.toString());
                    JSONObject testJSON = new JSONObject(sb.toString());
                    //Log.w(TAG,testJSON);

                    songMap = jsonToMap(sb.toString());
                     //Set messasnger
                    this.dialog.setMessage("Done");
                    this.dialog.show();
                    Log.w(TAG,sb.toString());
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
            dialog.dismiss();
            //Transform resul to Map
            HashMap<String, String> myResultInMap = new HashMap<>();
            try {
                myResultInMap = jsonToMap(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Start New Activity
            final Intent songIntent = new Intent(GenerateSong.this, MusicPlayer.class);
            //Add Things to Intent
            //GenerateSong Intent
            //profileID: <string of the google profile id>,
            //profileEmail': <string of the google email>
            //songName
            //location
            //songID
            songIntent.putExtra("songName", myResultInMap.get("song_name"));
            songIntent.putExtra("location", myResultInMap.get("location"));
            songIntent.putExtra("profileEmail", profileEmail);
            songIntent.putExtra("profileID", profileID);
            songIntent.putExtra("songID", myResultInMap.get("song_id"));
            Log.e(TAG, "Intent's Sent song Id"+myResultInMap.get("song_id"));
            startActivity(songIntent);
        }

    }
    public static HashMap<String, String> jsonToMap(String t) throws JSONException {
        HashMap<String, String> map = new HashMap<String, String>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.getString(key);
            map.put(key, value);
        }
        return map;
    }


}
