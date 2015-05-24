package automation.com.veracontroller.pojo.session;

import android.util.Log;

import java.util.Date;

import automation.com.veracontroller.constants.ConnectionConstants;
import automation.com.veracontroller.enums.VeraType;

public class SessionUI7 extends Session {
    private String serverRelay;
    private String sessionToken;
    private Date expirationDate;

    public static final long HOUR = 3600*1000;

    public void setServerRelay(String serverRelay) {
        this.serverRelay = serverRelay;
    }

    public String getServerRelay() {
        return this.serverRelay;
    }

    public void setSessionToken(String sessionToken) {
        this.setSystemType(VeraType.VERA_UI7);
        this.sessionToken = sessionToken;
        this.expirationDate = new Date();
    }

    public String getSessionToken() {
        return  this.sessionToken;
    }

    public boolean isExpired() {
        return expirationDate.before(new Date(System.currentTimeMillis() - 1000));
    }
}