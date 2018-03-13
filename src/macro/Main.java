macro "Main"{

requires("1.49t")

version = "1.0d 2016/10/11";
run("Set Measurements...", "area mean centroid bounding shape display redirect=None decimal=3");


//HISTORY_________________________________________________________________________________________________________
/*
2016/03/30 	Bug for "losing" the gonade on setbatchmode fixed: ROIManager is emptied in setbatchmode when "jumping" from
		one macro to another....
2016/03/30	Integration of the research tool for the Tip
2016/03/30	Getting pixel resolution for homogeneous analysis from one picture to another
2016/04/04	Rectification of the "corection of resolution" -> from linear to square
2016/04/04	Reports creation
2016/04/04	Change of scale in GUI pixels -> µm
2016/21/04	Fixing bug with size correction of the particles with the actual resolution (done with Fanny)
2016/21/04	Fixing bug of remaining images at the end of the process (done with Fanny)
2016/22/04	Fixing bug r hyperstack creation x to "+x+"
2016/05/10  Creation of Excel table to analyse size of foci (Fanny)
*/




//VARIABLES________________________________________________________________________________________________________

Debug = 0;		//If 1 debug is ON -> All operations will be displayed -> really slow and epileptic display
Log = 1;		//If 1 creates and feed a log file with all events of the analysis
Pathlog = "";		//Path of the log file
Pathlist = "";		//Path of the file containing all identified images to be processed
Slisting = "";		//String containing all path of the images
Listing = "";		//Array containing the path of the identified images
image = 0;		//Index of the actual image in the listing
Title = "";		//Name of the actual image

PathOriHTML = getDirectory("macros")+"NUKE-BREAK"+File.separator()+"HTML_Nuke.html";

//Image properties
RefPixW =0.08265;	//Reference pixel width
RefPixH =0.08265;	//Reference pixel height

//Gonade
TipX = 0;		//X position of the tip
TipY = 0; 		//Y position of the tip








//_________________________________________________________________________________________________________________

//Open ROIManager
n = roiManager("count");

//Hide all images opening/manipulation if debug mode is off
if (Debug==0){
	setBatchMode(true);
}

//Legal Hello World
Dialog.create("Welcome to NUKE-BREAK");
Dialog.addMessage("Version\n" + version);
if (Debug==1){
Dialog.addMessage("WARNING THIS VERSION IS UNDER DEVELOPMENT!\nDEBUG MODE IS ON!");
}
Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
Dialog.show();

Names = retrieveSettingsNames();


//Analysis parameters
Dialog.create("WELCOME IN NUKE BREAK MACRO");
Dialog.addMessage("Please choose your settings");
Dialog.addChoice("Settings:",Names);
Dialog.addMessage("To create a new setting choose -NEW-");
Dialog.show();

choix = Dialog.getChoice();
index = -1;

for (c=0; c<lengthOf(Names); c++){
	if (choix == Names[c]){
	index = c;
	c =lengthOf(Names) +1000;
	}
}


//Retrieve settings
SettingsValues = retrieveSettingsValues(index);

Name = SettingsValues[0];
Extension = SettingsValues[1];
MyChoice = SettingsValues[2];
minG = parseFloat(SettingsValues[3]);
maxG = SettingsValues[4];

if (maxG!="Infinity"){
maxG = parseFloat(SettingsValues[4]);
}

AlTres = SettingsValues[5];
minAR = parseFloat(SettingsValues[6]);
maxAR = parseFloat(SettingsValues[7]);
minCR = parseFloat(SettingsValues[8]);
maxCR = parseFloat(SettingsValues[9]);

minARb = parseFloat(SettingsValues[10]);
maxARb = parseFloat(SettingsValues[11]);
minCRb = parseFloat(SettingsValues[12]);
maxCRb = parseFloat(SettingsValues[13]);

if (Name == "New"){
	TitreW = "Creation of new settings";
}else{
	TitreW = "SETTINGS";
}

Dialog.create(TitreW);
Dialog.addString("Name:", Name, 30);
Dialog.addString("File type:", Extension);
Dialog.addString("Nuclei type:", MyChoice);
Dialog.addMessage("Nuclei type MUST BE Mitose or Meiose");
Dialog.addMessage("=======================================================");
Dialog.addNumber("Minimum sample size (px):", minG);
Dialog.addString("Maximum sample size (px):", ""+maxG);
Dialog.addMessage("=======================================================");
Dialog.addString("Algorythm to find nuclei:", AlTres);
Dialog.addMessage("Algorythm must be one of the 16 different automatic thresholding methods of ImageJ");
Dialog.addNumber("Minimum nuclei area (um):", minAR);
Dialog.addNumber("Maximum nuclei area (um):", maxAR);
Dialog.addNumber("Minimum nuclei circularity:", minCR);
Dialog.addNumber("Maximum nuclei circularity:", maxCR);
Dialog.addMessage("=======================================================");
Dialog.addNumber("Minimum foci area (um):", minARb);
Dialog.addNumber("Maximum foci area (um):", maxARb);
Dialog.addNumber("Minimum foci circularity:", minCRb);
Dialog.addNumber("Maximum foci circularity:", maxCRb);
if (Name=="New"){
Dialog.addMessage("=======================================================");
Dialog.addMessage("Once validated these settings will be saved!");
Dialog.addMessage("Re-launch the macro to use them!");
Dialog.addMessage("=======================================================");
}

Dialog.show();


Log = 1;

if(Name == "New"){
	sets = newArray(14);
	sets[0]=Dialog.getString();
	sets[1]=Dialog.getString();
	sets[2]=Dialog.getString();

	sets[3]=Dialog.getNumber();
	sets[4]=Dialog.getString();

	sets[5]=Dialog.getString();
	sets[6]=Dialog.getNumber();
	sets[7]=Dialog.getNumber();
	sets[8]=Dialog.getNumber();
	sets[9]=Dialog.getNumber();

	sets[10]=Dialog.getNumber();
	sets[11]=Dialog.getNumber();
	sets[12]=Dialog.getNumber();
	sets[13]=Dialog.getNumber();

	createSettings(sets);


exit();
}

minARi = minAR;
maxARi = maxAR;
minCRi = minCR;
maxCRi = maxCR;


minARbi = minARb;
maxARbi = maxARb;
minCRbi = minCRb;
maxCRbi = maxCRb;



//Correcting min and max AREA for Nuclei in pixels
minAR = minAR/(RefPixW*RefPixH);
maxAR = maxAR/(RefPixW*RefPixH);

//Correcting min and max AREA for Breaks in pixels
minARb = minARb/(RefPixW*RefPixH);
maxARb = maxARb/(RefPixW*RefPixH);

//Retrieving the path of the local folder to process
Folder = getDirectory("Folder to NUKE-BREAK "+ MyChoice);

//Genaerating the Pathlog and Pathlist path
Pathlog = Folder+File.separator()+"Analysis_log.txt";
Pathlist = Folder+File.separator()+"ListFiles.txt";

PathBilanNoyaux = Folder+File.separator()+"Bilan_nuclei.txt";
	BN = File.open(PathBilanNoyaux);
	File.close(BN);

PathBilanFoci = Folder+File.separator()+"Bilan_foci.txt";
	BF = File.open(PathBilanFoci);
	File.close(BF);

//Creation/Erasing the list file
Files = File.open(Pathlist);
File.close(Files);

//If log is ON creation of the file and initial feeding
	if (Log == 1){
	//Creation/Erasing the log file
	LOGFile = File.open(Pathlog);
	File.close(LOGFile);

	//Feeding the log
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" BEGINNING OF ANALYSIS", Pathlog);
	File.append("", Pathlog);
	File.append("PARAMETERS:", Pathlog);
	File.append("\tFolder: "+Folder, Pathlog);
	File.append("\tMinimum gonade area: "+minG, Pathlog);
	File.append("\tMaximum gonade area: "+maxG, Pathlog);
	File.append("\tMinimum nuclei area: "+minAR, Pathlog);
	File.append("\tMaximum nuclei area: "+maxAR, Pathlog);
	File.append("\tMinimum nuclei circularity: "+minCR, Pathlog);
	File.append("\tMaximum nuclei circularity: "+maxCR, Pathlog);
	File.append("", Folder+File.separator()+"Analysis_log.txt");
	}

