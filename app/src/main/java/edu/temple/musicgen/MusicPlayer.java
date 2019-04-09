package edu.temple.musicgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class MusicPlayer extends CustomMenuActivity {

    final static String TAG ="MusicPlayer";
    private MediaPlayer mediaPlayer;
    public TextView songName, duration;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 30000, backwardTime = 30000;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;
    DownloadManager downloadManager;
    String locationfromIntent, songNamefromIntent, profileID, profileEmail;
    Intent songIntent;
    private ListView lv;
    String currentPlayinglocation, currentPlayingSong;
    String currentPlayingSongID;
    PopupWindow popupWindow;
    private String newNamefromUser = "";
    EditText edit;

    ArrayList<HashMap<String, String>> historyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        //initialize views
        initializeViews();


    }
    public void initializeViews(){
        songName = findViewById(R.id.songName);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        songIntent = getIntent();
        songNamefromIntent = songIntent.getStringExtra("songName");
        songName.setText(songNamefromIntent);
        locationfromIntent = songIntent.getStringExtra("location");
        currentPlayingSongID = songIntent.getStringExtra("song_id");
        Log.w(TAG, "Current song ID"+currentPlayingSongID);


        profileID= songIntent.getStringExtra("profileID");
        profileEmail= songIntent.getStringExtra("profileEmail");
        Log.w(TAG, profileID);
        Log.w(TAG, profileEmail);

        mediaPlayer.reset();
        //set Media player
        try {
            mediaPlayer.setDataSource(locationfromIntent);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalTime = mediaPlayer.getDuration();
        duration = findViewById(R.id.songDuration);
        seekbar = findViewById(R.id.seekBar);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(true);
        historyList = new ArrayList<>();
        lv = findViewById(R.id.listView);

        new SendRequestHistory().execute();

    }

    //Update new View for music Player

    public void UpdateViews(){
        songName.setText(currentPlayingSong);
        finalTime =0;
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentPlayinglocation);
            mediaPlayer.prepareAsync();
            finalTime = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //duration = (TextView) findViewById(R.id.songDuration);

        seekbar = findViewById(R.id.seekBar);
        seekbar.setMax((int) finalTime);
        historyList = new ArrayList<>();
        lv = findViewById(R.id.listView);
        lv = findViewById(R.id.listView);
        new SendRequestHistory().execute();
    }

    // play mp3 song
    public void play(View view) {
        mediaPlayer.start();
        timeElapsed = mediaPlayer.getCurrentPosition();
        seekbar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateSeekBarTime, 100);
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

    // pause mp3 song
    public void pause(View view) {

        mediaPlayer.pause();
    }

    // go forward at forwardTime seconds
    public void forward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed + forwardTime) <= finalTime) {
            timeElapsed = timeElapsed + forwardTime;

            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }
    public void rewind(View view){
        if((timeElapsed - backwardTime)>0){
            timeElapsed = timeElapsed -backwardTime;
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }
    public void deletesong(View view){
        new SendRequestDeleteSong().execute();
    }
    public  void editname(View view) {
        Button closePopupBtn;
        Button editPopupBtn;


        //PopupWindow popupWindow;
        LayoutInflater layoutInflater = (LayoutInflater) MusicPlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup, null, false);

        closePopupBtn = customView.findViewById(R.id.closePopupBtn);
        editPopupBtn =customView.findViewById(R.id.editPopupBtn);
        edit = customView.findViewById(R.id.newName);

        Log.e(TAG,"new name from user input" +newNamefromUser);

        //instantiate popup window
        popupWindow = new PopupWindow(customView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);

        //display the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);
        //popupWindow.setOutsideTouchable(true);

        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"CLOSE BUTTON clicked");
                popupWindow.dismiss();
            }
        });
        //close the popup window on button click
        editPopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"EDIT BUTTON clicked");
                new SendRequestChangeName().execute();
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
    public long download(View view){
        long downloadReference;
        Uri uri = Uri.parse(currentPlayinglocation);
        Log.w(TAG, "Song download link"+currentPlayinglocation);
        // Create request for android download manager
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle(currentPlayingSong);

        //Setting description of request
        request.setDescription("Song from Wimbo Music");
        downloadReference = downloadManager.enqueue(request);
        Log.w(TAG, "Request process");

        return downloadReference;

    }


    public class SendRequestHistory extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0) {
            //Object
            JSONObject postDataParams = new JSONObject();
            try {
                postDataParams.put("profileID", profileID);
                postDataParams.put("profileEmail", profileEmail);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String request        = "http://18.191.144.92/history";
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    os.write(postDataParams.toString().getBytes(StandardCharsets.UTF_8));
                }
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


                    historyList.clear();

                    //Set messasnger
                    String jsonStr=sb.toString();
                    if (jsonStr != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(jsonStr);

                            // Getting JSON Array node
                            JSONArray arrayNode = jsonObj.getJSONArray("history");

                            // looping through All history
                            for (int i = 0; i < arrayNode.length(); i++) {
                                JSONObject c = arrayNode.getJSONObject(i);
                                String songid = c.getString("song_id");
                                String songname = c.getString("song_name");
                                String songlocation = c.getString("location");


                                // tmp hash map for single song
                                HashMap<String, String> songInfo = new HashMap<>();

                                // adding each child node to HashMap key => value
                                songInfo.put("song_id", songid);
                                songInfo.put("song_name", songname);
                                songInfo.put("location", songlocation);

                                // adding contact to contact list
                                historyList.add(songInfo);
                            }
                        } catch (final JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                        }

                    } else {
                        Log.e(TAG, "Couldn't get json from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server. Check LogCat for possible errors!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
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
            //Clean list view
            lv.setAdapter(null);
            ListAdapter adapter = new SimpleAdapter(MusicPlayer.this, historyList,
                    R.layout.activity_listview, new String[]{ "song_name","song_id"},
                    new int[]{ R.id.song_name,R.id.song_id});

            //try {
                //set time in mili
              //  Thread.sleep(1000);

            //}catch (Exception e){
              //  e.printStackTrace();
            //}



            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // Get the selected item text from ListView
                   // String selectedItem = (String) parent.getItemAtPosition(position);

                    // Display the selected item text on TextView
                    Log.e(TAG, "Your song at licked position : " + parent.getItemAtPosition(position));
                    String value = parent.getItemAtPosition(position).toString();

                    //Convert String back to Map

                    value = value.substring(1, value.length()-1);           //remove curly brackets
                    String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
                    HashMap<String,String> map = new HashMap<>();

                    for(String pair : keyValuePairs)                        //iterate over the pairs
                    {
                        String[] entry = pair.split("=");                   //split the pairs to get key and value
                        map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
                    }
                    currentPlayingSong = map.get("song_name");
                    currentPlayinglocation = map.get("location");
                    currentPlayingSongID = map.get("song_id");
                    Log.e(TAG,"song_name"+currentPlayingSong);


                    UpdateViews();
                }
            });


        }

    }






    public class SendRequestChangeName extends AsyncTask<String, Void, String> {

        //Parameters:
        //{
        //    'profileID': <string of the google profile id>,
        //    'profileEmail': <string of the google email>,
        //    'songID': <string of the song id from the /history song object>,
        //    'newName': <string of the new song name>
        //}


        protected void onPreExecute() {
            newNamefromUser = edit.getText().toString();

        }

        protected String doInBackground(String... arg0) {

            Log.e(TAG, "Send Request Change");
            Log.e(TAG, "Current song Id"+currentPlayingSongID);




            //Object
            JSONObject postDataParams = new JSONObject();
            try {
                postDataParams.put("profileID", profileID);
                postDataParams.put("profileEmail", profileEmail);
                postDataParams.put("songID", currentPlayingSongID);
                postDataParams.put("newName", newNamefromUser);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "Send object"+postDataParams);
            try {
                String request = "http://18.191.144.92/edit_song";
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setUseCaches(false);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(postDataParams.toString());
                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
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
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }


        //Action take after execute
        @Override
        protected void onPostExecute(String result) {
            //Update the listview
            currentPlayingSong = newNamefromUser;
            songName.setText(currentPlayingSong);
            new SendRequestHistory().execute();
        }
    }








    //DeleteSOngRequest

    public class SendRequestDeleteSong extends AsyncTask<String, Void, String> {

        //Parameters:
        //{
        //    'profileID': <string of the google profile id>,
        //    'profileEmail': <string of the google email>,
        //    'songID': <string of the song id from the /history song object>,
        //    'newName': <string of the new song name>
        //}


        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {
            Log.e(TAG, "Send Request Change");
            Log.e(TAG, "Current song Id"+currentPlayingSongID);



            //Object
            JSONObject postDataParams = new JSONObject();
            try {
                postDataParams.put("profileID", profileID);
                postDataParams.put("profileEmail", profileEmail);
                postDataParams.put("songID", currentPlayingSongID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "Send object"+postDataParams);
            try {
                String request = "http://18.191.144.92/remove_song";
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setUseCaches(false);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(postDataParams.toString());
                OutputStream os = conn.getOutputStream();
                os.write(postDataParams.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
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
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }


        //Action take after execute
        @Override
        protected void onPostExecute(String result) {
            //TODO: Add to what happen if to the media if user delete the current one
            new SendRequestHistory().execute();

        }
    }
}
