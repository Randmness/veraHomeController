package automation.com.veracontroller.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class Scene implements Parcelable, Comparable<Scene> {
    public static final Creator CREATOR = new Creator() {
        public Scene createFromParcel(Parcel in) {
            return new Scene(in);
        }

        public Scene[] newArray(int size) {
            return new Scene[size];
        }
    };
    private int sceneNum;
    private String sceneName;
    private String roomName;

    public Scene(int sceneNum, String sceneName, String roomName) {
        this.sceneNum = sceneNum;
        this.sceneName = sceneName;
        this.roomName = roomName;
    }

    public Scene(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        this.sceneNum = Integer.parseInt(data[0]);
        this.sceneName = data[1];
        this.roomName = data[2];
    }

    public String getSceneName() {
        return this.sceneName;
    }

    public int getSceneNum() {
        return this.sceneNum;
    }

    public String getRoomName() { return this.roomName; }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{String.valueOf(this.sceneNum), this.sceneName, this.roomName});
    }

    @Override
    public String toString() {
        return this.roomName+":"+this.getSceneName()+":"+this.getSceneNum();
    }

    @Override
    public int compareTo(Scene o) {
        return Comparators.NAME.compare(this, o);
    }

    public static class Comparators {

        public static Comparator<Scene> NAME = new Comparator<Scene>() {
            @Override
            public int compare(Scene o1, Scene o2) {
                return o1.toString().compareTo(o2.toString());
            }
        };
    }
}
