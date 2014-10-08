#Aggregates all click data into a form that the metrics program can use

#Makes sure that we're always in the right place
cd /vol/web/respice/

#For every folder in respice aka each participant
for d in */ ; do 
		#Enter their folder
		cd $d;
		#Get their username (minus / from the folder name)
		SUB=`echo $d | cut -d '/' -f 1`;
		#Pipe their data to the master file
		cat "$SUB.txt" >> /vol/web/respice/master-path.txt;
		#Add a new line to the master file
		echo $SUB ; sed -i -e '$a\' /vol/web/respice/master-path.txt;
		#Leave their folder
		cd ..;
done

echo "Success!"
