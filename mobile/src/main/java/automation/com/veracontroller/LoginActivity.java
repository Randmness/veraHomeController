package automation.com.veracontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.async.AuthenticateUserTask;
import automation.com.veracontroller.constants.IntentConstants;


public class LoginActivity extends Activity {
    private List<String> userList = new ArrayList<>();

    private Spinner userSpinner;
    private TextView password;
    private String serialNumber;
    private boolean initialLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userList = getIntent().getStringArrayListExtra(IntentConstants.USER_LIST);
        serialNumber = getIntent().getStringExtra(IntentConstants.SERIAL_NUMBER);
        initialLogin = getIntent().getBooleanExtra(IntentConstants.INITIAL_LOGIN, false);

        userSpinner = (Spinner) findViewById(R.id.users);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_spinner_item, userList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(dataAdapter);

        password = (TextView) findViewById(R.id.password);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        new AuthenticateUserTask(LoginActivity.this,
                                userSpinner.getSelectedItem().toString(), password.getText().toString(), serialNumber, initialLogin).execute();
                    }
                });

            }
        });
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
}
