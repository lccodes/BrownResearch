#!/usr/bin/python

import sys 
import csv
import string

def pullIDs(fname,ls,nl,nu,last):

	read = csv.reader(open(fname, 'r'), delimiter=';')
	writeToXOR = csv.writer(open('/home/betsy/xor.txt', 'w'))
	writeToPrint = csv.writer(open('/home/betsy/toPrint.txt', 'w'))
	finString=''
	for row in read:
		allUserExt=row
		i=0
		for entry in allUserExt:
			if entry.split(',')[0]==last:
				loc = i+1
			i+=1
		for l in range(int(ls),(int(nl)+int(ls))):
			for u in range(1,(int(nu)+1)):
				user = allUserExt[loc].split(',')
				userStr =user[0]+","+"bcsgff"+str(l)+"_"+str(u)+";France"+str(l)+str(u)+"|"
				finString=finString+userStr
				print "Ext. ID: "+user[0]+"  Ext. PW: "+user[1][0:9]+"  Yahoo ID: bcsgff"+str(l)+"_"+str(u)
				print ""
				print ""
				print ""
				#print user[0]+" "+user[1][0:9]+" bcsgff"+str(l)+"_"+str(u)+" France"+str(l)+str(u)
				loc+=1

 
		#write.writerow(newrow)
		#print finString	
	
def toInt(c):
	num = c.split(':')
	print num
	ans = int(num[0])*3600 +int(num[1])*60+int(num[2])
	return ans

def main(filename,ls,nl,nu,last):
	pullIDs(filename,ls,nl,nu,last)


if __name__ == "__main__":
    main(sys.argv[1],sys.argv[2],sys.argv[3], sys.argv[4],sys.argv[5])
