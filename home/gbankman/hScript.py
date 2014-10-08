import os
import fnmatch
hDict = {}

print(os.path.isdir("5c992aab0"))
def parseFile(hString):
    kvList = "".join(hString)
    kvList = kvList.split("||")
    for i in range(len(kvList)):
        kvList[i] = kvList[i].split(",")
    return kvList

for file in os.listdir(os.getcwd()):
     print(file)
     if(os.path.isdir(str(file))):
	for innerFile in os.listdir(file):
	     if fnmatch.fnmatch(innerFile, '*happy*'):
		os.chdir(file)
		myFile = open(innerFile, "r")
		hString = myFile.readlines()
                parsed = parseFile(hString)
                hDict[innerFile] = parsed
        os.chdir("..")
print(hDict)



