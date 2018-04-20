package privad.transactionsOperations;

import java.io.Serializable;
import java.util.ArrayList;

public class MetaData implements Serializable {
    private ArrayList<String> allowedKeys= new ArrayList<String>();
    private String description;
    private OperationType operationType;

    public ArrayList<String> getAllowedKeys() {
        return allowedKeys;
    }

    public void setAllowedKeys(ArrayList<String> allowedKeys) {
        this.allowedKeys = allowedKeys;
    }

    public void addAllowedKey(String key){
        this.allowedKeys.add(key);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
}
