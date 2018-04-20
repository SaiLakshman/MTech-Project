import json
import csv
import datetime

if __name__ == '__main__':
   print("Enter the details of the patient: ")
   nameofpatient= input("Patients Name: ")
   age= input("Age: ")
   gender= input("Gender: ")
   address= input("Address: ")
   occupation= input("Occupation: ")
   do1v= input("Date of 1st Visit: ")
   cnsummary= input("Clinical Note Summary: ")
   pdiagnosis= input("Provisional Diagnosis: ")
   investigations= input("Investigations: ")
   dai= input("Diagnosis After Investigations: ")
   advice= input("Advice: ")
   followup= input("Follow Up: ")
   observations= input("Observations: ")
   phone= input("Phone Number: ")
   district= input("District: ")
   state= input("State: ")
   date= datetime.datetime.now().strftime("%Y-%m-%d")
   result= dict()
   result= {"Name": nameofpatient, "Age": age, "Gender": gender, "Address": address, "Occupation": occupation, "Date of 1st Visit": do1v, "Date": date}
   
   
   #result.append(nameofpatient)
   #result.append(age)
   #result.append(gender)
   #result.append(address)
   #result.append(occupation)
   #result.append(do1v)
   print(result)
   #result1= json.dumps(result)
