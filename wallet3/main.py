from user import User
from wallet import Wallet
import crypto_utils
import bigchaindb_driver
import json 
import getpass
import random
from exceptions import IncorrectPasswordError, UnregisteredUserError, UserAlreadyExistsError
from bigchaindb.common.transaction import TransactionLink
from crypto_utils import decrypt, generate_key_from_password

DATAFILE = '/home/sailakshman/Desktop/MedRec/wallet/patients_data.dat'

if __name__ == '__main__':
   u= User()
   w= Wallet()
   var = 1
   f = open(DATAFILE, 'r')
   patient_data = json.load(f)
   print("Welcome to PrivAd\n")
   print("----------------------------------------------------\n")
   signIn= input("LOGIN or REGISTER: ")
   if signIn.lower() == "login":
      username= input("Username: ")
      password= getpass.getpass("Password: ")
      u=w.fetch_user(username,password)
   elif signIn.lower() == "register":
      u.get_input()
      w.add_user(u)
   print("----------------------------------------------------\n")
   #print("Original: ",patient_data[1])
   while var == 1:
      print("####################################################\n")
      typeofOp= input("Enter the type of Operation: \n1.Create\n2.Retrieve\n3.Share\n4.Modify \n5.Display Assets\n6.Userdata\n7.Asset Details\n\n")   
      if typeofOp.lower() == "create":
         asset_id= u.create_asset(patient_data[1],use_encryption= True)
         print("Creation Successful: ",asset_id)
 
      if typeofOp.lower() == "retrieve":
         asset_id= input("Enter ID of the asset to be Retreived: \n")
         hello= u.retrieve_txn(asset_id) 
         allowed_keys= hello['metadata']['allowed_keys']
         user2pckey= w.get_ec_public_key(allowed_keys[0])
         if not u.public_key in allowed_keys:
            print("Permission Denied.")
         else:
            key= u.retrieve_shared_secret(asset_id,user2pckey)
            print("Normal: ",key)
            print(u.retrieve_asset(asset_id,key))
    
      if typeofOp.lower() == "share":
         asset_id= input("Enter ID of the asset to be Shared: \n")
         hello= u.retrieve_txn(asset_id) 
         allowed_keys= hello['metadata']['allowed_keys']
         if not u.public_key in allowed_keys:
            print("Update Failed. Permission Denied.")
         else:
            updated_user= input("Share with : ")
            if not updated_user in Wallet._users:
               raise UnregisteredUserError(updated_user + ' is not a registered user')
               sys.exit()
            pk_updated_user= w.get_public_key(updated_user)
            print(type(pk_updated_user))
            u_asset_id= u.update_asset(pk_updated_user,asset_id,allowed_keys)
            print("Updation Successful ",u_asset_id)
      
      if typeofOp.lower() == "modify":
         asset_id= input("Enter ID of the asset to be modified: \n")
         hello= u.retrieve_txn(asset_id) 
         allowed_keys= hello['metadata']['allowed_keys']
         if not u.public_key in allowed_keys:
            print("Permission Denied.Can't Modify the data.")
         else:
            modified_asset_id= u.modify_asset(asset_id,u.public_key)
            print("Modification Successful.", modified_asset_id)
            
      if typeofOp.lower() == "transfer":
         asset_id= input("Enter ID of the asset to be Transferred: \n")
         hello= u.retrieve_txn(asset_id)
         allowed_keys= hello['metadata']['allowed_keys']
         if not u.public_key in allowed_keys:
            print("Transfer Failed. Permission Denied.")
         else:
            new_owner= input("Transfer to: ")
            if not new_owner in Wallet._users:
               raise UnregisteredUserError(new_owner + 'is not a registered user')
               sys.exit()
            pk_new_owner= w.get_public_key(new_owner)
            trans_asset_id= u.transfer_asset(asset_id,pk_new_owner)
            print(trans_asset_id)

      if typeofOp.lower() == "display assets":
         print(u.display_all_assets())
#         for i in u.get_owned_ids():
#            print(i.txid)
      
      if typeofOp.lower() == "userdata":
         print('Username: ', u.username)
         print('Public Key: ', u.public_key)
         print('P Key: ', u.private_key)

      if typeofOp.lower() == "asset details":
         asset_id= input("Enter ID of the asset: \n")
         w.get_asset_details(asset_id)
         w.get_username_given_public_key(u.public_key)
      if typeofOp.lower()=="try":
         generate_shared_secret("alice","bob")
         
      print("####################################################\n")
