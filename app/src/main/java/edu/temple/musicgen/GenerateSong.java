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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GenerateSong extends AppCompatActivity {

    //private static final String TAG = "SignInActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_song);

        Intent myIntent = getIntent();
        // Views
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


        Button button = findViewById(R.id.generate_button);
        final TextView textView = findViewById(R.id.textView3);


        //Button listener for the generate music
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                AsyncTask asyncTask  = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        URL APIUrl;
                        BufferedReader reader = null;
                        HttpURLConnection urlConnection = null;
                        String response = null;

                        try {

                            APIUrl = new URL("http://3.16.26.98:1337/generate_song");

                            Log.e("URL", String.valueOf(APIUrl));

                            urlConnection = (HttpURLConnection) APIUrl.openConnection();
                            urlConnection.setRequestMethod("POST");
                            //Get not use GET because some are exclusive GET
                            urlConnection.connect();

                            InputStream inputStream = urlConnection.getInputStream();
                            StringBuffer buffer = new StringBuffer();
                            if (inputStream == null) {
                                response = null;
                            }

                            reader = new BufferedReader(new InputStreamReader(inputStream));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }

                            if(buffer.length() == 0){
                                response = buffer.toString();
                            }
                            response = buffer.toString();

                            String tmpResponse;

                            tmpResponse = reader.readLine();
                            while (tmpResponse != null) {
                                response = response + tmpResponse;
                                tmpResponse = reader.readLine();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null){
                                urlConnection.disconnect();
                            }

                            if (reader != null){
                                try {
                                    reader.close();
                                } catch (final IOException e){
                                }
                            }
                        }

                        if(response != null) {
                            Log.e("Response", response);
                        }else{
                            Log.e("Response", "No Response");
                        }
                        return response;


                    }

                    @Override
                    protected void onPostExecute(Object o) {

                        textView.setText(o.toString());
                    }
                }.execute();
                //Start New Activity
                Intent song = new Intent(GenerateSong.this, MusicPlayer.class);
                startActivity(song);

            }
        });


    }
}
