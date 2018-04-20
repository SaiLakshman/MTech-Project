package hbasechaindb.backend;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.PublicKey;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.bitcoinj.core.Base58;
import org.jruby.compiler.ir.operands.Array;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.scenario.effect.impl.prism.PrImage;

import hbasechaindb.HbaseUtil;
import hbasechaindb.datamodel.Block;
import hbasechaindb.datamodel.Input;
import hbasechaindb.datamodel.Operation;
import hbasechaindb.datamodel.Output;
import hbasechaindb.datamodel.Transaction;
import hbasechaindb.datamodel.Vote;
import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import sun.security.jca.GetInstance;

public class Query {

	Configuration conf;
	Connection connection;
	static int i = 8;
	private static Query q;
	
	private Query() throws IOException {
		this.conf= HBaseConfiguration.create();
		conf.set("hbase.client.keyvalue.maxsize","0");
		this.connection = ConnectionFactory.createConnection(conf);
	}
	
	public static Query getInstance() throws IOException {
		if(q == null)
			q= new Query();
		return q;
	}

	/*public String insertTransaction(Transaction trans, PublicKey key) throws IOException {
		String rowId= HbaseUtil.getTransactionGson().toJson(key) + "_" + trans.calculateId();

		Table t= connection.getTable(TableName.valueOf("backLog"));

		Put p= new Put(Bytes.toBytes(rowId));
		p.add(Bytes.toBytes("transactions"), Bytes.toBytes("transaction"), Bytes.toBytes(trans.toString()));

		t.put(p);
		t.close();
		return rowId;
	}*/

	public String insertInBackLog(Transaction trans, String fedNode) throws IOException {
		String rowId= fedNode + "_" + trans.id;

		Table t= connection.getTable(TableName.valueOf("backLog"));

		Put p= new Put(Bytes.toBytes(rowId));
		p.add(Bytes.toBytes("transactions"), Bytes.toBytes("transaction"), Bytes.toBytes(trans.toString()));

		t.put(p);
		t.close();
		return rowId;
	}
	
	public List<String> insertInBackLog(List<Transaction> trans, String fedNode) throws IOException {
		ArrayList<Put> puts= new ArrayList<>();
		ArrayList<String> ans= new ArrayList<>();
		Table table= connection.getTable(TableName.valueOf("backLog"));
		for(Transaction t : trans) {
			String str= fedNode+"_"+t.id;
			Put p= new Put((str).getBytes());
			p.add(Bytes.toBytes("transactions"), Bytes.toBytes("transaction"), Bytes.toBytes(t.toString()));
			puts.add(p);
			ans.add(str);
		}
		
		table.put(puts);
		table.close();
		return ans;
	}

	public List<Transaction> getFromBackLog(String fedNode) throws IOException {
		ResultScanner rs= getRowsWithPrefix(fedNode, "backLog");
		byte[] b;
		List<Transaction> ans= new ArrayList();

		for(Result r : rs) {
			b= r.getValue(Bytes.toBytes("transactions"), Bytes.toBytes("transaction"));
			ans.add(HbaseUtil.getTransactionGson().fromJson(Bytes.toString(b), Transaction.class));
		}
		rs.close();
		return ans;
	}

	public void deleteFromBackLog(List<String> rowIds) throws IOException {
		Table t= connection.getTable(TableName.valueOf("backLog"));
		List<Delete> deletes= new ArrayList<>();
		for(String st : rowIds) 
			deletes.add(new Delete(Bytes.toBytes(st)));

		t.delete(deletes);
		t.close();

	}