//Searching for all compatible images in the folder
Arg1 = "" + Folder +"\t"+Extension+ "\t" + Log;
	if (Log == 1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING Explorer", Pathlog);
	File.append("", Pathlog);
	}
runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Explorer.java", Arg1);

//Retrieving Path of identified Images
Slisting = File.openAsString(Pathlist);
Listing = split(Slisting, "\n");

	if (Debug==1){
	waitForUser("Number of identified images: " + lengthOf(Listing));
	}

	if (Log == 1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" STARTING IMAGE PROCESSING", Pathlog);
	File.append("", Pathlog);
	}


//Main loop of Analysis
for (image = 0; image < lengthOf(Listing); image++){

	TipX = 0;	//X position of the tip
	TipY = 0; 	//Y position of the tip

	//Cleaning the roimanager
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"ROIeraser.java");

	//Opening the image
	open(Listing[image]);

	//Creating the output Folder
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	Tag = "_"+year+""+(month+1)+""+dayOfMonth+"_"+hour+""+minute;
	TitreC = File.nameWithoutExtension;
	OutputPath = File.getParent(Listing[image])+File.separator()+TitreC+Tag+File.separator();

	o = File.makeDirectory(OutputPath);



	//Retrieve current resolution
	getPixelSize(unit, PixW, PixH, pd);

	//Correction of the research parameters
	minA = minAR * (RefPixW*RefPixH)  / (PixW*PixH);
	maxA = maxAR * (RefPixW*RefPixH) / (PixW*PixH);
	minC = minCR;
	maxC = maxCR;

	minAb = minARb * (RefPixW*RefPixH)  / (PixW*PixH);
	maxAb = maxARb * (RefPixW*RefPixH) / (PixW*PixH);
	minCb = minCRb;
	maxCb = maxCRb;

	//Get Image Name
	Title = getTitle();
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" TREATING IMAGE:", Pathlog);
		File.append("\t" + Title, Pathlog);
		File.append("\t" + "Pixel Width: "+PixW+" "+unit, Pathlog);
		File.append("\t" + "Pixel Height: "+PixH+" "+unit, Pathlog);
		File.append("", Pathlog);
		}

	//Create the reports files
	F1 = File.open(OutputPath+TitreC+"_nuclei.txt");
	File.close(F1);

	F2 = File.open(OutputPath+TitreC+"_foci.txt");
	File.close(F2);

	F3 = File.open(OutputPath+TitreC+"_circ.txt");
	File.close(F3);
	File.append("Nucleus"+"\t"+"Area"+"\t"+"Circularity"+"\t"+"Mean"+"\t"+"StdDev"+"\t"+"Solidity", OutputPath+TitreC+"_circ.txt");


	//Initialize the report file
	Header1 = "Nuclei number" + "\t" + "Distance from tip (µm)" + "\t" + "Surface (µm2)" + "\t" + "Circularity" + "\t" + "Density" + "\t"+ "Number of foci";
	Header2 = "Nuclei number" + "\t" + "Distance from tip (µm)" + "\t" + "listing sizes (µm2)";

	File.append(Title, OutputPath+TitreC+"_nuclei.txt");
	File.append("X resolution (µm): "+ PixW, OutputPath+TitreC+"_nuclei.txt");
	File.append("Y resolution (µm): "+ PixH, OutputPath+TitreC+"_nuclei.txt");
	File.append(Header1, OutputPath+TitreC+"_nuclei.txt");

	File.append(Title, OutputPath+TitreC+"_foci.txt");
	File.append("X resolution (µm): "+ PixW, OutputPath+TitreC+"_foci.txt");
	File.append("Y resolution (µm): "+ PixH, OutputPath+TitreC+"_foci.txt");
	File.append(Header2, OutputPath+TitreC+"_foci.txt");

		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" Creating reports:", Pathlog);
		File.append("\t" + OutputPath+TitreC+"_nuclei.txt", Pathlog);
		File.append("\t" + OutputPath+TitreC+"_foci.txt", Pathlog);
		File.append("", Pathlog);
		}

	//Preparing the channels
	Arg2 = ""+Title +"\t"+ Folder+"\t"+Log;
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING Cleaner", Pathlog);
		File.append("", Pathlog);
		}
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Cleaner.java", Arg2);

	//Identifying the gonade shape
	//Arg3 = "Homogenized_Gonade"+"\t"+Pathlog+"\t"+Log+"\t"+minG+"\t"+maxG;
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING Gonade Detector", Pathlog);
		File.append("", Pathlog);
		}
	//runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"GonadeDetector.java", Arg3);

	if(MyChoice == "Mitose"){

	//waitForUser("Mitose" + MyChoice);

	selectWindow("Homogenized_Gonade");
	setAutoThreshold("Huang dark");
	run("Analyze Particles...", "size="+minG+"-"+maxG+" pixel show=Masks add");
	//run("Analyze Particles...", "size="+minG+"-"+maxG+" pixel add");

		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" DETECTING THE GONADE", Pathlog);
		File.append("\t" + roiManager("count") + " particles found.", Pathlog);
		File.append("", Pathlog);
		}



	//Detecting the tip of the gonade
	rename("Detected_Gonade");
	GWidth = getWidth();
	GHeight = getHeight();
	}

	if ((roiManager("count")==1)&&(MyChoice == "Mitose")){


	//waitForUser("Mitose" + MyChoice);

	roiManager("Select", 0);

	List.setMeasurements;
	GBX = List.getValue("BX");
	GBY = List.getValue("BY");
	GBW = List.getValue("Width");
	GBH = List.getValue("Height");


	if (GBH>GBW){
	//Gonade is vertical
		if (Log == 1){
			File.append("\t" + "Gonade is vertical", Pathlog);
		}

		if ((GBY>0) && (GBY+GBH == GHeight)){
			if (Log == 1){
				File.append("\t" + "Gonade is DOWN", Pathlog);
			}
			//OK on test 2016/03/30
			makeRectangle(GBX,GBY, GBW, 10);
			Profile = getProfile;
			Max = Array.findMaxima(Profile, 10);
			TipX = Max[0]+GBX;
			TipY = GBY;

		}else if ((GBY==0) && (GBY+GBH < GHeight)){
			if (Log == 1){
				File.append("\t" + "Gonade is UP", Pathlog);
			}
			//OK on test 2016/03/30
			makeRectangle(GBX, GBH-10, GBW, 10);
			Profile = getProfile;
			Max = Array.findMaxima(Profile, 10);
			TipX = Max[0]+GBX;
			TipY = GBH;

		}else{
			if (Log == 1){
				File.append("\t" + "Tip can't be found", Pathlog);
			}
		}

	} else{
	//Gonade is horizontal
		if (Log == 1){
			File.append("\t" + "Gonade is horizontal", Pathlog);
		}

		run("Rotate 90 Degrees Right");

		Wi = getWidth();
		Hi = getHeight();

		if ((GBX>0) && (GBX+GBW == GWidth)){
			if (Log == 1){
				File.append("\t" + "Gonade is RIGHT", Pathlog);
			}
			//OK on test 2016/03/30
			makeRectangle(Wi-(GBY+GBH), GBX, GBH, 10);
			Profile = getProfile;
			Max = Array.findMaxima(Profile, 10);
			TipX = GBX;
			TipY = Wi-(Max[0]+Wi-(GBY+GBH));

		}else if ((GBX==0) && (GBX+GBW < GWidth)){
			if (Log == 1){
				File.append("\t" + "Gonade is LEFT", Pathlog);
			}
			//OK on test 2016/03/30
			makeRectangle(Wi-(GBY+GBH), GBW-10, GBH, 10);
			Profile = getProfile;
			Max = Array.findMaxima(Profile, 10);
			TipX = GBW;
			TipY = Wi-(Max[0]+Wi-(GBY+GBH));

		}else{
			if (Log == 1){
				File.append("\t" + "Tip can't be found", Pathlog);
			}
		}


	}

	//waitForUser("Gonade tip "+ TipX+";"+TipY);

	selectWindow("Detected_Gonade");

	close();


	}//END IF MITOSE AND GONADE DETECTED


	if (MyChoice == "Meiose"){

	//waitForUser("Meiose" + MyChoice);
	selectWindow("Homogenized_Gonade");
	GBX = 0;
	GBY = 0;
	GBW = getWidth();
	GBH = getHeight();
	makeRectangle(0,0,GBW,GBH);
	roiManager("Add");


	TipX = GBW+GBX;
	TipY = GBH/2+GBY;

	}//END IF MEIOSE

	if (Log == 1){
		File.append("\t" + "Tip coordinates: "+TipX+";"+TipY, Pathlog);
		File.append("", Pathlog);
	}

	//makeRectangle(TipX-5, TipY-5, 10, 10);
	//waitForUser(""+TipX+";"+TipY);



	//The ROI will be deleted in NucleiDetector

	/* NEW CLEANING OF NUCLEI AND BREAKS



	*/

	//Storing coordinate of the gonade
	roiManager("Select", 0);
	Roi.getCoordinates(xG, yG);


	//CLEANING NUCLEI
	selectWindow("Nuclei");
	roiManager("Select", 0);
		run("Enhance Contrast...", "saturated=0 normalize process_all");
		//run("Subtract Background...", "rolling=50 stack");

	selectWindow("Raw_Nuclei");
	roiManager("Select", 0);
		run("Enhance Contrast...", "saturated=0 normalize process_all");
		//run("Subtract Background...", "rolling=50 stack");


	//CLEANING NUCLEI
	selectWindow("Breaks");
	roiManager("Select", 0);
		run("Enhance Contrast...", "saturated=0 normalize process_all");
		run("Subtract Background...", "rolling=50 stack");

	selectWindow("Raw_Breaks");
	roiManager("Select", 0);
		run("Enhance Contrast...", "saturated=0 normalize process_all");
		run("Subtract Background...", "rolling=50 stack");

	/* Old treatment to find potential Foci

	selectWindow("Raw_Breaks");
	setSlice(1);
	makeRectangle(0,0, getWidth(), getHeight());
	run("Convert to Mask", "method=MaxEntropy background=Dark calculate");



	//Creation tiff Fanny
	run("Merge Channels...","c1=Raw_Breaks c3=Raw_Nuclei create keep");
	x=nSlices/2;

	run("Stack to Hyperstack...", "order=xyczt(default) channels=2 slices="+x+" frames=1 display=Color");
	rename(Title);

	//Remove known extension

	saveAs(".tif", OutputPath+TitreC+".tif");
	close();
	*/

 	//Identifying nuclei
	Arg4 = "Raw_Nuclei"+"\t"+Pathlog+"\t"+Log+"\t"+minA+"\t"+maxA+"\t"+minC+"\t"+maxC+"\t"+AlTres;
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING Nuclei Detector", Pathlog);
		File.append("", Pathlog);
		}
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"NucleiDetector.java", Arg4);

	//Removing the doublets
	Arg5 = ""+Pathlog+"\t"+Log;
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING TwinKiller", Pathlog);
		File.append("", Pathlog);
		}
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"TwinKiller.java", Arg5);


	//Sava image
	saveAs("Tif", OutputPath+TitreC+"_Detected_Nuclei.tif");
	N1 = getTitle();

	run("Invert LUT");
	makeRectangle(0, 0, getWidth(), getHeight());
	run("Z Project...", "projection=[Sum Slices]");
	run("RGB Color");


	makeSelection("polygon", xG, yG);
	setForegroundColor(255,0,0);
	run("Draw");
	makeOval(TipX-10,TipY-10,20,20);
	setForegroundColor(0,255,0);
	run("Fill");
	saveAs("Jpeg", OutputPath+TitreC+"_Detected_Nuclei.jpg");
	close();
	open(OutputPath+TitreC+"_Detected_Nuclei.jpg");

	im = getTitle();
	createThumb(OutputPath, im, TitreC+"_Detected_Nuclei",885);




	N2 = getTitle();

	//Creating temp table file for HTML
	F3 = File.open(OutputPath+TitreC+"_table.txt");
	File.close(F3);

	Header = newArray("ANALYSIS", "Index of the Nucleus","Distance from the tip (um)","Number of foci");

	Tableau = "DETECTED NUCLEI AND Rad51 FOCI\n"
		+"<table>\n"
		+"<thead>\n";

	for(C=0; C<lengthOf(Header);C++){
		Tableau +=  "<td><B>"+ Header[C]+"</B></td>";
	}
	Tableau += "</thead>"+"\n"+ "<body>";

	File.append(Tableau,OutputPath+TitreC+"_table.txt");







	//Loop of analysis of each nuclei (with local refining)
	for (nucleus = 0; nucleus < roiManager("count"); nucleus ++){
	Arg6 = ""+nucleus+"\t"+"Nuclei"+"\t"+"Raw_Breaks"+"\t"+Pathlog+"\t"+Log+"\t"+OutputPath+TitreC+"\t"+TipX+"\t"+TipY+"\t"+minA+"\t"+maxA+"\t"+0+"\t"+maxC+"\t"+PixW+"\t"+PixH+"\t"+minAb+"\t"+maxAb+"\t"+minCb+"\t"+maxCb+"\t"+OutputPath+TitreC+"_table.txt"+"\t"+TitreC;
		if (Log == 1){
		getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
		File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" LAUCHING BreakFinder", Pathlog);
		File.append("", Pathlog);
		}
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"BreakFinder.java", Arg6);
	}

	//Finishing table for html
	Tableau = "</body></table>";
	File.append(Tableau,OutputPath+TitreC+"_table.txt");


	//waitForUser("Images");

	//Closing images
	selectWindow("MASK_nuclei");
	close();
	selectWindow("Raw_Breaks");
	close();
	selectWindow("Homogenized_Gonade");
	close();
	selectWindow("Nuclei");
	close();
	selectWindow("Breaks");
	close();
	selectWindow(N1);
	close();
	selectWindow(N2);
	close();


	E = File.delete(OutputPath+TitreC+"_Detected_Nuclei.tif");

	//Cleaning the roimanager
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"ROIeraser.java");

	//Initial analysis without QC
	Arg7 = OutputPath+TitreC +"_nuclei.txt"+"\t"+MyChoice;
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Table.java", Arg7);

	Arg8 = OutputPath+TitreC +"_foci.txt"+"\t"+MyChoice;
	runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Table.java", Arg8);

	NNonC = File.openAsString(OutputPath+TitreC +"_nuclei.txt");
	File.append(NNonC, PathBilanNoyaux);

	FNonC = File.openAsString(OutputPath+TitreC +"_foci.txt");
	File.append(FNonC, PathBilanFoci);


	//waitForUser(""+OutputPath+TitreC+"_nuclei.xls");
	Res = getStat(OutputPath+TitreC+"_nuclei.xls");

	open(OutputPath+TitreC+"_nuclei.txt_Cumulative_Foci.jpg");
	im = getTitle();
	createThumb(OutputPath, im, TitreC+"_nuclei.txt_Cumulative_Foci.jpg",885);
	close();

	//Creating HTML
	ListeFichierJ = newArray("ENS.jpg", "CNRS.jpg", "LBMC.jpg", "UCBL.jpg");

	for (k=0; k<lengthOf(ListeFichierJ); k++){
		open(getDirectory("macros")+"NUKE-BREAK"+File.separator()+ListeFichierJ[k]);
		saveAs("Jpeg", OutputPath+ListeFichierJ[k]);
		close();
	}

	CSS = File.openAsRawString(getDirectory("macros")+"NUKE-BREAK"+File.separator()+"style_Nuke.css");
	File.saveString(CSS, OutputPath+"style_Nuke.css");

	Tableau = File.openAsString(OutputPath+TitreC+"_table.txt");



	HTML = File.openAsRawString(PathOriHTML);

	HTML = replace(HTML, "MON_TITRE_ANALYSE", TitreC);

	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	myDate=""+year+"/"+(month+1)+"/"+dayOfMonth+" at "+hour+":"+minute;

	HTML = replace(HTML, "ATIMEA", myDate);

	HTML = replace(HTML, "VERSION", version);
	HTML = replace(HTML, "NAMESETTINGS",  Name);
	HTML = replace(HTML, "EXTENSION",  Extension);
	HTML = replace(HTML, "MYCHOICE",  MyChoice);
	HTML = replace(HTML, "MING",  minG);
	HTML = replace(HTML, "MAXG",  maxG);
	HTML = replace(HTML, "ALTRES",  AlTres);
	HTML = replace(HTML, "MINAR",  minARi);
	HTML = replace(HTML, "MAXAR",  maxARi);
	HTML = replace(HTML, "MINCR",  minCRi);
	HTML = replace(HTML, "MAXCR",  maxCRi);
	HTML = replace(HTML, "MINbARb",  minARbi);
	HTML = replace(HTML, "MAXbARb",  maxARbi);
	HTML = replace(HTML, "MINbCRb",  minCRbi);
	HTML = replace(HTML, "MAXbCRb",  maxCRbi);

	HTML = replace(HTML, "RESOLUTION",  PixW);

	HTML = replace(HTML, "NOMBRENUCLEI",  Res[0]);
	HTML = replace(HTML, "NOMBREFOCI",  Res[1]);
	HTML = replace(HTML, "NOMBRENUCLFOCI",  Res[2]);

	HTML = replace(HTML, "MONTITREIMAGE3A", "Cumulative curve of foci over distance from the tip of the gonage");
	HTML = replace(HTML, "NOMFICHIERIMAGE3A", TitreC+"_nuclei.txt_Cumulative_Foci.jpg");
	HTML = replace(HTML, "PATHIMAGE3A", TitreC+"_nuclei.txt_Cumulative_Foci.jpg_thumb.jpg");
	HTML = replace(HTML, "ALTIMAGE3A", "Cumulative curve");
	HTML = replace(HTML, "NOMIMAGE3A", "Cumulative curve");
	HTML = replace(HTML, "MONSOUSTITREIMAGE3A", "Click To Enlarge!");


	HTML = replace(HTML, "MONCHEMIN_BILAN1", OutputPath+TitreC +"_nuclei.xls");
	HTML = replace(HTML, "MONCHEMIN_BILAN2", OutputPath+TitreC +"_foci.xls");


	HTML = replace(HTML, "MONTITREIMAGE2A", "BILAN");
	HTML = replace(HTML, "NOMFICHIERIMAGE2A", TitreC+"_Detected_Nuclei.jpg");
	HTML = replace(HTML, "PATHIMAGE2A", TitreC+"_Detected_Nuclei_thumb.jpg");
	HTML = replace(HTML, "ALTIMAGE2A", "Bilan picture of the entire analysis process");
	HTML = replace(HTML, "NOMIMAGE2A", "Bilan picture of the entire analysis process");
	HTML = replace(HTML, "MONSOUSTITREIMAGE2A", "Click To Enlarge!");

	HTML = replace(HTML, "TABLEAU1", Tableau);
	HTML = replace(HTML, "PATHDOSSIER", TitreC+Tag);




	File.saveString(HTML, File.getParent(Listing[image])+File.separator()+TitreC+Tag+".html");



	//Cleaning Memory
	call("java.lang.System.gc");
}



