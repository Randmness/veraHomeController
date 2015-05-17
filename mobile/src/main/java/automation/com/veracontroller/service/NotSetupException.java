package automation.com.veracontroller.service;

/**
 * Created by mrand on 5/17/15.
 */
public class NotSetupException extends Exception {

    public NotSetupException(String reason) {
        super(reason);
    }

    public NotSetupException(Throwable reason) {
        super(reason);
    }
}
