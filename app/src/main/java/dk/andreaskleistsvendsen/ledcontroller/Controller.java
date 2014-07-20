package dk.andreaskleistsvendsen.ledcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Controller extends Activity {

    private Button onButton_;
    private Button offButton_;
    private TextView statusLabel_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        statusLabel_ = (TextView) findViewById(R.id.statusLabel);
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

    private void addButtonListeners() {
        onButton_ = (Button) findViewById(R.id.onButton);
        offButton_ = (Button) findViewById(R.id.offButton);

        setStringOnClick_(onButton_, R.string.turning_on);
        setStringOnClick_(offButton_, R.string.turning_off);
    }

    private void setStringOnClick_(Button button, final int stringId) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusLabel_.setText(stringId);
            }
        });
    }
}
