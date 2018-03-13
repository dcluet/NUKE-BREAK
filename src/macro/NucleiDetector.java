macro "NucleiDetector"{

version = "1.0a 2015/03/02";

//Decompacting arguments
Argument = getArgument();
Arguments = split(Argument, "\t");

//VARIABLES________________________________________________________________________________________________________
ImageName = Arguments[0];				//Name of the image to treat 
Pathlog = Arguments[1];					//Path of the log file
Log = parseFloat(Arguments[2]);				//Log file on or off
minA = parseFloat(Arguments[3]);			//Minimum nuclei area (pixels) for initial nuclei search
maxA = parseFloat(Arguments[4]);			//Maximum nuclei area (pixels) for initial nuclei search	
minC = parseFloat(Arguments[5]);			//Minimum nuclei circularity for initial nuclei search
maxC = parseFloat(Arguments[6]);			//Maximum nuclei circularity for initial nuclei search
Tr = Arguments[7];					//Algorythm for thresholding
xG =""							//x Coordinates of the shape of the gonade
yG =""							//y Coordinates of the shape of the gonade
//_________________________________________________________________________________________________________________

//Acknowledging receiving correctly the arguments
if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Nuclei Detector active", Pathlog);
	File.append("Arguments:", Pathlog);
	File.append("\tImage to treat: "+ ImageName, Pathlog);
	File.append("\tMinimum Area: "+ minA, Pathlog);
	File.append("\tMaximum Area: "+ maxA, Pathlog);
	File.append("\tMinimum Circularity: "+ minC, Pathlog);
	File.append("\tMaximum Circularity: "+ maxC, Pathlog);
	File.append("", Pathlog);
}

//Saving the coordinates of the gonade in arrays 
roiManager("Select", 0);	
Roi.getCoordinates(xG, yG);
//Deleting the ROI
roiManager("Delete");

selectWindow(ImageName);


//waitForUser("");


//Binarising the nuclei
//setAutoThreshold("Default dark");
//setOption("BlackBackground", false);
run("Convert to Mask", "method="+Tr+" background=Dark calculate");
//Increasing the noise as a blur to disconnect the nuclei		
run("Invert", "stack");
run("Gaussian Blur...", "sigma=2 stack");
setAutoThreshold("Default dark");
rename("MASK_nuclei");
//Real identification of the nuclei present WITHIN the shape of the gonade
makeSelection("polygon", xG, yG);
//Smoothing and enlarging Gonade
run("Enlarge...", "enlarge=5 pixel");
//Searching for nuclei
run("Analyze Particles...", "size="+minA+"-"+maxA+" pixel circularity="+minC+"-"+maxC+" show=Masks exclude include add stack");
rename("Nuclei");

	if (Log == 1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" DETECTING NUCLEI", Pathlog);
	File.append("\t" + roiManager("count") + " particles found.", Pathlog);
	File.append("", Pathlog);
	}
}
