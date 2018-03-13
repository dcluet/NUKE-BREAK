macro "Explorer"{

version = "1.0b 2016/04/22";

//HISTORY_________________________________________________________________________________________________________
/*
2016/22/04	Fixing troubles with arborescence recognition
*/


//Decompacting arguments
Argument = getArgument();
Arguments = split(Argument, "\t");

//VARIABLES________________________________________________________________________________________________________
Folder = Arguments[0];					//Path of the root folder to explor
Extension = Arguments[1];				//extension of the valid files
Log = parseFloat(Arguments[2]);				//Log file on or off
Pathlog = Folder+File.separator()+"Analysis_log.txt";	//Path of the log file
Pathlist = Folder+File.separator()+"ListFiles.txt";	//Path of the file containing all identified images to be processed
i = 0;							//Index in the array of identified files
//_________________________________________________________________________________________________________________

//Acknowledging receiving correctly the arguments
if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Explorer active", Pathlog);
	File.append("Arguments: ", Pathlog);
	File.append("\tFolder: "+ Folder, Pathlog);
	File.append("\tExtension: "+ Extension, Pathlog);
	File.append("", Pathlog);
}


//Finding the files and adding their path in the listing file
listFiles(Folder, Extension); 

if(Log==1){
	File.append("", Pathlog);
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" END OF EXPLORING", Pathlog);
	File.append("", Pathlog);
}


//FUNCTIONS________________________________________________________________________________________________________

function listFiles(folder, extension) {

	
	list = getFileList(folder);
	for (i=0; i<list.length; i++) {
			
			//print (list[i]));

		
       		if (	File.isDirectory(folder+list[i])	){
           	listFiles(""+folder+list[i], extension);
       		}
		
		if (endsWith(list[i], extension)){
		File.append(""+folder+list[i], Pathlist);
			if(Log==1){
				getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
				File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" File found:", Pathlog);
				File.append(""+folder+list[i], Pathlog);
			}
		}
	}
}
}	//End of the macro
