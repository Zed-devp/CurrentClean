import os 
def clean_outlier_timestamp_data(OUTPUT_FILE = "../data/all.txt", USING_GENERATED_DATA = False):

	valid_id = {'A434F11F1B05', 'A434F11EEE06', 'A434F11F1684', 'A434F11F1E86', 'A434F11EF48B', 'A434F11F2003', 
				'A434F11EEF0E', 'A434F11EA281', 'A434F11F1D06', 'A434F11F1000', 'A434F11F1606', 'A434F11FF78E', 
				'A434F11F3681', 'A434F11F0C80', 'A434F11F1B88', 'A434F11EF609', 'A434F11FFE0D', 'A434F11F1B8A', 
				'A434F1201380', 'A434F11F1B07', 'A434F11F0E06', 'A434F11F2F84', 'A434F11F1001', 'A434F11A3408', 
				'A434F1204007', 'A434F11EA080', 'A434F1201282', 'A434F11EF80D', 'A434F11F1404', 'A434F11F1486', 
				'A434F11F1683', 'A434F11F1A0A', 'A434F11F1783', 'A434F11F118D', 'A434F11EEB80', 'A434F11F0E83', 
				'A434F11F1083', 'A434F11F1B84', 'A434F11F1D04', 'A434F11F1482', 'A434F11F1187', 'A434F11F1C85', 
				'A434F1204005', 'A434F11F1F03', 'A434F11F3902', 'A434F11EF68F', 'A434F11F1106', 'A434F11F1782', 
				'A434F11F1607', 'A434F11F4287', 'A434F11F1F02', 'A434F11F1406', 'A434F11F0E85', 'A434F11EEF8C', 
				'A434F11F1E09', 'A434F11F0E03', 'A434F11F1483', 'A434F11F1F85'}
				
	print("generating all.txt ...")
	output = open(OUTPUT_FILE, "w")
	for root, dirs, files in os.walk("../Sensor/"):
		for filename in files:
			if filename[-4:] != '.txt': continue

			f = open("../Sensor/"+filename,'r')
			for line in f:
				row = line.strip().split(",")

				# eliminate outliers
				if len(row) < 7 or len(row[0]) < 10 or len(row[6].split(":")) != 2:
					print("Eliminate data outlier:" + line.strip())
					continue
				ID = "".join(row[1].split(":"))

				if ID in valid_id or USING_GENERATED_DATA:
					try:
						## cannot call the built-in round() function in python 3.X, because
						## it will not round 0.5 up to 1.
						timestamp = int(int(float(row[0])/10+0.5)*10)
						temp = int(float(row[3].split(":")[1])*10+0.5)/10
						humidity = int(float(row[4].split(":")[1])*10+0.5)/10
						pressure = int(float(row[5].split(":")[1])/10+0.5)*10
					except ValueError:
						print("Eliminate ValueError outlier:" + line.strip())
						continue
					except IndexError:
						print("Eliminate IndexError outlier:" + line.strip())
						continue


					output.write("%s,%s,T:%s,H:%s,P:%s,%s\n" % (str(timestamp),ID,str(temp),str(humidity),str(pressure),row[6]))

			f.close()
			#print(filename)
	output.close()

	print("sorting the file by timestamps ...")
	os.system("sort %s -o %s" % (OUTPUT_FILE,OUTPUT_FILE))
	print("Done.")

def strip_data(PATH_TO_ALL_FILE = "../data/all.txt", OUTPUT_PATH = "../data/", num = 2000):
	os.system("tail -%d  %s > %s" % (num, PATH_TO_ALL_FILE, OUTPUT_PATH))

