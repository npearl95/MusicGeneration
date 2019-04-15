package edu.temple.musicgen;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CustomMenuActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
                case R.id.SignIn:
                    SignIn();
                    return true;
                case R.id.Generate:
                    Generate();
                    return true;
                    default:
                return super.onOptionsItemSelected(item);
        }
       //return true;
    }

    private void SignIn(){
        //Show the about screen
        Intent myIntent = new Intent(this, SignIn.class);
        startActivityForResult(myIntent, 0);

    }

    private void Generate(){
        //Do something new
        Intent myIntent = new Intent(this, GenerateSong.class);
        startActivityForResult(myIntent, 0);
    }
}