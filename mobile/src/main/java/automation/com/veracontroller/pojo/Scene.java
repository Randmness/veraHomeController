package automation.com.veracontroller.pojo;

import java.io.Serializable;

/**
 * Created by root on 2/22/15.
 */
public class Scene implements Serializable {
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

}
