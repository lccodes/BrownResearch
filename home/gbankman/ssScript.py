import os
import fnmatch

def parseFile(hString, suspense, surprise, userNums, fileName):
	if(len(hString) > 0):
		surveys = hString[0].split("|||")
		lenSur = len(surveys[0].split("|")) - 1
		#this is the previous survey (for surprise, not suspense)
		lastSurvey = []
		for i in range(lenSur):
			lastSurvey.append(0.0)
		tempSuspense = []
		tempSurprise = []
		sOne = 0
		sTwo = 0
		for survey in surveys:
			data = survey.split("|")
			while("" in data):
				data.remove("")
			curLen = len(data)
			if curLen == lenSur:
				sOne += 1
				data[lenSur - 1] = data[lenSur - 1].split(",")[0]
				curSur = 0
				for elNum in range(curLen):
					try:
						cur = float(data[elNum].split(":")[1])
						if cur > 1:
							cur = cur/100
						#here we increment the surprise
						curSur += (cur - lastSurvey[elNum])**2
						data[elNum] = cur
					except IndexError:
						print	29
					except:
						print 31
				tempSurprise.append(round(curSur, 8))
				lastSurvey = data
			elif curLen == 2*lenSur:
				sTwo += 1
				data[curLen - 1] = data[curLen - 1].split(",")[0]
				curSusp = 0.0
				first = True
				for test in lastSurvey:
					if(test != 0):
						first = False
				if(not first):
					for elNum in range(curLen):
						try:
							cur = float(data[elNum].split(":")[1])
							if cur > 1:
								cur = cur/100
							curSusp += (cur - lastSurvey[elNum%lenSur])**2
						except:
							print 52
				tempSuspense.append(round(curSusp/2, 8))
		if(sOne == sTwo):
			if(userNums):
				curNum = userNums[len(userNums) - 1] + 1
			else:
				curNum = 1
			for j in range(sOne):
				userNums.append(curNum)
			suspense += tempSuspense
			if(len(tempSurprise) > 0):
				tempSurprise[0] = 0.0
			surprise += tempSurprise
#suspense - the x axis
suspense = []
#surprise - the y axis
surprise = []
#user numbers will be the z axis
userNums = []

allSurveyResults = {}
os.chdir("../../web/respice")
for file in os.listdir(os.getcwd()):
	if(os.path.isdir(file) and file != "admin6789"):
		for innerFile in os.listdir(file):
			if fnmatch.fnmatch(innerFile, file + '-survey.txt'):
				os.chdir(file)
				myFile = open(innerFile, "r")
				hString = myFile.readlines()
				parseFile(hString, suspense, surprise, userNums, file + '-survey.txt')
				os.chdir("..")

toWrite = open("ssdata.csv", "w")
for susp in suspense:
	toWrite.write(str(susp) + ",")
toWrite.write("\n")
for surp in surprise:
	toWrite.write(str(surp) + ",")
toWrite.write("\n")
for un in userNums:
	toWrite.write(str(un) + ",")
toWrite.write("\n")
print len(suspense)
print len(surprise)
print len(userNums)
