package hbasechaindb;
import java.io.*;
import java.lang.reflect.Type;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.CryptoConditionReader;
import org.interledger.cryptoconditions.CryptoConditionWriter;
import org.interledger.cryptoconditions.Ed25519Sha256Condition;
import org.interledger.cryptoconditions.Ed25519Sha256Fulfillment;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.der.DerEncodingException;
import org.interledger.cryptoconditions.der.DerOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;

public class HbaseUtil {

	public static byte[] objectSerialize(Serializable obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public static Object objectDeserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	public static String byteArrayToHex(byte[] hash) {
		StringBuffer buff = new StringBuffer();

		for (byte b : hash) {
			buff.append(String.format("%02x", b & 0xFF));
		}

		return buff.toString();
	}

	public static byte[] hexToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	public static byte[] conditionToByteArray(Ed25519Sha256Condition cond) {

		return null;
	}

	public static byte[] fulfillToByteArray(Ed25519Sha256Fulfillment ffill) {
		byte[] pk= ffill.getPublicKey().getEncoded();

		return null;
	}


	public static TypeAdapter<EdDSAPublicKey> getPublicKeyTypeAdapter() {
		return new TypeAdapter<EdDSAPublicKey>() {

			@Override
			public EdDSAPublicKey read(JsonReader arg0) throws IOException {
				Base58 bf= new Base58();
				X509EncodedKeySpec specPub= null;
				String str= arg0.nextString();
				try {
					specPub = new X509EncodedKeySpec(bf.decode(str));
				} catch (JsonSyntaxException | AddressFormatException e1) {
					e1.printStackTrace();
				}
				EdDSAPublicKey eddsakey = null;
				try {
					eddsakey = new EdDSAPublicKey(specPub);
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				}
				return eddsakey;
			}

			@Override
			public void write(JsonWriter arg0, EdDSAPublicKey arg1) throws IOException {
				Base58 nf= new Base58();
				if(arg1 == null)
					arg0.value(nf.encode("null".getBytes()));
				else
					arg0.value(nf.encode(arg1.getEncoded()));
			}

		};
	}

	public static TypeAdapter<EdDSAPrivateKey> getPrivateKeyType() {
		return new TypeAdapter<EdDSAPrivateKey>() {
			Gson g= new Gson();
			Base58 nf= new Base58();
			PKCS8EncodedKeySpec specPri;
			@Override
			public EdDSAPrivateKey read(JsonReader arg0) throws IOException {
				try {
					specPri = new PKCS8EncodedKeySpec(nf.decode(arg0.nextString()));
				} catch (JsonSyntaxException | AddressFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				EdDSAPrivateKey eddsakey = null;
				try {
					eddsakey = new EdDSAPrivateKey(specPri);
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return eddsakey;
			}

			@Override
			public void write(JsonWriter arg0, EdDSAPrivateKey arg1) throws IOException {
				Gson g= new Gson();
				Base58 nf= new Base58();
				if(arg1 == null)
					arg0.jsonValue(g.toJson(null));
				else
					arg0.value(nf.encode(arg1.getEncoded()));
			}
		};
	}

	public static TypeAdapter<byte[]> getByteArrayAdaptor() {
		return new TypeAdapter<byte[]>() {

			@Override
			public byte[] read(JsonReader arg0) throws IOException {
				Gson g= new Gson();
				Base58 nf= new Base58();
				byte[] ans= null;
				try {
					ans= nf.decode(arg0.nextString());
				} catch (JsonSyntaxException | AddressFormatException e) {
					e.printStackTrace();
				}
				return ans;
			}

			@Override
			public void write(JsonWriter arg0, byte[] arg1) throws IOException {
				Gson g= new Gson();
				Base58 nf= new Base58();
				if(arg1 == null)
					arg0.jsonValue(nf.encode(g.toJson(null).getBytes()));
				else
					arg0.value(nf.encode(arg1));
			}
		};
	}

	public static TypeAdapter<Condition> getConditionAdapter() {
		return new TypeAdapter<Condition>() {

			@Override
			public Condition read(JsonReader arg0) throws IOException {
				Base58 bf= new Base58();
				Condition c= null;
				try {
					String str= arg0.nextString();
					if(str.equals("3pm5bR"))
						c= null;
					else
						c= CryptoConditionReader.readCondition(bf.decode(str));
				} catch (DerEncodingException | AddressFormatException e) {
					e.printStackTrace();
				}
				return c;
			}

			@Override
			public void write(JsonWriter arg0, Condition arg1) throws IOException {
				Base58 bf= new Base58();
				Gson g= new Gson();
				
				if(arg1 == null)
					arg0.value(bf.encode("null".getBytes()));
				else
					try {
						arg0.value(bf.encode(CryptoConditionWriter.writeCondition(arg1)));
					} catch (DerEncodingException e) {
						e.printStackTrace();
					}
				
			}
		};
	}

	public static TypeAdapter<Fulfillment> getFulfillmentAdapter() {
		return new TypeAdapter<Fulfillment>() {

			@Override
			public Fulfillment read(JsonReader arg0) throws IOException {
				Base58 bf= new Base58();
				Fulfillment f= null;
				String str= arg0.nextString();
				byte b[];
				try {
					if(str.equals("3pm5bR"))
						f= null;
					else
						f= CryptoConditionReader.readFulfillment(bf.decode(str));
				} catch (AddressFormatException | DerEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return f;
			}

			@Override
			public void write(JsonWriter arg0, Fulfillment arg1) throws IOException {
				Base58 bf= new Base58();
				Gson g= new Gson();
				if(arg1 == null)
					arg0.value(bf.encode(g.toJson(null).getBytes()));
				else
					try {
						arg0.value(bf.encode(CryptoConditionWriter.writeFulfillment(arg1)));
					} catch (DerEncodingException e) {
						e.printStackTrace();
					}
			}
		};
	}

	/*public static JsonSerializer<PublicKey> getPublicSerielizer() {
		return new JsonSerializer<PublicKey>() {

			@Override
			public JsonElement serialize(PublicKey arg0, Type arg1, JsonSerializationContext arg2) {
				// TODO Auto-generated method stub
				Base58 bf= new Base58();
				return new JsonPrimitive(bf.encode(arg0.getEncoded()));
			}
		};
	}

	public static JsonSerializer<PrivateKey> getPrivateSerielizer() {
		return new JsonSerializer<PrivateKey>() {

			@Override
			public JsonElement serialize(PrivateKey arg0, Type arg1, JsonSerializationContext arg2) {
				Base58 bf= new Base58();
				return new JsonPrimitive(bf.encode(arg0.getEncoded()));
			}
		};
	}

	public static JsonDeserializer<PublicKey> getPublicDeserializer() {
		return new JsonDeserializer<PublicKey>() {

			@Override
			public PublicKey deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
					throws JsonParseException {
				// TODO Auto-generated method stub
				Base58 bf= new Base58();
				X509EncodedKeySpec specPub= null;
				try {
					specPub= new X509EncodedKeySpec(bf.decode(arg0.getAsJsonPrimitive().getAsString()));
				} catch (AddressFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					return new EdDSAPublicKey(specPub);
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
	}

	public static JsonDeserializer<PrivateKey> getPrivateDeserializer() {
		return new JsonDeserializer<PrivateKey>() {

			@Override
			public PrivateKey deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
					throws JsonParseException {
				// TODO Auto-generated method stub
				Base58 nf= new Base58();
				PKCS8EncodedKeySpec specPri = null;
				try {
					specPri= new PKCS8EncodedKeySpec(nf.decode(arg0.getAsJsonPrimitive().getAsString()));
				} catch (AddressFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					return new EdDSAPrivateKey(specPri);
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
	}*/

	public static Gson getTransactionGson() {
		GsonBuilder b= new GsonBuilder();
		b.registerTypeAdapter(PublicKey.class, getPublicKeyTypeAdapter());
		b.registerTypeAdapter(PrivateKey.class, getPrivateKeyType());
		b.registerTypeAdapter(Condition.class, getConditionAdapter());
		b.registerTypeAdapter(Fulfillment.class, getFulfillmentAdapter());
		b.registerTypeAdapter(EdDSAPublicKey.class, getPublicKeyTypeAdapter());
		b.registerTypeAdapter(EdDSAPrivateKey.class, getPrivateKeyType());
		Gson g= b.create();
		return g;
	}

	public Serializer<Fulfillment> getFulfillmentSerializer() {
		return new Serializer<Fulfillment>() {
			@Override
			public Fulfillment read(Kryo arg0, Input arg1, Class<Fulfillment> arg2) {
				int len= arg1.read();
				byte b[]= new byte[len];
				arg1.read(b, 0, len);
				Fulfillment ans= null;
				try {
					ans= CryptoConditionReader.readFulfillment(b);
				} catch (DerEncodingException e) {
					e.printStackTrace();
				}
				arg1.close();
				return ans;
			}

			@Override
			public void write(Kryo arg0, Output arg1, Fulfillment arg2) {
				byte b[]= null;
				try {
					b = CryptoConditionWriter.writeFulfillment(arg2);
				} catch (DerEncodingException e) {
					e.printStackTrace();
				}
				arg1.write(b.length);
				arg1.write(b);
				arg0.writeClass(arg1, Fulfillment.class);
				arg1.flush();
				arg1.close();
			}
		};
	}

	public static Serializer<PublicKey> getPublicKeySerializer() {
		return new Serializer<PublicKey>() {

			@Override
			public PublicKey read(Kryo arg0, Input arg1, Class<PublicKey> arg2) {
				int len= arg1.read();
				byte b[]= new byte[len];
				arg1.read(b, 0, len);
				X509EncodedKeySpec specPub= null;
				try {
					specPub = new X509EncodedKeySpec(b);
				} catch (JsonSyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				EdDSAPublicKey eddsakey = null;
				try {
					eddsakey = new EdDSAPublicKey(specPub);
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return eddsakey;
			}

			@Override
			public void write(Kryo arg0, Output arg1, PublicKey arg2) {
				byte b[]= arg2.getEncoded();
				arg1.write(b.length);
				arg1.write(b);
				arg0.writeClass(arg1, PublicKey.class);
				arg1.flush();
				arg1.close();
			}
		};
	}

	public static Serializer<PrivateKey> getPrivateKeySerializer() {
		return new Serializer<PrivateKey>() {

			@Override
			public PrivateKey read(Kryo arg0, Input arg1, Class<PrivateKey> arg2) {
				int len= arg1.read();
				byte b[]= new byte[len];
				arg1.read(b, 0, len);
				PKCS8EncodedKeySpec specPri= null;
				try {
					specPri = new PKCS8EncodedKeySpec(b);
				} catch (JsonSyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				EdDSAPrivateKey eddsakey = null;
				try {
					eddsakey = new EdDSAPrivateKey(specPri);
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return eddsakey;
			}

			@Override
			public void write(Kryo arg0, Output arg1, PrivateKey arg2) {
				byte b[]= arg2.getEncoded();
				arg1.write(b.length);
				arg1.write(b);
				arg0.writeClass(arg1, PublicKey.class);
				arg1.flush();
				arg1.close();
			}
		};
	}

	public static byte[] serialize(Object obj) {
		Kryo k= new Kryo();

		return null;
	}

	public static Object deserialize(byte b[]) {
		return null;

	}

}
