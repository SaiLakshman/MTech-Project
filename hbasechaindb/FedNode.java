package hbasechaindb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Ed25519Sha256Condition;
import org.interledger.cryptoconditions.ThresholdSha256Condition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.concurrent.TimeUnit;
import hbasechaindb.backend.Query;
import hbasechaindb.datamodel.*;
import net.corda.core.crypto.Base58;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class FedNode {
	Query q;
	KeyPair keyPair;
	String pKeyString; 
	String prevVote;
	Signature sign;
	Base58 bf;
	int nTrans= 0, nBlocks= 0;
	
	public FedNode(KeyPair key) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		this.q= Query.getInstance();
		this.keyPair= key;
		Base58 bf= new Base58();
		pKeyString= bf.encode(keyPair.getPublic().getEncoded());
		MessageDigest md= MessageDigest.getInstance("SHA-512");
		this.sign= new EdDSAEngine(md);
		sign.initSign(keyPair.getPrivate());
		this.bf= new Base58();
	}
	
	public String voteGenesis() throws IOException, SignatureException {
		String genesis= q.getGenesis();
		Vote v= new Vote(keyPair.getPublic(), genesis, null, true);
		sign.update(v.getDetailsEncoded());
		v.setSignature(bf.encode(sign.sign()));
		q.insertToVote(v);
		return genesis;
	}
	
	public void createBlock() throws IOException, SignatureException, InterruptedException {
		List<Transaction> trans= q.getFromBackLog(pKeyString);
		Vector<String> transIds= new Vector<>();
		Vector<String> delete= new Vector<>();
		Vector<Transaction> validTransactions= new Vector<>();
		
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(Transaction t : trans) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						if(isValidTransaction(t)) {
							validTransactions.add(t);
							transIds.add(t.id);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					delete.add(pKeyString+"_"+t.id);
				}
			});
		}
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.MINUTES);
		
		trans= null;
		
		if(transIds.size() == 0)
			return;
		
		//create a block, sign
		Block b= new Block(transIds, keyPair.getPublic(), null);
		sign.update(b.id.getBytes());
		b.setSignature(bf.encode(sign.sign()));
		
		//insert the newly created block
		q.insertBlock(b);
		q.insertBlockOfTransactions(validTransactions, b.id);
		
		//update toVote table 
		Thread th1= new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					q.insertToToVote(transIds, b);
				} catch (IOException e) {
				}
			}
		});
		th1.start();
		
		//vote for the created block
		Vote v= new Vote(keyPair.getPublic(), b.getId(), this.prevVote, true);
		sign.update(v.getDetailsEncoded());
		v.setSignature(bf.encode(sign.sign()));
		q.insertToVote(v);
		this.prevVote= b.id;
		
		//delete the processed transactions from backlog 
		Thread th2= new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					q.deleteFromBackLog(delete);
				} catch (IOException e) {
				}
			}
		});
		th2.start();
	}
	
	public void voteBlock() throws IOException, SignatureException {
		List<String> toVote= q.getFromToVote(pKeyString);
		System.out.println(toVote.size());
		if(toVote.size() == 0) return;
		for(String str : toVote) {
			String blockId= str.split("_")[1];
			List<Transaction> trans= q.getTransactionsWithPrefix(blockId);
			boolean isValid= true;
			for(Transaction t : trans) {
				if(!isValidTransaction(t)) {
					isValid= false;
					break;
				}
			}
			Vote v= new Vote(keyPair.getPublic(), blockId, prevVote, isValid);
			sign.update(v.getDetailsEncoded());
			v.setSignature(bf.encode(sign.sign()));
			q.insertToVote(v);
			q.deleteFromToVote(str);
		}
		
	}
	
	public boolean isValidTransaction(Transaction tran) throws IOException {
		if(tran.getOperation().equals(Operation.TRANSFER)) {
			System.out.println("validity : "+isValidTrasfer(tran));
			return isValidTrasfer(tran);
		}
		System.out.println("validity : "+isValidCreation(tran));
		return isValidCreation(tran);
	}
	
	public boolean isValidCreation(Transaction tran) {
		for(Input in : tran.getInputs()) {
			
			if(in.getOwnerBefore().size() == 1) {
				Ed25519Sha256Condition cond= new Ed25519Sha256Condition((EdDSAPublicKey) in.getOwnerBefore().get(0));
				if(!in.getFulfillment().verify(cond, tran.id.getBytes()))
						return false;
			} 
			else {
				List<Condition> subCond= new ArrayList<Condition>();
				for(int i= 0; i < in.getOwnerBefore().size(); i++)
					subCond.add(new Ed25519Sha256Condition((EdDSAPublicKey) in.getOwnerBefore().get(i)));
				ThresholdSha256Condition cond= new ThresholdSha256Condition(in.getOwnerBefore().size(), subCond);
				if(!in.getFulfillment().verify(cond, tran.id.getBytes()))
					return false;
			}
		}
			
		return true;
	}
	
	public boolean isValidTrasfer(Transaction tran) throws IOException {
		List<Transaction> prev; 

		for(Input in : tran.getInputs()) {
			TransactionLink ln= in.getFulfill();
			Transaction t= q.getTransaction(ln.getTransId());
			Output out= t.getOutputs().get(ln.getOutputIndex());
			if(!out.getPublicKeys().equals(in.getOwnerBefore()))
				return false;
			if(!in.getFulfillment().verify(out.getCondition(), tran.id.getBytes()))
				return false;
			if(isDoubleSpent(in))
				return false;
		}
		return true;
	}
	
	public boolean isDoubleSpent(Input in) {
		
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		Gson g= HbaseUtil.getTransactionGson();
		BufferedReader pub= new BufferedReader(new InputStreamReader(new FileInputStream("conf/keyRing.txt")));
		BufferedReader pri= new BufferedReader(new InputStreamReader(new FileInputStream("conf/priKeys.txt")));
		ArrayList<PublicKey> pk= HbaseUtil.getTransactionGson().fromJson(pub.readLine(), new TypeToken<ArrayList<PublicKey>>(){}.getType());
		ArrayList<PrivateKey> sk= HbaseUtil.getTransactionGson().fromJson(pri.readLine(), new TypeToken<ArrayList<PrivateKey>>(){}.getType());
		KeyPair kp= new KeyPair(pk.get(0), sk.get(0));
		
		FedNode fd= new FedNode(kp);
		fd.prevVote= fd.voteGenesis();
		System.out.println(fd.prevVote);
		System.out.println(fd.pKeyString);
		
		try {
			fd.createBlock();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Thread th= new Thread(new Runnable() {
			@Override
			public void run() {
				while(true)
					try {
						fd.createBlock();
						TimeUnit.SECONDS.sleep(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});
		th.start();
		
		/*while(true)
			fd.voteBlock();*/
		
		
	}
	
}
