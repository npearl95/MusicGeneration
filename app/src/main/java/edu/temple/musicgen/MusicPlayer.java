package edu.temple.musicgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;
    DownloadManager downloadManager;
    String locationfromIntent, songNamefromIntent, profileID, profileEmail;
    Intent songIntent;
    private ListView lv;
    String currentPlayinglocation, currentPlayingSong;
    PopupWindow popupWindow;
    boolean click = true;

    ArrayList<HashMap<String, String>> historyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        //initialize views
        initializeViews();
        historyList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listView);
        new SendRequestHistory().execute();



    }
    public void initializeViews(){
        songName = (TextView) findViewById(R.id.songName);
        //mediaPlayer = MediaPlayer.create(this, R.raw.mymusic);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        songIntent = getIntent();
        songNamefromIntent = songIntent.getStringExtra("songName");
        songName.setText(songNamefromIntent);
        locationfromIntent = songIntent.getStringExtra("location");

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
        //Log.w(TAG, "duration "+duration);
        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(true);

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
            //mediaPlayer.start();
            //mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //duration = (TextView) findViewById(R.id.songDuration);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax((int) finalTime);
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
    public  void editname(View view) {
        Button closePopupBtn;
        //PopupWindow popupWindow;
        //instantiate the popup.xml layout file
        LayoutInflater layoutInflater = (LayoutInflater) MusicPlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = layoutInflater.inflate(R.layout.popup, null, false);

        closePopupBtn = (Button) customView.findViewById(R.id.closePopupBtn);

        //instantiate popup window
        popupWindow = new PopupWindow(customView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);

        //display the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 10, 10);
        Log.e(TAG,"CLOSE BUTTON about to clicked");
        //popupWindow.setOutsideTouchable(true);
        //close the popup window on button click
        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"CLOSE BUTTON clicked");
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
        Uri uri = Uri.parse(locationfromIntent);
        // Create request for android download manager
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle(songNamefromIntent);

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
                String request        = "http://api.thewimbo.me/history";
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
                os.write(postDataParams.toString().getBytes("UTF-8"));
                os.close();

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
            ListAdapter adapter = new SimpleAdapter(MusicPlayer.this, historyList,
                    R.layout.activity_listview, new String[]{ "song_id","song_name"},
                    new int[]{R.id.song_id, R.id.song_name});
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
                    Log.e(TAG,"song_name"+currentPlayingSong);
                    UpdateViews();
                }
            });


        }

    }
}
