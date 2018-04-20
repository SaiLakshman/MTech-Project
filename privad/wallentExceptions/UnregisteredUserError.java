package privad.wallentExceptions;

public class UnregisteredUserError extends Exception {
    public UnregisteredUserError() {
        super();
    }

    public UnregisteredUserError(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Unregistered User Error: Caused by invalid register login.";
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }
}
