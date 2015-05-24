package automation.com.veracontroller.enums;

import android.util.Log;

/**
 * Created by mrand on 5/16/15.
 */
public enum VeraType {
    VERA_UI5("UI5"),
    VERA_UI7("UI7");

    private String veraType;

    VeraType(String veraType) {

        this.veraType = veraType;
    }

    public static VeraType fromType(String veraType) {
        for (VeraType type : values()) {
            if (type.toString().equalsIgnoreCase(veraType)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public String toString() {

        return this.veraType;
    }
}
