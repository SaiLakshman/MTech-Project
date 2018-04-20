package hbasechaindb.datamodel;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Ed25519Sha256Condition;
import org.interledger.cryptoconditions.Ed25519Sha256Fulfillment;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.ThresholdSha256Fulfillment;
import org.javatuples.Pair;
import com.google.gson.Gson;
import hbasechaindb.HbaseUtil;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class Transaction implements Serializable, Cloneable {
	
	public String id;
	private Operation operation;
	private List<Input> inputs;
	private List<Output> outputs;
	private Map asset;
	private Map metadata;
	
	public Transaction(Operation op,Map asset, List<Input> in, List<Output> out, Map metadata) {
		/*if((op == Operation.CREATE || op == Operation.GENESIS) &&  !asset.containsKey("data"))
			throw new RuntimeException("Create and Genesis asset must contain data");
		else if(op == Operation.TRANSFER && !asset.containsKey("id"))
			throw new RuntimeException("Transfer asset must have id");*/
		
		this.inputs= in;
		this.outputs= out;
		this.operation= op;
		this.metadata= metadata;
		this.asset= asset;
		System.out.println("start");
		this.id= calculateId();
		System.out.println("end");
	}

	public Transaction(String id, Operation op,Map asset, List<Input> in, List<Output> out, Map metadata) {
		this.inputs= in;
		this.outputs= out;
		this.operation= op;
		this.metadata= metadata;
		this.asset= asset;
		this.id= id;
	}
	
	public List<Input> getInputs() {
		return inputs;
	}


	public List<Output> getOutputs() {
		return outputs;
	}

	public Map getAsset() {
		return asset;
	}

	public Map getMetadata() {
		return metadata;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Transaction))
			return false;
		Transaction t= (Transaction) obj;
		if(!t.operation.equals(this.operation))
			return false;
		if(!t.asset.equals(this.asset))
			return false;
		if(t.metadata != null && this.metadata != null && !t.metadata.equals(this.metadata))
			return false;
		return true;
	}
	
	public String toString() {
		return HbaseUtil.getTransactionGson().toJson(this);
	}
	
	public Operation getOperation() {
		return this.operation;
	}
	
	public String calculateId() {
		Gson g= HbaseUtil.getTransactionGson();
		Transaction t= g.fromJson(g.toJson(this), Transaction.class);
		List<Fulfillment> fFillments= new ArrayList<Fulfillment>();
		for(Input in : t.inputs) {
			fFillments.add(in.getFulfillment());
			in.setFulfillment(null);
		}
		DigestSHA3 dg= new DigestSHA3(256);
		dg.update(HbaseUtil.getTransactionGson().toJson(t).getBytes());
		for(int i= 0; i < t.inputs.size(); i++)
			t.inputs.get(i).setFulfillment(fFillments.get(i));
		return Base58.encode(dg.digest());
	}
	
	public static Transaction prepareGenesisTransaction(ArrayList<PublicKey> signers) {
		ArrayList<Input> in= new ArrayList<Input>();
		ArrayList<Output> out= new ArrayList<Output>();
		
		in.add(new Input(null, signers, null));
		out.add(new Output(null, signers, 1));
		
		Map asset= new HashMap<String, String>();
		asset.put("id", "1");
		asset.put("data", HbaseUtil.getTransactionGson().toJson(signers));
		return new Transaction(Operation.GENESIS, asset, in, out, null);
	}
	
	public static Transaction prepareCreateTransaction(ArrayList<PublicKey> signers, List<Pair<ArrayList, Integer>> recipient, Map metadata, Map asset) {
		if(signers.size() == 0)
			throw new RuntimeException("Signers can't be 0");
		if(recipient.size() == 0)
			throw new RuntimeException("Recipient can't be 0");
		
		ArrayList<Output> outs= new ArrayList<>();
		ArrayList<Input> ins= new ArrayList<>();
		
		for(Pair p : recipient) {
			ArrayList l= (ArrayList) p.getValue0();
			int i= (int) p.getValue1();
			outs.add(Output.generate(l, i));
		}
		ins.add(new Input(null, signers, null));
		
		
		return new Transaction(Operation.CREATE, asset, ins, outs, metadata);
	}
	
	public static Transaction prepareCreateTransaction(ArrayList<PublicKey> signers, ArrayList<PublicKey> recipient, int amount, Map metadata, Map asset) {
		if(signers.size() == 0)
			throw new RuntimeException("Signers can't be 0");
		if(recipient.size() == 0)
			throw new RuntimeException("Recipient can't be 0");
		
		ArrayList<Output> outs= new ArrayList<>();
		ArrayList<Input> ins= new ArrayList<>();
		
		outs.add(Output.generate(recipient, amount));
		ins.add(new Input(null, signers, null));
		
		return new Transaction(Operation.CREATE, asset, ins, outs, metadata);
	}
	
	public static Transaction prepareTransferTransaction(List<Input> inputs, List<Pair<List, Integer>> recipient, String assetId, Map metadata) {
		if(inputs.size() == 0)
			throw new RuntimeException("Input can't be 0");
		if(recipient.size() == 0)
			throw new RuntimeException("Recipient can't be 0");
		
		ArrayList<Output> outs= new ArrayList<>();
		for(Pair p : recipient) {
			ArrayList l= (ArrayList) p.getValue0();
			int i= (int) p.getValue1();
			outs.add(Output.generate(l, i));
		}
		
		Map asset= new HashMap<String, String>();
		asset.put("id", assetId);
		return new Transaction(Operation.TRANSFER, asset, inputs, outs, metadata);
	}
	
	public static Transaction prepareTransferTransaction(ArrayList<Input> inputs, ArrayList<PublicKey> recipient, int amount, String assetId, Map metadata) {
		if(inputs.size() == 0)
			throw new RuntimeException("Input can't be 0");
		if(recipient.size() == 0)
			throw new RuntimeException("Recipient can't be 0");
		
		ArrayList<Output> outs= new ArrayList<>();
		
		outs.add(Output.generate(recipient, amount));
		
		Map asset= new HashMap<String, String>();
		asset.put("id", assetId);
		
		return new Transaction(Operation.TRANSFER, asset, inputs, outs, metadata);
	}
	
	public ArrayList<Input> createInputs(List<Integer> indices) {
		ArrayList<Input> ans= new ArrayList<>();
		if(indices == null) {
			indices= new ArrayList<>();
			for(int i= 0; i < this.outputs.size(); i++)
				indices.add(i);
		}
		for(int i : indices) {
			ans.add(new Input(null, this.outputs.get(i).getPublicKeys(), new TransactionLink(this.id, i)));
		}
		return ans;
	}
	
	public void addInput(Input in) {
		if(in == null)
			throw new RuntimeException("Input can't be empty");
		this.inputs.add(in);
	}
	
	public void addOutput(Output out) {
		if(out == null)
			throw new RuntimeException("Output can't be empty");
		this.outputs.add(out);
	}
	
	/**
	 * This method creates and assign fulfillments to the transaction for a particular input
	 * provided in the index argument. The private key passed in the argument should be in 
	 * the same order as the corresponding publickey in owner before of the input. 
	 * @param index of the input to be signed
	 * @param sk is the list of private key which will be used for signing (order matters)
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public void signInput(int index, List<PrivateKey> sk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if(inputs.get(index).getOwnerBefore().size() == 1)
			signSimpleSignatureFulfillment(inputs.get(index), sk.get(0));
		else if(inputs.get(index).getOwnerBefore().size() > 1)
			signThresholdSignatureFulfillment(inputs.get(index), sk);
		else
			throw new RuntimeException("Fulfillment couldn't be matched");
	}
	
	public void signSimpleSignatureFulfillment(Input in, PrivateKey sk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Ed25519Sha256Condition cond= new Ed25519Sha256Condition((EdDSAPublicKey) in.getOwnerBefore().get(0));
		
		MessageDigest md= MessageDigest.getInstance("SHA-512");
		Signature sign= new EdDSAEngine(md);
		sign.initSign(sk);
		sign.update(this.id.getBytes());
		Ed25519Sha256Fulfillment ffill= new Ed25519Sha256Fulfillment((EdDSAPublicKey) in.getOwnerBefore().get(0), sign.sign());
		
		in.setFulfillment(ffill);
	}
	
	public void signThresholdSignatureFulfillment(Input in, List<PrivateKey> sk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		List<Condition> subCond= new ArrayList<>();
		for(int i= 0; i < sk.size(); i++)
			subCond.add(new Ed25519Sha256Condition((EdDSAPublicKey) in.getOwnerBefore().get(i)));
		
		List<Fulfillment> ffills= new ArrayList<>();
		for(int i= 0; i < sk.size(); i++) {
			MessageDigest md= MessageDigest.getInstance("SHA-512");
			Signature sign= new EdDSAEngine(md);
			sign.initSign(sk.get(i));
			sign.update(this.id.getBytes());
			ffills.add(new Ed25519Sha256Fulfillment((EdDSAPublicKey) in.getOwnerBefore().get(i), sign.sign()));
		}
		
		in.setFulfillment(new ThresholdSha256Fulfillment(subCond,ffills));
	}
	

}
