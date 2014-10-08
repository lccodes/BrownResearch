import os
import fnmatch


def parseFile(hString):
    newDict = {}
    for x in hString:
    	kvList = x.split("||")
    	for i in range(len(kvList)):
       	     kvList[i] = kvList[i].split(",")
       	     if len(kvList[i]) > 1:
	     	newDict[kvList[i][1]] = kvList[i][0]
    return newDict

dictList = []
os.chdir("../../web/respice")
for file in os.listdir(os.getcwd()):
    if(os.path.isdir(file)):
        for innerFile in os.listdir(file):
            if fnmatch.fnmatch(innerFile, file + '.txt'):
                os.chdir(file)
		myFile = open(innerFile, "r")
		hString = myFile.readlines()
                newDict = parseFile(hString)
                dictList.append(newDict)
		os.chdir("..")

days = []

for i in range(30):
    days.append(0)

sepDays = []

print(len(dictList))
for aDict in dictList:
   newDays = [] 
   for k in aDict:
	for i in range(30):
		newDays.append(0)
	if len(k.split("-")) > 2:
        	if k.split("-")[1] == "09":
			   newDays[int(k.split("-")[2]) - 1] += 1
			   days[int(k.split("-")[2]) - 1] += 1
   sepDays.append(newDays)

newFile = open("actions.txt", "w")
for chara in days:
	newFile.write(str(chara) + ", ")
newFile.close()

sepFile = open("sepActions.txt", "w")
print(len(sepDays))
for person in sepDays:
	for day in person:
		sepFile.write(str(day) + ", ")
	sepFile.write("\n\n")
sepFile.close()

