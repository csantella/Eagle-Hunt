package com.team404.eaglehunt;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class QuizModeLauncher extends ActionBarActivity {

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_mode_launcher);
        i = new Intent(this, QuizMode.class);
        Button med = (Button) findViewById(R.id.medButon);
        Button hard = (Button) findViewById(R.id.hardButton);

        //med.setEnabled(false);
        //hard.setEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quiz_mode_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEasyClick(View v)
    {
        i.putExtra("difficulty", 0);
        startQuizMode();
    }

    public void onMediumClick(View v)
    {
        i.putExtra("difficulty", 1);
        startQuizMode();
    }

    public void onHardClick(View v)
    {
        i.putExtra("difficulty", 2);
        startQuizMode();
    }

    public void startQuizMode()
    {
        startActivity(i);
    }
}
