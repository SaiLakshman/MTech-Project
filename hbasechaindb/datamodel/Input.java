package hbasechaindb.datamodel;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

import org.interledger.cryptoconditions.Fulfillment;

public class Input implements Serializable{
	private List<PublicKey> ownerBefore;
	private Fulfillment fulfillment;
	private TransactionLink fulfill;
	
	public Input(Fulfillment ffillments, List<PublicKey> ownerBefore, TransactionLink ffills) {
		this.fulfillment= ffillments;
		this.ownerBefore= ownerBefore;
		this.fulfill= ffills;
	}
	
	
	public List<PublicKey> getOwnerBefore() {
		return ownerBefore;
	}
	public void setOwnerBefore(List<PublicKey> ownerBefore) {
		this.ownerBefore = ownerBefore;
	}
	public Fulfillment getFulfillment() {
		return fulfillment;
	}
	public void setFulfillment(Fulfillment fulfillment) {
		this.fulfillment = fulfillment;
	}
	public TransactionLink getFulfill() {
		return fulfill;
	}
	public void setFulfill(TransactionLink fulfill) {
		this.fulfill = fulfill;
	}

}
