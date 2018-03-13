macro "Cleaner"{

version = "1.0a 2016/04/05";

//HISTORY_________________________________________________________________________________________________________
/*
2016/03/30 	Increasing the cleaning of the foci channel
2016/04/05	Enhanced cleaning of the foci
2016/04/22	Channel attribution has been fitted for LSM files (1=blue, 2=red)
2016/04/22	Auto detection of labelling
*/



//Decompacting arguments
Argument = getArgument();
Arguments = split(Argument, "\t");

//VARIABLES________________________________________________________________________________________________________
Title = Arguments[0];					//Title of the image to treat
Folder = Arguments[1];					//Path of the root folder to explor
Log = parseFloat(Arguments[2]);				//Log file on or off
Pathlog = Folder+File.separator()+"Analysis_log.txt";	//Path of the log file
//_________________________________________________________________________________________________________________

//Acknowledging receiving correctly the arguments
if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Cleaner active", Pathlog);
	File.append("Arguments:", Pathlog);
	File.append("\tTitle: "+ Title, Pathlog);
	File.append("\tFolder: "+ Folder, Pathlog);
	File.append("", Pathlog);
}

//Separating the channels
run("Split Channels");

//Renaming the channels
selectWindow("C1-"+Title);
getLut(reds, greens, blues);
if (reds[255]>blues[255]){
	rename("Raw_Breaks");
}else{
	rename("Raw_Nuclei");
}

selectWindow("C2-"+Title);
getLut(reds, greens, blues);
if (reds[255]>blues[255]){
	rename("Raw_Breaks");
}else{
	rename("Raw_Nuclei");
}

selectWindow("Raw_Nuclei");
run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=0.2000000");
run("Grays");

selectWindow("Raw_Breaks");
run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=0.2000000");
run("Grays");
run("Duplicate...", "title=Breaks duplicate");
selectWindow("Raw_Breaks");

//Treating Nuclei
selectWindow("Raw_Nuclei");
run("Subtract Background...", "rolling=50 stack");
if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Nuclei cleaned", Pathlog);
}
run("Duplicate...", "title=Gonade duplicate");
run("Z Project...", "projection=[Max Intensity]");
rename("Max");
selectWindow("Gonade");
run("Z Project...", "projection=[Average Intensity]");
rename("Average");
imageCalculator("Subtract create", "Max","Average");
rename("Homogenized_Gonade");


selectWindow("Max");
close();
selectWindow("Average");
close();
selectWindow("Gonade");
close();
selectWindow("Raw_Nuclei");
run("Duplicate...", "title=Nuclei duplicate");
if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Gonade Homogenized", Pathlog);
}


}	//End of the macro
