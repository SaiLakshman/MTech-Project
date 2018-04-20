package privad.Util;
import com.lambdaworks.crypto.SCryptUtil;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import privad.transactionsOperations.MyTransactions;
import privad.wallentExceptions.IncorrectPasswordError;
import privad.wallentExceptions.UnregisteredUserError;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;

public class User implements Serializable {
    private static Scanner scanner= new Scanner(System.in);
    private String userName;
    private String password;
    private String privateKey;
    private String publicKey;
    private String symmetricKeys;

    public User(){
        super();
    }

    public User(String userName, String password, String privateKey, String publicKey, String symmetricKeys) {
        this.userName = userName;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.symmetricKeys = symmetricKeys;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSymmetricKeys() {
        return symmetricKeys;
    }

    public void setSymmetricKeys(String symmetricKeys) {
        this.symmetricKeys = symmetricKeys;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public User makeUserObjectFromJson(JSONObject object){
        this.setUserName(object.get("username").toString());
        this.setPassword(object.get("password").toString());
        this.setPublicKey(object.get("publickey").toString());
        this.setPrivateKey(object.get("privatekey").toString());
        this.setSymmetricKeys(null);
        return this;
    }

    /***
     * Create user object with all required parameters.
     * @param userName
     * @param password
     * @return
     */
    public User getUserDetails(String userName, String password) throws UnregisteredUserError, IncorrectPasswordError{
            if(RegisteredUsers.isRegistered(userName)){

                User user= makeUserObjectFromJson((JSONObject)RegisteredUsers.registedUsers.get(userName));
                System.out.println(user.getPassword());
                if(SCryptUtil.check(password,user.getPassword()))
                    return user;
                else
                    throw new IncorrectPasswordError("Incorrect password");
            }
            throw new UnregisteredUserError("Unregistered User Error");
    }

    public void login() throws Exception{
        System.out.print("Enter UserName: ");
        String userName= scanner.nextLine();
        System.out.print("Enter Password: ");
        String password= scanner.nextLine();

        //Returns the full details of the user if the credentials are correct.
        try{
            User user= getUserDetails(userName,password);
            if(user != null){
                MyTransactions transaction= new MyTransactions(this);
                transaction.transactionChoice();
            }
        }catch (UnregisteredUserError e){
            System.out.println("Unregistered User: " + e.getMessage());
        }

    }

    public void registerUser() throws Exception {
        System.out.print("Enter username to register: ");
        setUserName(scanner.nextLine());
        do {
            try {
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                System.out.print("Confirm the password: ");
                if (password.equals(scanner.nextLine())) {
                    setPassword( SCryptUtil.scrypt(password,16384,8,1));
                    store_register();
                    break;
                } else {
                    throw new IncorrectPasswordError("Passwords do not match exception.");
                }
            }catch (IncorrectPasswordError e){
                continue;
            }
        }while (true);
    }


    public JSONObject makeJsonObjectFromUser(){
        JSONObject user= new JSONObject();
        user.put("username",getUserName());
        user.put("password",getPassword());
        user.put("publickey",getPublicKey());
        user.put("privatekey",getPrivateKey());
        user.put("symmetrickey",getSymmetricKeys());
        return user;
    }

    public boolean store_register() throws Exception{
        Base58 base58= new Base58();

        if(!RegisteredUsers.isRegistered(getUserName())){
            KeyPairGenerator keyGen= new KeyPairGenerator();
            KeyPair pair= keyGen.generateKeyPair();
            setPrivateKey(base58.encode(pair.getPrivate().getEncoded()));
            setPublicKey(base58.encode(pair.getPublic().getEncoded()));
            setSymmetricKeys(base58.encode(CryptoUtilities.getSymmetricKeyPassword(getPassword(),"Salt".getBytes(),1200,16)));

            RegisteredUsers.registedUsers.put(getUserName(), makeJsonObjectFromUser());
            File file= new File("/home/sailakshman/Desktop/MedRec/register.json");
            PrintWriter writer= new PrintWriter(new FileOutputStream(file));
            writer.write(RegisteredUsers.registedUsers.toJSONString());
            writer.close();
        }

        return true;
    }

    @Override
    public String toString() {
        return "username: " + getUserName() + " Password: " + getPassword() + " Public Key: " + getPublicKey()
                + " Private Key: " + getPrivateKey() + " Symmentric Key: " + getSymmetricKeys();
    }
}
