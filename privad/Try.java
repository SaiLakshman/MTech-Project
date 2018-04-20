package privad;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.Base58;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import hbasechaindb.HbaseUtil;
import hbasechaindb.backend.Query;
import hbasechaindb.datamodel.Transaction;
import net.i2p.crypto.eddsa.KeyPairGenerator;


public class Try {
	public static ArrayList<KeyPair> kps;

	public static ArrayList<KeyPair> generateKeys(int n) {
		KeyPairGenerator kpg= new KeyPairGenerator();
		ArrayList<KeyPair> keyPairs= new ArrayList<>();
		for(int i= 0; i < n; i++)
			keyPairs.add(kpg.generateKeyPair());
		return keyPairs;
	}
	
	public static String getFedNodes() throws JsonSyntaxException, IOException {
		Gson g= HbaseUtil.getTransactionGson();
		BufferedReader pub= new BufferedReader(new InputStreamReader(new FileInputStream("conf/keyRing.txt")));
		ArrayList<PublicKey> pk= HbaseUtil.getTransactionGson().fromJson(pub.readLine(), new TypeToken<ArrayList<PublicKey>>(){}.getType());
		Base58 bf= new Base58();
		return bf.encode(pk.get(0).getEncoded());
	}
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, JsonSyntaxException, IOException
	{
		String fedNode= getFedNodes();
		Query q= Query.getInstance();
		Base58 bf= new Base58();
		kps= generateKeys(2);
		
		int d= 0/*(rnd.nextInt(kps.size()))*/;
		System.out.println(kps.get(d).getPublic());
		System.out.println(kps.get(d).getPrivate());
		Map<String,String> asset= new HashMap<String,String>();
		asset.put("data","Hello this is lakshman");

		ArrayList<PublicKey> signer= new ArrayList<PublicKey>();
		signer.add(kps.get(d).getPublic());
		System.out.println(signer);

		ArrayList<PrivateKey> sk= new ArrayList<>();
		sk.add(kps.get(d).getPrivate());

		ArrayList<PublicKey> r= new ArrayList<PublicKey>();
		r.add(kps.get(1/*((d+3)) % kps.size()*/).getPublic());
		System.out.println(r);
      /*  Map <String, String> metadata= new HashMap<>();
        List<PublicKey>allowed_keys;
        allowed_keys.add((PublicKey) signer);
        metadata.put("allowed_keys",allowed_keys);*/
		Transaction t= Transaction.prepareCreateTransaction(signer,r, 1 , null ,asset);
		t.signInput(0, sk);
	}

}
