package automation.com.veracontroller.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by root on 2/22/15.
 */
public class Scene implements Parcelable {
    private int sceneNum;
    private String sceneName;

    public Scene(int sceneNum, String sceneName) {
        this.sceneNum = sceneNum;
        this.sceneName = sceneName;
    }

    public String getSceneName() {
        return this.sceneName;
    }

    public int getSceneNum() {
        return this.sceneNum;
    }

    public Scene(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);
        this.sceneNum = Integer.parseInt(data[0]);
        this.sceneName = data[1];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {String.valueOf(this.sceneNum), this.sceneName});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Scene createFromParcel(Parcel in) {
            return new Scene(in);
        }

        public Scene[] newArray(int size) {
            return new Scene[size];
        }
    };
}
