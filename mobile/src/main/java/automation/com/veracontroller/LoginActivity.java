package automation.com.veracontroller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import automation.com.veracontroller.async.AuthenticateUserTask;
import automation.com.veracontroller.constants.IntentConstants;
import automation.com.veracontroller.enums.VeraType;
import automation.com.veracontroller.pojo.session.Session;
import automation.com.veracontroller.pojo.session.SessionUI7;
import automation.com.veracontroller.util.RestClientUI7;


public class LoginActivity extends Activity {
    private List<String> userList = new ArrayList<>();

    private TextView username;
    private TextView password;

    private Session session;
    private VeraType veraType;
    private boolean initialLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = RestClientUI7.getSession();
        initialLogin = getIntent().getBooleanExtra(IntentConstants.INITIAL_LOGIN, false);
        veraType = VeraType.fromType(getIntent().getStringExtra(IntentConstants.VERA_TYPE));

        username = (TextView) findViewById(R.id.username);

        if (!initialLogin) {
            if (session.getUserName() != null) {
                username.setText(session.getUserName());
            }
        } else {
            if (veraType == VeraType.VERA_UI7) {
                session = new SessionUI7();
            } else {
                session = new Session();
            }
            session.setSystemType(veraType);
        }

        password = (TextView) findViewById(R.id.password);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new AuthenticateUserTask(LoginActivity.this, username.getText().toString().trim(),
                    password.getText().toString(), session, initialLogin).execute();
            }
        });
    }
}
