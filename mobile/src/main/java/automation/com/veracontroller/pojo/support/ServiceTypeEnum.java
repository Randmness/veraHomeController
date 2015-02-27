package automation.com.veracontroller.pojo.support;

/**
 * Created by root on 2/22/15.
 */
public enum ServiceTypeEnum {
    SWITCH_LIGHT("urn:upnp-org:serviceId:SwitchPower1"),
    SCENE("urn:micasaverde-com:serviceId:HomeAutomationGateway1");

    String serviceType;

    ServiceTypeEnum(String serviceType) {
        this.serviceType = serviceType;
    }

    public String toString() {
        return serviceType;
    }
}
