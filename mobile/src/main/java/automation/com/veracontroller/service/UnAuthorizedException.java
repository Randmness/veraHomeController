package automation.com.veracontroller.service;

/**
 * Created by mrand on 5/17/15.
 */
public class UnAuthorizedException extends Exception {

    public UnAuthorizedException(String reason) {
        super(reason);
    }

    public UnAuthorizedException(Throwable reason) {
        super(reason);
    }
}
