package edu.temple.musicgen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class History extends CustomMenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //Description: returns all previously generated songs by a user
        //Method: POST
        //Parameters:
        //{
        //    'profileID': <string of the google profile id>,
        //    'profileEmail': <string of the google email>
        //}


    }
}
