package privad.wallentExceptions;

public class IncorrectPasswordError extends Exception {
    String message= null;

    public IncorrectPasswordError() {
        super();
    }

    public IncorrectPasswordError(String message) {
        this.message= message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
