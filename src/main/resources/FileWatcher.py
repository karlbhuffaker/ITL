# If schedule isn't installed then run this command:
#	pip install schedule
#
import os
import shutil
import time
import schedule
from datetime import datetime

def check_and_move_file():
	prod_source_folder="\\\\nas00583pn\\SCM\Builds\\ITL"
	otl_destination_folder="\\\\otl-svm0.otl.lab\\icp-wvc\DEVOPS\\Karl\\ITL_War_Build\\"
	test_destination_folder="\\\\icplabs-app-02\\d$\\app\\Tomcat9\\webapps\\"
	dev_destination_folder="\\\\icplabs-app-03\\D$\\app\\Tomcat9\\webapps\\"
	file_name = "ITL.war"
	log_file = os.path.join(otl_destination_folder, "ITL_War_Build.log")
    
	# Copy the ITL.war file to OTL save directory.
	prod_source_folder = os.path.join(prod_source_folder, file_name)
	print(f"In {prod_source_folder}")
	if os.path.exists(prod_source_folder):
		shutil.copy(prod_source_folder, test_destination_folder)
		shutil.copy(prod_source_folder, dev_destination_folder)	

		print(f"Checking for WAR file in {otl_destination_folder}")
		file_path = otl_destination_folder + file_name
		if os.path.exists(file_path): 			
			os.remove(file_path)
			shutil.move(prod_source_folder, otl_destination_folder)
		else:
			shutil.move(prod_source_folder, otl_destination_folder)
			
		print(f"File moved to prod {otl_destination_folder}")
		
		log_message = f"{datetime.now()}: {file_name} has been loaded onto the test and dev VM's and moved to  {otl_destination_folder}\n"
	else:
		print(f"{file_name} not found in {prod_source_folder}")
		log_message = f"{datetime.now()}: {file_name} not found in {prod_source_folder}\n"

	with open(log_file, "a") as log:
		log.write(log_message)
	print(log_message)
	
# Schedule the task to run every minutes
#schedule.every(60).minutes.do(check_and_move_file)
#schedule.every(15).seconds.do(check_and_move_file)
check_and_move_file()
quit()


# Keep the script running
#while True:
	#check_and_move_file()
	#time.sleep(15)