package privad.wallentExceptions;

public class UserAlreadyExistsError extends Exception {

    public UserAlreadyExistsError() {
        super();
    }

    public UserAlreadyExistsError(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "User Already Exists Error";
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }
}
