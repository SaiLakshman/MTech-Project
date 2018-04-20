package privad.wallentExceptions;

public class UnknownChoice extends Exception {

    private String message;

    public UnknownChoice(String msg){
        message= msg;
    }

    @Override
    public String getMessage() {
        return "Unknown Choice Exception: Please choose the valid option.";
    }
}
