package privad.transactionsOperations;

import privad.Util.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import hbasechaindb.HbaseUtil;
import hbasechaindb.backend.Query;
import hbasechaindb.datamodel.Transaction;
import net.corda.core.crypto.Base58;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class MyTransactions {

	private User user;

	public MyTransactions(User user) {
		this.user = user;
	}

	public static String getFedNodes() throws JsonSyntaxException, IOException {
		Gson g= HbaseUtil.getTransactionGson();
		BufferedReader pub= new BufferedReader(new InputStreamReader(new FileInputStream("conf/keyRing.txt")));
		ArrayList<PublicKey> pk= HbaseUtil.getTransactionGson().fromJson(pub.readLine(), new TypeToken<ArrayList<PublicKey>>(){}.getType());
		Base58 bf= new Base58();
		return bf.encode(pk.get(0).getEncoded());
	}

	public String createTransaction() throws Exception{
		ArrayList<PublicKey> signer= new ArrayList<PublicKey>();
		Base58 base58= new Base58();
		X509EncodedKeySpec specPub= new X509EncodedKeySpec(base58.decode(this.user.getPublicKey()));
		EdDSAPublicKey pub_Key = new EdDSAPublicKey(specPub);
		signer.add(pub_Key);

		PKCS8EncodedKeySpec specPri = new PKCS8EncodedKeySpec(base58.decode(this.user.getPrivateKey()));
		EdDSAPrivateKey priv_Key = new EdDSAPrivateKey(specPri);
		ArrayList<PrivateKey> sk= new ArrayList<>();
		sk.add(priv_Key);

		Asset_MetaData asset= new Asset_MetaData();
		//asset.loadData(new File("/home/sailakshman/Desktop/MedRec/patients_data.dat"));
		asset.loadImage("/home/sailakshman/Desktop/MedRec/wallet/data/2.mp4");
		ArrayList<String> allowedKeys= new ArrayList<>();
		allowedKeys.add(this.user.getPublicKey());
		asset.loadMetaData(allowedKeys, "Created Asset", OperationType.CREATE);
		
		Transaction t= Transaction.prepareCreateTransaction(signer, signer, 1, asset.getMetaData(), asset.getAsset());
		t.signInput(0, sk);
		Query q= Query.getInstance();

		//Should give Transaction and federation node public key as the arguments.
		String fedNode= getFedNodes();
		long start= System.currentTimeMillis();
		String assetId= q.insertInBackLog(t, fedNode);
        String[] id= assetId.split("_");
		System.out.println("Transaction Created with id: " + id[1] + " Time Taken: " + (System.currentTimeMillis() - start) + ":: " +(System.currentTimeMillis() - start)/1000);

		return id[1];
	}

	public Transaction retrieveAsset(String id) throws IOException
	{
		Query q= Query.getInstance();
		long start= System.currentTimeMillis();
		Transaction t = q.getTransaction(id);
		System.out.println(" Time Taken: " + (System.currentTimeMillis() - start) + ":: " +(System.currentTimeMillis() - start)/1000);
		return t;

	}

	public void transactionChoice() throws IOException{
		Scanner scanner= new Scanner(System.in);

		String operation;
		System.out.println("Available Trasactions: ");
		System.out.println("1) create\n" +
				"2) retrieve");

		while (true){
			operation= scanner.nextLine();
			if (operation.equals("exit"))
				break;
			else{
				if(operation.equals("create")){
					String id;
					try {
						long start= System.currentTimeMillis();
						id = createTransaction();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(operation.equals("retrieve")){
					String id;
					System.out.println("Enter the asset id: ");
					id= scanner.nextLine();
					Transaction t= retrieveAsset(id);
					String asset= (String) t.getAsset().get("NAME");
					System.out.println("Asset: "+ asset);
				}
			}
		}
	}
}