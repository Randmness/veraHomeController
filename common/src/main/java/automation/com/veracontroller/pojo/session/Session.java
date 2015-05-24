package automation.com.veracontroller.pojo.session;

import android.util.Log;

import java.util.Date;

import automation.com.veracontroller.enums.VeraType;

public class Session {
    private VeraType veraType = VeraType.VERA_UI5;
    private String localUrl;
    private String remoteUrl;
    private boolean leverageRemote = true;

    private String userName;
    private String password;
    private String serialNumber;

    public VeraType getSystemType() {
        return this.veraType;
    }

    public void setSystemType(VeraType type) {
        this.veraType = type;
    }

    public boolean getLeverageRemote() {
        return this.leverageRemote;
    }

    public void setLeverageRemote(boolean newleverageRemote) {
        this.leverageRemote = newleverageRemote;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getLocalUrl() {
        return this.localUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getRemoteUrl() {
        return this.remoteUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }
}