//Cleaning the roimanager
runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"ROIeraser.java");

//Graph Generaux
runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Table.java", PathBilanNoyaux+"\t"+MyChoice);
runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Table.java", PathBilanFoci+"\t"+MyChoice);

//Cleaning Memory
call("java.lang.System.gc");

waitForUser("ANALYSIS IS OVER");



//_____________________________________________________________________________________________________________________________
function getStat(Pathxls){
r=newArray(3);
t= File.openAsString(Pathxls);
table= split(t, "\n");
r[0] = lengthOf(table)-1; //remove header

for (l=1; l<lengthOf(table); l++){
	//waitForUser(""+table[l]);
	line = split(table[l], "\t");
	r[1]=r[1]+line[4];
	if(line[4]>0){
		r[2]=r[2]+1;
	}
}
return r;

}

function createThumb(PathFolder, im, Nom, Size){
selectWindow(im);
H = getHeight();
W = getWidth();
ratio = Size/W;
newH = round(H*ratio);
newW = Size;
makeRectangle(0,0, W,H);
run("Scale...", "x=- y=- width="+newW+" height="+newH+" interpolation=Bilinear average create");

saveAs("Jpeg", PathFolder+Nom+"_thumb.jpg");
close();
}

function retrieveSettingsNames(){

T = File.openAsString(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");
TL = split(T, "\n");

N = newArray(lengthOf(TL));

for (i=0; i<lengthOf(TL); i++){
	l= split(TL[i], "\t");
	N[i] = l[0];
		}

return N;
}

function retrieveSettingsValues(L){

T = File.openAsString(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");
TL = split(T, "\n");

V= split(TL[L], "\t");

return V;
}


function createSettings(M){
T = File.openAsString(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");
TL = split(T, "\n");

text="";

File.saveString(text, getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");

for(k=0; k<lengthOf(TL)-1; k++){
	File.append(TL[k], getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");
}


t = M[0] + "\t" + M[1] + "\t" + M[2] + "\t" + M[3] + "\t" + M[4] + "\t" + M[5] + "\t" + M[6] + "\t" + M[7] + "\t" + M[8] + "\t" + M[9] + "\t" + M[10] + "\t" + M[11] + "\t" + M[12] + "\t" + M[13];

File.append(t, getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");

Nom = "New";
Extension = ".lsm";
Type = "Mitose";
//or "Meiose"
GonadeMin = "50000";
GonadeMax = "Infinity";
TresholdN = "Default";
MinAreaN = "3.8";
MaxAreaN = "8";
MinCircN = "0";
MaxCircN = "1";
MinAreaF = "0";
MaxAreaF = "1";
MinCircF = "0";
MaxCircF = "1";

T = Nom +"\t"+ Extension +"\t"+ Type +"\t"+ GonadeMin +"\t"+ GonadeMax +"\t"+ TresholdN +"\t"+ MinAreaN +"\t"+ MaxAreaN +"\t"+ MinCircN +"\t"+ MaxCircN +"\t"+ MinAreaF +"\t"+ MaxAreaF +"\t"+ MinCircF +"\t"+ MaxCircF ;
File.append(T, getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");

}





}



//END OF MACRO
