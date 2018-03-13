macro "GonadeDetector"{

version = "1.0a 2015/03/02";

//Decompacting arguments
Argument = getArgument();
Arguments = split(Argument, "\t");

//VARIABLES________________________________________________________________________________________________________
ImageName = Arguments[0];				//Name of the image to treat 
Pathlog = Arguments[1];					//Path of the log file
Log = parseFloat(Arguments[2]);				//Log file on or off
minG = parseFloat(Arguments[3]);			//Minimum size of the gonade to be found
maxG = Arguments[4];					//Maximum size of the gonade to be found
if (maxG != "Infinity"){
	maxG = parseFloat(maxG);
}
//_________________________________________________________________________________________________________________

//Acknowledging receiving correctly the arguments
	if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Gonade Detector active", Pathlog);
	File.append("Arguments:", Pathlog);
	File.append("\tImage to treat: "+ ImageName, Pathlog);
	File.append("\tMinimum Area: "+ minG, Pathlog);
	File.append("\tMaximum Area: "+ maxG, Pathlog);
	File.append("", Pathlog);
	}

//Searching for the Gonade
selectWindow(ImageName);
setAutoThreshold("Huang dark");
run("Analyze Particles...", "size="+minG+"-"+maxG+" pixel add");
	
	if (Log == 1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" DETECTING THE GONADE", Pathlog);
	File.append("\t" + roiManager("count") + " particles found.", Pathlog);
	File.append("", Pathlog);
	}

waitForUser("Number gonade "+roiManager("count"));
}
