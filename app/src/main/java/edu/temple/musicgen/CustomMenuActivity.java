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

                case R.id.GenerateNav:
                    Generate();
                    return true;
                    default:
                return super.onOptionsItemSelected(item);
        }
       //return true;
    }

    private void Generate(){
        //Do something new
        Intent myIntent = new Intent(this, GenerateSong.class);
        startActivityForResult(myIntent, 0);
    }
}