	public void insertBlockOfTransactions(List<Transaction> trans, String blockId) throws IOException {
		Table table= connection.getTable(TableName.valueOf("hbaseChainDB"));
		Base58 b58= new Base58();
		Gson g= HbaseUtil.getTransactionGson();
		ArrayList<Put> puts= new ArrayList<>();

		for(Transaction t : trans) {
			Put p= new Put(Bytes.toBytes(blockId + "_" + t.id));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("id"), Bytes.toBytes(t.id));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("asset"), Bytes.toBytes(g.toJson(t.getAsset())));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("operation"), Bytes.toBytes(g.toJson(t.getOperation())));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("metadata"), Bytes.toBytes(g.toJson(t.getMetadata())));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("inputs"), Bytes.toBytes(g.toJson(t.getInputs())));
			p.add(Bytes.toBytes("transaction"), Bytes.toBytes("outputs"), Bytes.toBytes(g.toJson(t.getOutputs())));
			puts.add(p);
		}

		table.put(puts);
	}

	/*public List<Transaction> getTransactions(List<String> transId) throws IOException {
		Table table= connection.getTable(TableName.valueOf("hbaseChainDB"));
		List<Get> get= new ArrayList<Get>();
		for(String id : transId)
			get.add(new Get(Bytes.toBytes(id)));
		Result res[]= table.get(get);

		List<Transaction> ans= new ArrayList<Transaction>();
		byte b[];
		Gson g= HbaseUtil.getTransactionGson();

		for(Result r : res) 
			ans.add(extractTransactionFromResult(r));
		
		return ans;
	}*/

	public void insertBlock(Block bl) throws IOException {
		Table t= connection.getTable(TableName.valueOf("block"));

		Put p= new Put(Bytes.toBytes(bl.getId()));
		p.add(Bytes.toBytes("listOfTrans"), Bytes.toBytes("block"), Bytes.toBytes(bl.toString()));

		t.put(p);
		t.close();
	}

	public void insertToToVote(List<String> fedNodes, Block bl) throws IOException {
		Table table= connection.getTable(TableName.valueOf("toVote"));
		ArrayList<Put> puts= new ArrayList<>();

		for(String node : fedNodes) {
			Put p= new Put(Bytes.toBytes(node + "_" + bl.getId()));
			p.add(Bytes.toBytes("votes"), Bytes.toBytes("block"), Bytes.toBytes(HbaseUtil.getTransactionGson().toJson(bl.getTransactionIds())));
			puts.add(p);
		}
		table.put(puts);
	}

	public List<String> getFromToVote(String fedNode) throws IOException {
		ArrayList<String> ans= new ArrayList<>();
		ResultScanner rs= getRowsWithPrefix(fedNode, "toVote");
		for(Result r : rs)
			ans.add(Bytes.toString(r.getRow()));
		return ans;

	}

	public void deleteFromToVote(String rowId) throws IOException {
		Table t= connection.getTable(TableName.valueOf("toVote"));
		t.delete(new Delete(Bytes.toBytes(rowId)));
		t.close();
	}
	
	public void insertToVote(Vote vote) throws IOException {
		Table table= connection.getTable(TableName.valueOf("vote"));
		String id= vote.getBlockId() + "_" + vote.getValidity();
		Put p= new Put(Bytes.toBytes(id));
		p.add(Bytes.toBytes("votes"), Bytes.toBytes("details"), Bytes.toBytes(HbaseUtil.getTransactionGson().toJson(vote)));
		table.put(p);
	}

	//given a transaction's reference id, searches for its block
	public Transaction getTransaction(String transId) throws IOException {
		SingleColumnValueFilter filter= new SingleColumnValueFilter(Bytes.toBytes("transaction"), 
		Bytes.toBytes("id"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(transId));

		Scan sc= new Scan();
		sc.setFilter(filter);
		//sc.addColumn(Bytes.toBytes("transaction"), Bytes.toBytes("id"));
		
		Table t= connection.getTable(TableName.valueOf("hbaseChainDB"));
		ResultScanner rs= t.getScanner(sc);
		ArrayList<Transaction> ans= new ArrayList<>();
		for(Result r : rs) {
			ans.add(extractTransactionFromResult(r));
		}

		if(ans.size() == 0)
			throw new RuntimeException("No such trasaction exists");

		return ans.get(ans.size()-1);
	}
	
	//helper functions
	public List<Transaction> getTransactionsWithPrefix(String prefix) throws IOException {
		ArrayList<Transaction> ans= new ArrayList<>();
		ResultScanner rs= getRowsWithPrefix(prefix, "hbaseChainDB");
		for(Result r : rs) {
			ans.add(extractTransactionFromResult(r));
		}
		
		return ans;
	}
	
	
	//helper function
	public Transaction extractTransactionFromResult(Result r) {
		byte b[];
		Gson g= HbaseUtil.getTransactionGson();
		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("id"));
		String id= Bytes.toString(b); 

		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("asset"));
		Map asset= g.fromJson(Bytes.toString(b), Map.class);

		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("operation"));
		Operation op= g.fromJson(Bytes.toString(b), Operation.class);

		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("metadata"));
		Map meta= g.fromJson(Bytes.toString(b), Map.class);
	
		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("inputs"));
		Type listType= new TypeToken<ArrayList<Input>>() {}.getType();
		List<Input> ins= g.fromJson(Bytes.toString(b), listType);

		b= r.getValue(Bytes.toBytes("transaction"), Bytes.toBytes("outputs"));
		Type listType1= new TypeToken<ArrayList<Output>>() {}.getType();
		List<Output> outs= g.fromJson(Bytes.toString(b), listType1);
		
		return new Transaction(id, op, asset, ins, outs, meta);
	}

	public String getGenesis() throws IOException {
		String ans= null;
		ResultScanner rs= getRowsWithPrefix("GENESIS", "block");
		for(Result r : rs)
			ans= Bytes.toString(r.getRow());
		
		return ans;
	}
	/*public Transaction getTransaction(String id) throws IOException {
		Configuration conf= HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(conf);
		Table t= connection.getTable(TableName.valueOf("backLog"));

		Get g= new Get(Bytes.toBytes(id));
		Result r= t.get(g);
		byte[] b= r.getValue(Bytes.toBytes("transactions"), Bytes.toBytes("transaction"));

		return HbaseUtil.getTransactionGson().fromJson(Bytes.toString(b), Transaction.class);
	}*/

	//helper function
	public ResultScanner getRowsWithPrefix(String prefix, String table) throws IOException {
		byte[] pre= prefix.getBytes();

	
		Table t= connection.getTable(TableName.valueOf(table));

		Scan sc= new Scan(pre);
		PrefixFilter preFilter= new PrefixFilter(pre);
		sc.setFilter(preFilter);
		ResultScanner rs= t.getScanner(sc);

		return rs;
	}


}