def generate_senor_index_mapping(INPUT_FILE_PATH = "../data/all.txt", OUTPUT_FILE_PATH = "../data/sensor_index.txt", OUTPUT_FILE_PATH_PROB = "../data/probabilistic.tsv"):
	print("generating senor_index mapping ...")
	sensor_set = set()

	INPUT_FILE = open(INPUT_FILE_PATH,"r")
	for line in INPUT_FILE: 
		ID = line.strip().split(",")[1]
		sensor_set.add(ID)
	INPUT_FILE.close()

	OUTPUT_FILE = open(OUTPUT_FILE_PATH, "w")
	index = 1
	for ID in sensor_set:
		OUTPUT_FILE.write("%d,%s,Temperature;Humidity;AirPressure;Voltage\n" % (index, ID))
		index+=1
	OUTPUT_FILE.close()

def generate_prob(OUTPUT_FILE = "../data/probobilities.tsv"):
	print("generating probobilities.tsv")
	f = open(OUTPUT_FILE, "w")
	for a in "Temperature;Humidity;AirPressure;Voltage".split(";"):
		f.write("Sensor_%s\n" % (a))
	f.close()
	print("Done.")

def generate_cell_file(INPUT_FILE_PATH = "../data/sensor_index.txt", OUTPUT_FILE_PATH = "../data/UModel/cell.tsv"):
	print("generating cell.tsv ...")

	INPUT_FILE = open(INPUT_FILE_PATH,"r")
	OUTPUT_FILE = open(OUTPUT_FILE_PATH, "w")

	for line in INPUT_FILE: 
		index, _, attributes = line.strip().split(",")

		for attr in attributes.split(";"):
			OUTPUT_FILE.write("Sensor_%s_%s\tSensor_%s\tSensor_%s\n" % (index,attr,index,attr))
	INPUT_FILE.close()
	OUTPUT_FILE.close()
	print("Done.")

