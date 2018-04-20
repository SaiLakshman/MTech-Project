package hbasechaindb.datamodel;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

import hbasechaindb.HbaseUtil;

public class Block {
	public String id;
	BlockDetails details;
	String signature;
	
	public Block(List<String> trans, PublicKey creater, ArrayList<PublicKey> voters) {
		details= new BlockDetails();
		details.creater= creater;
		details.transactions= trans;
		details.voters= voters;
		
		DigestSHA3 dg= new DigestSHA3(256);
		dg.update(HbaseUtil.getTransactionGson().toJson(details).getBytes());
		
		Base58 bf= new Base58();
		id= System.currentTimeMillis() + "-" +bf.encode(dg.digest());
	}
	
	public byte[] getBlockDetailsEncoded() {
		return HbaseUtil.getTransactionGson().toJson(details).getBytes();
	}
	
	public List<String> getTransactionIds() {
		return details.transactions;
	}
	
	public void setSignature(String sign) {
		this.signature= sign;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String toString() {
		return HbaseUtil.getTransactionGson().toJson(this);
	}
	
}

class BlockDetails {
	List<String> transactions;
	PublicKey creater;
	List<PublicKey> voters;
}
