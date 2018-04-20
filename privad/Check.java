package privad;

import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;
import org.bitcoinj.core.Base58;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Check {

    public static byte[] getEncryptedPassword(String password, byte[] salt,  int iterations,  int derivedKeyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength * 8);

        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return f.generateSecret(spec).getEncoded();
    }

    public static void main(String[] args) throws Exception {
        String s= SCryptUtil.scrypt("sairam",16384,8,1);
        String s1= SCryptUtil.scrypt("sairam",16384,8,1);

        System.out.println(SCryptUtil.check("sairam",s));
        System.out.println(SCryptUtil.check("sairam",s1));
        Base58 base58= new Base58();

        System.out.println(getEncryptedPassword("sairam","Salt".getBytes(),1200,16).length);
        System.out.println(base58.encode(getEncryptedPassword("sairam","Salt".getBytes(),1200,16)));
        System.out.println(base58.encode(getEncryptedPassword("sairam","Salt".getBytes(),1200,16)));

    }
}
