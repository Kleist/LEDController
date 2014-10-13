package dk.andreaskleistsvendsen.ledcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.UnknownHostException;


public class Controller extends Activity {
    private static final String HOME_IP_ADDRESS = "192.168.1.124"; // Milight2
    private TextView statusLabel_;
    private LEDBridge bridge_;

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        statusLabel_ = (TextView) findViewById(R.id.statusLabel);
        try {
            bridge_ = new LEDBridge(HOME_IP_ADDRESS, 8899);
        } catch (UnknownHostException e) {
            statusLabel_.setText(e.getMessage());
            e.printStackTrace();
        }
        addButtonListeners();
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addListener_(int id, View.OnClickListener listener) {
        Button btn = (Button) findViewById(id);
        btn.setOnClickListener(listener);
    }

    private void addListener2_(int id, final String msg, final Runnable runnable) {
        Button btn = (Button) findViewById(id);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runnable.run();
                statusLabel_.setText(msg);
            }
        });
    }

    void addButtonListeners() {
        addListener2_(R.id.btnAllOff, "Turning all off", new Runnable(){
            @Override public void run() {
                bridge_.allOff();
            }
        });

        addListener2_(R.id.btnAllOn, "Turning all on", new Runnable(){
            @Override public void run() {
                bridge_.allOn();
            }
        });

        addListener2_(R.id.btnWhiteOff, "Turning white off", new Runnable(){
            @Override public void run() {
                bridge_.whiteOff();
            }
        });

        addListener2_(R.id.btnWhiteOn, "Turning white on", new Runnable(){
            @Override public void run() {
                bridge_.whiteOn();
            }
        });

        addListener2_(R.id.btnRGBOff, "Turning RGB off", new Runnable(){
            @Override public void run() {
                bridge_.rgbOff();
            }
        });

        addListener2_(R.id.btnRGBOn, "Turning RGB on", new Runnable(){
            @Override public void run() {
                bridge_.rgbOn();
            }
        });

    }
}
