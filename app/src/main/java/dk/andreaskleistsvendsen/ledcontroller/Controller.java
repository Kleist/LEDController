package dk.andreaskleistsvendsen.ledcontroller;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.UnknownHostException;


public class Controller extends Activity {
    private TextView statusLabel_;
    private LEDBridge bridge_;
    private SharedPreferences preferences_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        statusLabel_ = (TextView) findViewById(R.id.statusLabel);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences_ = PreferenceManager.getDefaultSharedPreferences(this);
        preferences_.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                initLEDBridge_();
            }
        });
        initLEDBridge_();
        addButtonListeners();
    }

    private void initLEDBridge_() {
        try {
            String ipAddress = preferences_.getString("ip_address", "undefined");
            int port = Integer.parseInt(preferences_.getString("port", "8899"));
            bridge_ = new LEDBridge(ipAddress, port);
        } catch (UnknownHostException e) {
            statusLabel_.setText(e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectButton_(int id, final LEDBridge.LED led, final boolean on) {
        Button btn = (Button) findViewById(id);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bridge_.change(led, on);
                if (on) {
                    statusLabel_.setText(led.toString() + " on");
                }
                else {
                    statusLabel_.setText(led.toString() + " on");
                }
            }
        });
    }

    void addButtonListeners() {
        connectButton_(R.id.btnAllOff, LEDBridge.LED.ALL, false);
        connectButton_(R.id.btnAllOn,  LEDBridge.LED.ALL, true);

        connectButton_(R.id.btnWhiteOff, LEDBridge.LED.WHITE, false);
        connectButton_(R.id.btnWhiteOn,  LEDBridge.LED.WHITE, true);

        connectButton_(R.id.btnRGBOff, LEDBridge.LED.RGB, false);
        connectButton_(R.id.btnRGBOn,  LEDBridge.LED.RGB, true);
    }
}
