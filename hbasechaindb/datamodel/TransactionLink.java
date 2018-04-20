package hbasechaindb.datamodel;

import java.io.Serializable;

public class TransactionLink implements Serializable{
	private int outputIndex;
	private String transId;
	
	public TransactionLink(String out, int indx) {
		this.transId= out;
		this.outputIndex= indx;
	}
	
	public int getOutputIndex() {
		return outputIndex;
	}
	public void setOutputIndex(int outputIndex) {
		this.outputIndex = outputIndex;
	}
	public String getTransId() {
		return transId;
	}
	public void setTransId(String output) {
		this.transId = output;
	}
	
}
