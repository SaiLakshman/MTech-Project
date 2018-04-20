package privad.Util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;

public class RegisteredUsers {
    public static JSONObject registedUsers= new JSONObject();

    public static boolean isRegistered(String userName){
        File file= new File("/home/sailakshman/Desktop/MedRec/register.json");

        try {
            JSONParser jsonParser= new JSONParser();

            registedUsers =  (JSONObject) jsonParser.parse(new FileReader(file));

            if(registedUsers.containsKey(userName))
                return true;
            else return false;
        }catch (Exception e){
            return false;
        }
    }
}
