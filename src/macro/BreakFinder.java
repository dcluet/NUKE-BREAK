macro "BreakFinder"{

version = "1.0b 2016/04/05";

//HISTORY_________________________________________________________________________________________________________
/*
2016/04/04	Reports creation
2016/04/05	Counting and Measuring the foci
*/




//Decompacting arguments
//	Arg6 = ""+nucleus+"\t"+"Raw_Nuclei"+"\t"+"Raw_Breaks"+"\t"+Pathlog+"\t"+Log;
Argument = getArgument();
Arguments = split(Argument, "\t");

//VARIABLES________________________________________________________________________________________________________
indexN = parseFloat(Arguments[0])	//number of the nucleus in the roiManager
ImageNoyaux = Arguments[1]		//Name of the image with the nuclei
ImageBreaks = Arguments[2]		//Name of the image with the breaks
Pathlog = Arguments[3];			//Path of the log file
Log = parseFloat(Arguments[4]);		//Log file on or off
Path = Arguments[5];			//Path of the native image
TipX =  parseFloat(Arguments[6]);	//X position of the tip
TipY =  parseFloat(Arguments[7]); 	//Y position of the tip
minA = parseFloat(Arguments[8]);	//Minimum nuclei area (pixels) for initial nuclei search
maxA = parseFloat(Arguments[9]);	//Maximum nuclei area (pixels) for initial nuclei search
minC = parseFloat(Arguments[10]);	//Minimum nuclei circularity for initial nuclei search
maxC = parseFloat(Arguments[11]);	//Maximum nuclei circularity for initial nuclei search
PixW = parseFloat(Arguments[12]);	//X resolution (µm)
PixH= parseFloat(Arguments[13]);	//Y resolution (µm)
minAb= parseFloat(Arguments[14]);
maxAb= parseFloat(Arguments[15]);
minCb= parseFloat(Arguments[16]);
maxCb= parseFloat(Arguments[17]);
pathTable = Arguments[18];
TitreC = Arguments[19];


//_________________________________________________________________________________________________________________

indexNC = indexN+1;

//Acknowledging receiving correctly the arguments
	if(Log==1){
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	File.append(""+year+"/"+month+1+"/"+dayOfMonth+" "+hour+":"+minute+" BreakFinder active", Pathlog);
	File.append("Analysing nucleus: "+ indexNC, Pathlog);
	File.append("", Pathlog);
	}

//Initialize string for report
RN = "";		//Text to append to report nuclei
RF = "";		//Text to append to report foci
RH = File.openAsString(getDirectory("macros")+"NUKE-BREAK"+File.separator()+"tableline.html");
indexNC = indexN+1;

/*
<tr><td><img src="SOURCENOYAU" alt="ALTNOYAU" /></td><td>NUMBER</td><td>DISTANCE</td><td>FOCI</td>


*/




RN = RN + indexNC;
RF = RF + indexNC;
//RH = "<tr><td><img src=MONIMAGE alt=TITRE /></td><td>"+ indexNC+"</td>";
RH = replace(RH, "SOURCENOYAU", TitreC+"_Nucleus_"+indexNC+".jpg");
RH = replace(RH, "ALTNOYAU", "Nucleus_"+indexNC);
RH = replace(RH, "NUMBER", indexNC);

//Identify the original number of ROI
Nroi = roiManager("count");

//Select the Nuclei image
selectWindow(ImageNoyaux); 
//Select the ROI and get info
roiManager("Select", indexN);
S = getSliceNumber();
roiManager("Select", indexN);
Roi.getCoordinates(xnR, ynR);
//Get raw size
List.setMeasurements;
S1 = List.getValue("Area");
X = List.getValue("X");
Y = List.getValue("Y");
C = List.getValue("Circ.");

S1 = toString(S1*(PixW*PixH), 2);
D = sqrt( (X-TipX)*(X-TipX) + (Y-TipY)*(Y-TipY) ) * PixW;

//Update the report string
RN = RN + "\t" + D + "\t" + S1 + "\t" + C;
RF = RF + "\t" + D;
RH = replace(RH, "DISTANCE", D);

run("Enlarge...", "enlarge=4 pixel");
//run("Enlarge...", "enlarge=2 pixel");

Roi.getCoordinates(xn, yn);
List.setMeasurements;
Surface = List.getValue("Area");
nx = List.getValue("X");
ny = List.getValue("Y");
bx = List.getValue("BX");
by = List.getValue("BY");
w = List.getValue("Width");
h = List.getValue("Height");
S = getSliceNumber();

	//Perform analysis only if position in stack is OK
	if ((S>=4) && (S<=nSlices()-4)){
		//Creating recipient images
		newImage("Current_Nuclei", "16-bit white", w, h, 7);	
		newImage("Current_Breaks", "16-bit white", w, h, 7);	
		newImage("Stack_Breaks", "16-bit white", w, h, 7);
		newImage("Bilan_Nuclei", "8-bit white", 2*w, h, 1);	
		newImage("Bilan_Breaks", "8-bit white", 2*w, h, 1);
		
		

		//Translating the research ROI to recipient images
		Xn = xn;
		Yn = yn;
		
		for (point=0; point<lengthOf(xn); point++){
		Xn[point]= xn[point]-bx;
		Yn[point]= yn[point]-by;
		}

		//Translating the original ROI to recipient images
		XnR = xnR;
		YnR = ynR;

		for (point=0; point<lengthOf(xnR); point++){
		XnR[point] = xnR[point]-bx+1;
		YnR[point] = ynR[point]-by+1;
		}

		StartIndex = S-4;
		//Creating local mini stacks
		for (i=1; i<=7; i++){
			selectWindow(ImageNoyaux);
			setSlice(StartIndex+i);
			makeRectangle(bx,by, w,h);
			run("Copy");
			selectWindow("Current_Nuclei");
			setSlice(i);
			makeRectangle(0,0,w,h);
			run("Paste");
			
			selectWindow(ImageBreaks);
			setSlice(StartIndex+i);
			makeRectangle(bx,by, w,h);
			run("Copy");
			selectWindow("Current_Breaks");
			setSlice(i);
			makeRectangle(0,0,w,h);
			run("Paste");

			selectWindow("Breaks");
			setSlice(StartIndex+i);
			makeRectangle(bx,by, w,h);
			run("Copy");
			selectWindow("Stack_Breaks");
			setSlice(i);
			makeRectangle(0,0,w,h);
			run("Paste");


			

		}

		//Projection of the Nuclei stack channel
		selectWindow("Current_Nuclei");
		run("Invert", "stack");
		run("Invert LUT");
		run("Z Project...", "projection=[Average Intensity]");
		//run("Z Project...", "projection=[Sum Slices]");
		//Too noisy
		rename("Projected_Nucleus");
		run("8-bit");
		
		//Refining of the Chromatin Islets shape channel
		makeSelection("polygon", Xn, Yn);
		setAutoThreshold("MaxEntropy");
		run("Analyze Particles...", "size="+(minA/10)+"-"+(maxA*2)+" circularity="+minC+"-"+maxC+" show=Masks");
		rename("Detected_Nucleus");
		run("8-bit");
		//Measuring density
		makeSelection("polygon", Xn, Yn);
		List.setMeasurements;
		Dens = List.getValue("Mean");
		//Update the report string
		RN = RN + "\t" + Dens;
		
		//Transferring the projection of the nuclei to bilan image
		selectWindow("Projected_Nucleus");
		makeRectangle(0,0,w,h);
		run("Copy");
		selectWindow("Bilan_Nuclei");
		makeRectangle(0,0,w,h);
		run("Paste");

		//Transferring the detected nuclei to bilan image
		selectWindow("Detected_Nucleus");
		makeRectangle(0,0,w,h);
		run("Invert");
		//Fingerprint of enlarged ROI
		setForegroundColor(125, 125, 125);
		makeSelection("polygon", Xn, Yn);
		run("Draw");
		makeRectangle(0,0,w,h);
		run("Copy");
		selectWindow("Bilan_Nuclei");
		makeRectangle(w,0,w,h);
		run("Paste");
		
		//Preparing Bilan nuclei for channels merging
		selectWindow("Bilan_Nuclei");
		makeRectangle(0,0,getWidth(), getHeight());
		run("Invert");


		//Projection of the Breaks raw stack channel
		selectWindow("Stack_Breaks");
		//run("Invert", "stack");
		//run("Invert LUT");
		run("Z Project...", "projection=[Average Intensity]");

		//less visual on report
		//run("Z Project...", "projection=[Sum Slices]");
		//noisy
		run("Subtract Background...", "rolling=5 light slice");
		rename("Flat_Breaks");
		run("Invert");
		run("8-bit");

		//Projection of the Background removed break stack channel
		selectWindow("Current_Breaks");
		run("Invert", "stack");
		run("Invert LUT");
		run("Z Project...", "projection=[Average Intensity]");
		//run("Z Project...", "projection=[Sum Slices]");
		run("Subtract Background...", "rolling=5 light slice");
		//Too noisy
		rename("Projected_Breaks");
		run("8-bit");

		//waitForUser("");

		//Transferring the raw breaks to bilan image
		selectWindow("Flat_Breaks");
		makeRectangle(0,0,w,h);
		run("Copy");
		selectWindow("Bilan_Breaks");
		makeRectangle(0,0,w,h);
		run("Paste");

		/*
		Sensitive part: 
			1)	Find the ROI of the foci
			2)	Measure them
			3) 	Remove them without altering the initial state of the roiManager (nuclei)
		*/

		//Initial number of ROI
		oriROI = roiManager("count");

		//1)	Detecting the Breaks
		selectWindow("Projected_Breaks");
		makeSelection("polygon", Xn, Yn);
		//Best thus far but too selective with good quality
			
		setAutoThreshold("MaxEntropy");
		
	
		//setAutoThreshold("Default");
		//Not enough sensitive

//Change hardcoded size for foci FANNY
		run("Analyze Particles...", "size="+minAb+"-"+maxAb+" circularity="+minCb+"-"+maxCb+" show=Masks add");
//Change hardcoded size for foci FANNY

		rename("Detected_Breaks");
		run("8-bit");

		//New number of roi
		newROI = roiManager("count");
		
		//Number of foci
		foci = newROI - oriROI;

		//Update report 
		RN = RN + "\t" + foci;
		RH = replace(RH, "FOCI", foci);
		//2+3) Measuring and deleting the foci
		
		for (k = oriROI; k < newROI; k++){
			//The program always select the first one  due to the k -1 cmd at the end and the deletion of k'eniem roi			
			roiManager("Select", k);
			List.setMeasurements;		
			Sf = List.getValue("Area");
			Cf = List.getValue("Circ.");
			Mf = List.getValue("Mean");
			Sdf = List.getValue("StdDev");
			Solf = List.getValue("Solidity");
			
			Sf = Sf*(PixW*PixH);

			//Update report
			RF = RF + "\t" + Sf ;

			File.append(""+indexNC+"\t"+Sf+"\t"+Cf+"\t"+Mf+"\t"+Sdf+"\t"+Solf, Path+"_circ.txt");

			//Deleting the current foci
			roiManager("Delete");

			if (roiManager("count") == oriROI){
				k = newROI +1000;		//Quit the loop when the number of ROI is restored
			}else{
				k = k -1;
			} 


		}
		
		/*
		Integrety of the origianl roi manager validated 2016/04/05
		*/

		//Transferring the detected breaks to bilan image
		selectWindow("Detected_Breaks");
		makeRectangle(0,0,w,h);
		run("Invert");
		run("Copy");
		selectWindow("Bilan_Breaks");
		makeRectangle(w,0,w,h);
		run("Paste");
		
		//Preparing Bilan breaks for channels merging
		selectWindow("Bilan_Breaks");
		makeRectangle(0,0,getWidth(), getHeight());
		run("Invert");

		//Merge Channels
		run("Merge Channels...", "c1=Bilan_Breaks c3=Bilan_Nuclei");
		selectWindow("RGB");

		W1 = getWidth();
		H1 = getHeight();
		
		factor = 200/W1;

		w1 = W1 * factor;
		h1 = H1 * factor;

		run("Scale...", "x="+factor+" y="+factor+" width="+w1+" height="+h1+" interpolation=Bilinear average create");
		saveAs("Jpeg", Path+"_Nucleus_"+indexNC+".jpg");
		T=getTitle();
		
		
		

		
		//Close all remaiining images
		selectWindow("Current_Nuclei");
		close();
		selectWindow("Stack_Breaks");
		close();		
		selectWindow("Flat_Breaks");
		close();

		selectWindow("Current_Breaks");
		close();
		selectWindow("Projected_Nucleus");
		close();
		selectWindow("Detected_Nucleus");
		close();
		selectWindow("Projected_Breaks");
		close();
		selectWindow("Detected_Breaks");
		close();
		selectWindow(T);
		close();
		
		selectWindow("RGB");
		close();

		//append report
		File.append(RN, Path+"_nuclei.txt");
		File.append(RF, Path+"_foci.txt");
		File.append(RH, pathTable);
		
		/*
		Reports validated 2016/04/05
		*/

	}

}
