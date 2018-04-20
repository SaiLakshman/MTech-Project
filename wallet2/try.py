from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import dh

parameters = dh.generate_parameters(generator=2, key_size=512, backend=default_backend())

a_private_key = parameters.generate_private_key()
a_peer_public_key = a_private_key.public_key()
print("pub _key: ",a_peer_public_key, "  priv_key: ", a_private_key)
b_private_key = parameters.generate_private_key()
b_peer_public_key = b_private_key.public_key()

a_shared_key = a_private_key.exchange(b_peer_public_key)
b_shared_key = b_private_key.exchange(a_peer_public_key)

print ('a_secret: ',a_shared_key)
print ('b_secret: ',b_shared_key)
