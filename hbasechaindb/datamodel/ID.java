package hbasechaindb.datamodel;

import java.io.Serializable;

public class ID implements Serializable {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
