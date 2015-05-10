package automation.com.veracontroller.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;

public class BinaryLight implements Parcelable {
    private int deviceNum;

    private String name;
    private String roomName;
    private boolean state = false;

    public BinaryLight(int deviceNum, String name, String roomName) {
        this.deviceNum = deviceNum;
        this.name = name;
        this.roomName = roomName;
    }

    public int getDeviceNum() {
        return this.deviceNum;
    }

    public String getRoomName() { return this.roomName; }

    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.state;
    }

    public String onOrOff(boolean var) {
        if (var) {
            return "ON";
        }

        return "OFF";
    }

    public String onOrOff() {
        return onOrOff(state);
    }

    public void setState(boolean state) {
        this.state = state;
    }


    public BinaryLight(Parcel in) {
        String[] data = new String[4];

        in.readStringArray(data);
        this.deviceNum = Integer.parseInt(data[0]);
        this.name = data[1];
        this.roomName = data[2];
        this.state = Boolean.parseBoolean(data[3]);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {String.valueOf(this.deviceNum), this.name, this.roomName,
                String.valueOf(this.state)});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BinaryLight createFromParcel(Parcel in) {
            return new BinaryLight(in);
        }

        public BinaryLight[] newArray(int size) {
            return new BinaryLight[size];
        }
    };
}