def generate_lastupdate_update_time_file(
	RMODEL_LASTUPDATE_PATH = "../data/RModel/lastupd.tsv",
	RMODEL_UPDATED_PATH = "../data/RModel/updated.tsv",
	UMODEL_LASTUPDATE_PATH = "../data/UModel/lastupd.tsv",
	UMODEL_UPDATED_PATH = "../data/UModel/updated.tsv",
	UMODEL_TIME_PATH = "../data/UModel/time.tsv",
	INPUT_FILE_PATH = "../data/all.txt",
	INDEX_ID_PATH = "../data/sensor_index.txt"):

	time = set()
	ID_to_index = dict()
	abbrev_to_name = {"T":"Temperature", "H":"Humidity", "P":"AirPressure", "B":"Voltage"}

	last_update = dict()
	last_update_time = dict()  # Map of map, storing the last update timestamp of an attribute from sensor(ID), e.g. {ID, {abbrev, timestamp}}
	secondlast_update = dict()

    # read ID to index(tuple) mapping
	INDEX_ID_FILE = open(INDEX_ID_PATH,"r")
	for line in INDEX_ID_FILE:
		index, ID, _ = line.strip().split(",")
		ID_to_index[ID] = index
	INDEX_ID_FILE.close()

    
    # read and proccess all.txt file
	INPUT_FILE = open(INPUT_FILE_PATH,"r")
	umodel_updated_file = open(UMODEL_UPDATED_PATH,"w")
	rmodel_updated_file = open(RMODEL_UPDATED_PATH,"w")

	print("generating updated.tsv ...")
	for line in INPUT_FILE: 
		# example: 1522382400,A4:34:F1:1A:34:08,T:31.3,H:16.4,P:100510,B:2.96
		timestamp, ID, temp, humidity, pressure, voltage = line.strip().split(",")

		# add to time set
		time.add(timestamp)

		if ID in last_update:
			# update the second laste updated dict
			# secondlast_update[ID] = last_update[ID]

			t = zip([temp, humidity, pressure, voltage], last_update[ID].split(","))

			for new_attr, old_attr in t:
				abbrev1,value_new = new_attr.split(":")
				abbrev2,value_old = old_attr.split(":")

				if value_old != value_new:
					umodel_updated_file.write("Sensor_%s_%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev1],timestamp))
					rmodel_updated_file.write("Sensor_%s_%s\t%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev1],timestamp,value_new))
					last_update_time[ID][abbrev1] = timestamp
			last_update[ID] = "%s,%s,%s,%s" % (temp, humidity, pressure, voltage)
		else:
	        # update the last updated dict
			last_update[ID] = "%s,%s,%s,%s" % (temp, humidity, pressure, voltage)
			m = dict()
			for a in [temp, humidity, pressure, voltage]:
				m[a.split(":")[0]] = timestamp
			last_update_time[ID] = m


	INPUT_FILE.close()
	umodel_updated_file.close()
	rmodel_updated_file.close()
	print("Done.")

	print("generating lastupd.tsv ...")
	umodel_lastupdate_file = open(UMODEL_LASTUPDATE_PATH,"w")
	rmodel_lastupdate_file = open(RMODEL_LASTUPDATE_PATH,"w")
	for ID in last_update.keys():
		attrs = last_update[ID]

		t = attrs.split(",")
		for a1 in t:
			abbrev,value = a1.split(":")

			umodel_lastupdate_file.write("Sensor_%s_%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev],last_update_time[ID][abbrev]))
			rmodel_lastupdate_file.write("Sensor_%s_%s\t%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev],last_update_time[ID][abbrev],value))
		# if ID not in secondlast_update:
		# 	print("Tuple %s, ID %s only appears once." % (ID_to_index.get(ID),ID))
		# 	continue

		# attrs1 = last_update[ID]
		# attrs2 = secondlast_update[ID]

		# if attrs1 != attrs2:
		# 	t = zip(attrs1.split(","), attrs2.split(","))
		# 	for a1,a2 in t:
		# 		abbrev1,value1 = a1.split(":")
		# 		abbrev2,value2 = a2.split(":")

		# 		if value1 != value2:
		# 			umodel_lastupdate_file.write("Sensor_%s_%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev1],last_update_time[ID]))
		# 			rmodel_lastupdate_file.write("Sensor_%s_%s\t%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev1],last_update_time[ID],value1))
	umodel_lastupdate_file.close()
	rmodel_lastupdate_file.close()
	os.system("sort -k 2 %s -o %s" % (UMODEL_LASTUPDATE_PATH,UMODEL_LASTUPDATE_PATH))
	os.system("sort -k 2 %s -o %s" % (RMODEL_LASTUPDATE_PATH,RMODEL_LASTUPDATE_PATH))
	print("Done.")


	print("generating time.tsv ...")
	time_file = open(UMODEL_TIME_PATH,"w")
	for val in time:
		time_file.write("%s\n" % (val))
	time_file.close()
	print("sorting the file by timestamps ...")
	os.system("sort %s -o %s" % (UMODEL_TIME_PATH,UMODEL_TIME_PATH))
	print("Done.")

def select_time_interval(
	start = 0, 
	end = float("inf"), 
	PATH_TO_ALL = "../data/all.txt",
	PATH_TO_OUTPUT = "../data/ranged_all.txt"):
	
	all_file = open(PATH_TO_ALL, "r")
	out_file = open(PATH_TO_OUTPUT, "w")

	# print(start)
	# print(end)

	for row in all_file:
		time = row.strip().split(",")[0]
		if len(time) > 10:
			continue

		time = int(time)

		if time >= start and time <= end:
			out_file.write("%s\n" % row.strip())
		else:
			if time > end:
				break
	all_file.close()
	out_file.close()


def generate_lastupdate_update_time_file_umodel_only(
	UMODEL_LASTUPDATE_PATH = "../data/UModel/lastupd.tsv",
	UMODEL_UPDATED_PATH = "../data/UModel/updated.tsv",
	UMODEL_TIME_PATH = "../data/UModel/time.tsv",
	INPUT_FILE_PATH = "../data/all_umodel.txt",
	INDEX_ID_PATH = "../data/sensor_index.txt"):

	time = set()
	ID_to_index = dict()
	abbrev_to_name = {"T":"Temperature", "H":"Humidity", "P":"AirPressure", "B":"Voltage"}

	last_update = dict()
	last_update_time = dict()  # Map of map, storing the last update timestamp of an attribute from sensor(ID), e.g. {ID, {abbrev, timestamp}}
	secondlast_update = dict()

    # read ID to index(tuple) mapping
	INDEX_ID_FILE = open(INDEX_ID_PATH,"r")
	for line in INDEX_ID_FILE:
		index, ID, _ = line.strip().split(",")
		ID_to_index[ID] = index
	INDEX_ID_FILE.close()

    
    # read and proccess all.txt file
	INPUT_FILE = open(INPUT_FILE_PATH,"r")
	umodel_updated_file = open(UMODEL_UPDATED_PATH,"w")

	print("generating updated.tsv ...")
	for line in INPUT_FILE: 
		# example: 1522382400,A4:34:F1:1A:34:08,T:31.3,H:16.4,P:100510,B:2.96
		timestamp, ID, temp, humidity, pressure, voltage = line.strip().split(",")

		# add to time set
		time.add(timestamp)

		if ID in last_update:
			# update the second laste updated dict
			# secondlast_update[ID] = last_update[ID]

			t = zip([temp, humidity, pressure, voltage], last_update[ID].split(","))

			for new_attr, old_attr in t:
				abbrev1,value_new = new_attr.split(":")
				abbrev2,value_old = old_attr.split(":")

				if value_old != value_new:
					umodel_updated_file.write("Sensor_%s_%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev1],timestamp))
					last_update_time[ID][abbrev1] = timestamp
			last_update[ID] = "%s,%s,%s,%s" % (temp, humidity, pressure, voltage)
		else:
	        # update the last updated dict
			last_update[ID] = "%s,%s,%s,%s" % (temp, humidity, pressure, voltage)
			m = dict()
			for a in [temp, humidity, pressure, voltage]:
				m[a.split(":")[0]] = timestamp
			last_update_time[ID] = m


	INPUT_FILE.close()
	umodel_updated_file.close()
	print("Done.")

	print("generating lastupd.tsv ...")
	umodel_lastupdate_file = open(UMODEL_LASTUPDATE_PATH,"w")
	for ID in last_update.keys():
		attrs = last_update[ID]

		t = attrs.split(",")
		for a1 in t:
			abbrev,value = a1.split(":")

			umodel_lastupdate_file.write("Sensor_%s_%s\t%s\n" % (ID_to_index[ID],abbrev_to_name[abbrev],last_update_time[ID][abbrev]))

	umodel_lastupdate_file.close()
	os.system("sort -k 2 %s -o %s" % (UMODEL_LASTUPDATE_PATH,UMODEL_LASTUPDATE_PATH))
	print("Done.")


	print("generating time.tsv ...")
	time_file = open(UMODEL_TIME_PATH,"w")
	for val in time:
		time_file.write("%s\n" % (val))
	time_file.close()
	print("sorting the file by timestamps ...")
	os.system("sort %s -o %s" % (UMODEL_TIME_PATH,UMODEL_TIME_PATH))
	print("Done.")

if __name__ == "__main__":

	# os.system("rm ../data/*.txt ../data/UModel/* ../data/RModel/*")

	# clean_outlier_timestamp_data()
	# generate_senor_index_mapping()
	# select_time_interval(start = 0, end = float("inf"), PATH_TO_ALL = "./static/all.txt", PATH_TO_OUTPUT = "./tmp/ranged_all.txt")
	# create_data_umodel_only(PATH_TO_ALL_FILE = "./tmp/ranged_all.txt", OUTPUT_PATH = "./tmp/ranged_all_umodel.txt")
	# generate_cell_file(INPUT_FILE_PATH = "./static/sensor_index.txt", OUTPUT_FILE_PATH = "./test_sensor/input/cell.tsv")
	# generate_prob(OUTPUT_FILE = "./repair/Input/probabilistic.tsv")
	# generate_prob(OUTPUT_FILE = "./test_sensor/input/probabilistic.tsv")
	# generate_lastupdate_update_time_file(
	# 	RMODEL_LASTUPDATE_PATH = "./repair/Input/lastupd.tsv",
	# 	RMODEL_UPDATED_PATH = "./repair/Input/updated.tsv",
	# 	UMODEL_LASTUPDATE_PATH = "./test_sensor/input/lastupd.tsv",
	# 	UMODEL_UPDATED_PATH = "./test_sensor/input/updated.tsv",
	# 	UMODEL_TIME_PATH = "./test_sensor/input/time.tsv",
	# 	INPUT_FILE_PATH = "./tmp/ranged_all.txt",
	# 	INDEX_ID_PATH = "./static/sensor_index.txt")
	# generate_lastupdate_update_time_file_umodel_only(
	# 	UMODEL_LASTUPDATE_PATH = "./test_sensor/input/lastupd.tsv",
	# 	UMODEL_UPDATED_PATH = "./test_sensor/input/updated.tsv",
	# 	UMODEL_TIME_PATH = "./test_sensor/input/time.tsv",
	# 	INPUT_FILE_PATH = "./tmp/ranged_all_umodel.txt",
	# 	INDEX_ID_PATH = "./static/sensor_index.txt")

	cwd = os.getcwd()
	select_time_interval(start = 1522932390, end = 1522987200, PATH_TO_ALL = os.path.join(cwd,"static","all.txt"), PATH_TO_OUTPUT = os.path.join(cwd,"tmp","ranged_all.txt"))
	strip_data(PATH_TO_ALL_FILE = os.path.join(cwd,"tmp", "ranged_all.txt"), OUTPUT_PATH = os.path.join(cwd,"tmp", "ranged_all_umodel.txt"))
	strip_data(PATH_TO_ALL_FILE = os.path.join(cwd,"tmp","ranged_all.txt"), OUTPUT_PATH = os.path.join(cwd,"tmp", "ranged_all_rmodel.txt"), num = 10000)
	generate_cell_file(INPUT_FILE_PATH = os.path.join(cwd,"static", "sensor_index.txt"), OUTPUT_FILE_PATH = os.path.join(cwd,"test_sensor","input","cell.tsv"))
	generate_prob(OUTPUT_FILE = os.path.join(cwd,"repair","Input","probabilistic.tsv"))
	generate_prob(OUTPUT_FILE = os.path.join(cwd,"test_sensor","input","probabilistic.tsv"))
	generate_lastupdate_update_time_file(
		RMODEL_LASTUPDATE_PATH = os.path.join(cwd,"repair","Input","lastupd.tsv"),
		RMODEL_UPDATED_PATH = os.path.join(cwd,"repair","Input","updated.tsv"),
		UMODEL_LASTUPDATE_PATH = os.path.join(cwd,"test_sensor","input","lastupd.tsv"),
		UMODEL_UPDATED_PATH = os.path.join(cwd,"test_sensor","input","updated.tsv"),
		UMODEL_TIME_PATH = os.path.join(cwd,"test_sensor","input","time.tsv"),
		INPUT_FILE_PATH = os.path.join(cwd,"tmp","ranged_all_rmodel.txt"),
		INDEX_ID_PATH = os.path.join(cwd,"static","sensor_index.txt"))
	generate_lastupdate_update_time_file_umodel_only(
		UMODEL_LASTUPDATE_PATH = os.path.join(cwd,"test_sensor","input","lastupd.tsv"),
		UMODEL_UPDATED_PATH = os.path.join(cwd,"test_sensor","input","updated.tsv"),
		UMODEL_TIME_PATH = os.path.join(cwd,"test_sensor","input","time.tsv"),
		INPUT_FILE_PATH = os.path.join(cwd,"tmp","ranged_all_umodel.txt"),
		INDEX_ID_PATH = os.path.join(cwd,"static","sensor_index.txt"))
	
	# f = open("../Sensor/2018_4_5_23.txt",'r')
	# d = set()
	# for line in f:
	# 	row = line.strip().split(",")
	# 	d.add(row[1])
	# f.close()
	# print(d)





	