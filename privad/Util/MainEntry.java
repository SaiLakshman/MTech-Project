package privad.Util;

import privad.wallentExceptions.UnknownChoice;
import java.util.Scanner;

public class MainEntry {

    Scanner scanner= new Scanner(System.in);
    //Login/Register User Object.
    User user= new User();

    public void login_register() throws UnknownChoice, Exception{

        System.out.print("Login or Register: ");
        String choice= scanner.nextLine();
        if(choice.toLowerCase().equals("login"))
            user.login();
        else if(choice.toLowerCase().equals("register")){
            user.registerUser();
        }else{
            throw new UnknownChoice("Selection option is not found... Please choose the valid option.");
        }
    }

    public static void main(String[] args) throws Exception {
        MainEntry mainEntry= new MainEntry();

        while(true) {
            mainEntry.login_register();
        }
    }
}

