import sys


from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import serialization

def make_unicode(input):
    if type(input) != 'unicode':
        input =  input.decode('utf-8')
        return input
    else:
        return input
private_key = ec.generate_private_key(ec.SECP384R1(), default_backend())
mypubkey= private_key.public_key()
print("Original")
print(private_key)
print(mypubkey)
seriaP= private_key.private_bytes(encoding=serialization.Encoding.PEM,format=serialization.PrivateFormat.PKCS8,encryption_algorithm=serialization.NoEncryption())
serialized_public = mypubkey.public_bytes(encoding=serialization.Encoding.PEM,format=serialization.PublicFormat.SubjectPublicKeyInfo)
print("Serialized")
print(seriaP)
print(serialized_public)

seriaP= seriaP.decode()
print(seriaP)
seriaP= seriaP.encode()
print("encoded : ",seriaP)
loaded_public_key = serialization.load_pem_public_key(serialized_public,backend=default_backend())
loaded_private_key = serialization.load_pem_private_key(seriaP,password=None,backend=default_backend())
#print("Loaded")
#print("P Key: ",loaded_public_key)
print("Priv Key ",loaded_private_key)
shared= loaded_private_key.exchange(ec.ECDH(),loaded_public_key)
print(private_key.key_size())
#print(seriaP.splitlines(3))




#print(serialized_public.splitlines())






#priv2= ec.generate_private_key(ec.SECP384R1(), default_backend())
#peerpub= priv2.public_key()
#serialized_pub = peerpub.public_bytes(encoding=serialization.Encoding.PEM,format=serialization.PublicFormat.SubjectPublicKeyInfo)
#loaded_public= serialization.load_pem_public_key(serialized_pub,backend=default_backend())
#shared= loaded_private_key.exchange(ec.ECDH(),loaded_public)
#print("sjared: ",shared)
#shared_key = private_key.exchange(ec.ECDH(), peerpub)

#shared= priv2.exchange(ec.ECDH(),mypubkey)

#if shared_key == shared:
#   print("Success")

