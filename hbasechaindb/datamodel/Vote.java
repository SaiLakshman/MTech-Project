package hbasechaindb.datamodel;

import java.io.Serializable;
import java.security.PublicKey;

import hbasechaindb.HbaseUtil;

public class Vote implements Serializable{
	public PublicKey voter;
	public VoteDetails details;
	public String signature;
	
	public Vote (PublicKey voter, String blockId, String prevVote, boolean validity) {
		details= new VoteDetails();
		details.voteFor= blockId;
		details.prevVoted= prevVote;
		details.isValid= validity;
		this.voter= voter;
	}
	
	public byte[] getDetailsEncoded() {
		return HbaseUtil.getTransactionGson().toJson(details).getBytes();
	}
	
	public void setSignature(String sign) {
		this.signature= sign;
	}
	
	public String getBlockId() {
		return details.voteFor;
	}
	
	public boolean getValidity() {
		return details.isValid;
	}
	
	public String toString() {
		return HbaseUtil.getTransactionGson().toJson(this);
	}
}

class VoteDetails implements Serializable{
	public String voteFor;
	public String prevVoted;
	public boolean isValid;
}