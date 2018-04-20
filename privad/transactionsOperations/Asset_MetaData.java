package privad.transactionsOperations;

import org.apache.avro.data.Json;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class Asset_MetaData {
    private enum AssetFields {ADDRESS, NAME, SPECIALTY_CODE, OPERATION, PRE_OP_INV, DETAILS, STATE, OPP_DATE, PATIENT_ID,
        SEX, DISCHDATE, DISTRICT, PHONE, ADVICE, ADMDATE, LONG_DESC}

    private Map<String, MetaData> metaData= new HashMap<String, MetaData>();
    private Map<String, String> asset= new HashMap<String, String>();
    
    public void loadImage(String file) throws IOException {
    	JSONObject json= new JSONObject();
    	byte[] data= Files.readAllBytes(Paths.get(file));
    	json.put("image", Base64.getEncoder().encodeToString(data).toString());
    	asset.put("image", json.toJSONString());
    }
    
    public void loadData(File file) throws Exception{
        JSONParser jsonParser= new JSONParser();

        JSONArray data =  (JSONArray) jsonParser.parse(new FileReader(file));

        JSONObject obj= (JSONObject)data.get(0);
        System.out.println("Address: " + obj.get(AssetFields.ADDRESS.toString()));
        asset.put(AssetFields.ADDRESS.toString(),obj.get(AssetFields.ADDRESS.toString()).toString());
        asset.put(AssetFields.ADMDATE.toString(),obj.get(AssetFields.ADMDATE.toString()).toString());
        asset.put(AssetFields.ADVICE.toString(),obj.get(AssetFields.ADVICE.toString()).toString());
        asset.put(AssetFields.DETAILS.toString(),obj.get(AssetFields.DETAILS.toString()).toString());
        asset.put(AssetFields.DISCHDATE.toString(), obj.get(AssetFields.DISCHDATE.toString()).toString());
        asset.put(AssetFields.DISTRICT.toString(), obj.get(AssetFields.DISTRICT.toString()).toString());
        asset.put(AssetFields.LONG_DESC.toString(), obj.get(AssetFields.LONG_DESC.toString()).toString());
        asset.put(AssetFields.NAME.toString(), obj.get(AssetFields.NAME.toString()).toString());
        asset.put(AssetFields.OPERATION.toString(), obj.get(AssetFields.OPERATION.toString()).toString());
        asset.put(AssetFields.OPP_DATE.toString(), obj.get(AssetFields.OPP_DATE.toString()).toString());
        asset.put(AssetFields.PATIENT_ID.toString(), obj.get(AssetFields.PATIENT_ID.toString()).toString());
        asset.put(AssetFields.PHONE.toString(), obj.get(AssetFields.PHONE.toString()).toString());
        asset.put(AssetFields.PRE_OP_INV.toString(), obj.get(AssetFields.PRE_OP_INV.toString()).toString());
        asset.put(AssetFields.SEX.toString(), obj.get(AssetFields.SEX.toString()).toString());
        asset.put(AssetFields.SPECIALTY_CODE.toString(), obj.get(AssetFields.SPECIALTY_CODE.toString()).toString());
        asset.put(AssetFields.STATE.toString(), obj.get(AssetFields.STATE.toString()).toString());
    }

    public void loadMetaData(ArrayList<String> allowedKeys, String desc, OperationType type){
        MetaData metaData= new MetaData();
        metaData.setAllowedKeys(allowedKeys);
        metaData.setDescription(desc);
        metaData.setOperationType(type);
        this.metaData.put("info", metaData);
    }

    public Map<String, MetaData> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, MetaData> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getAsset() {
        return asset;
    }

    public void setAsset(Map<String, String> asset) {
        this.asset = asset;
    }

    public static void main(String[] args) throws Exception{
        Asset_MetaData asset_metaData= new Asset_MetaData();
        asset_metaData.loadData(new File("/home/sailakshman/Desktop/MedRec/patients_data.dat"));
        System.out.println("Address: " + asset_metaData.asset.get(AssetFields.ADDRESS.toString()));
    }

}
