package hbasechaindb.datamodel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.generated.thrift.thrift_jsp;
import org.apache.log4j.net.SocketNode;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Ed25519Sha256Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.ThresholdSha256Condition;

import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

public class Output implements Serializable {
	public static int MAX_AMOUNT = 9 * 10 * 18;
	private ArrayList<PublicKey> publicKeys;
	private int amount;
	private Condition condition;
	
	public Output(Condition cond, ArrayList<PublicKey> keys, int amount) {
		if(keys.size() == 0)
			throw new RuntimeException("Should have atleast 1 key");
		if(amount < 1)
			throw new RuntimeException("Amount should be more than 0");
		if(amount > MAX_AMOUNT)
			throw new RuntimeException("amount limit exceeded");
		
		this.publicKeys=  keys;
		this.amount= amount;
		this.condition= cond;
	}
	
	
	public static Output generate(ArrayList<PublicKey> keys, int amount) {
		if(keys.size() == 0)
			throw new RuntimeException("Should have atleast 1 key");
		if(amount < 1)
			throw new RuntimeException("Amount should be more than 0");
		
		Condition cond;
		if(keys.size() == 1) {
			cond= new Ed25519Sha256Condition((EdDSAPublicKey) keys.get(0));
			return new Output(cond, (ArrayList<PublicKey>) keys, amount);
		}
		else {
			List<Condition> subCond= new ArrayList<Condition>();
			for(int i= 0; i < keys.size(); i++)
				subCond.add(new Ed25519Sha256Condition((EdDSAPublicKey) keys.get(i)));
			cond= new ThresholdSha256Condition(keys.size(), subCond);
		}
		return new Output(cond, keys, amount);
	}
	
	public Condition getCondition() {
		return this.condition;
	}
	
	public List<PublicKey> getPublicKeys() {
		return publicKeys;
	}
	public void setPublicKeys(ArrayList<PublicKey> publicKeys) {
		this.publicKeys = publicKeys;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
}
