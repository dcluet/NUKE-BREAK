macro "Installation_2018-03-13"{

tag = "v1.0.0"
lastStableCommit = "07b6a9e7"
myProgram = "Nuclei and DNA damage Analysis";


//IJ version verification and close the macro's window
//selectWindow("Installation.ijm");			
//run("Close");
requires("1.49g");

//Initialisation of the error counter
Errors=0;

//GUI Message
Welcome(myProgram, tag, lastStableCommit);

//Prepare key paths
PathSUM = getDirectory("macros")+File.separator+"StartupMacros.fiji.ijm";
PathFolderInput =File.directory+File.separator+"macro"+File.separator;
PathOutput = getDirectory("macros")+"NUKE-BREAK"+File.separator;

//Listing of the files to instal
Listing = newArray("Main.java","Explorer.java", "Cleaner.java", "ROIeraser.java", "TwinKiller.java", "NucleiDetector.java", "GonadeDetector.java", "BreakFinder.java", "Table_Analysis.java", "Table.java", "CNRS.jpg", "ENS.jpg", "LBMC.jpg", "UCBL.jpg", "HTML_Nuke.html", "style_Nuke.css", "tableline.html", "Reconstruction.java", "Settings.txt");

//Create the installation folder if required
if(File.exists(PathOutput)==0){
File.makeDirectory(getDirectory("macros")+File.separator+"NUKE-BREAK");
}

//Installation of all files of the listing
for(i=0; i<lengthOf(Listing); i++){
	if(File.exists(PathFolderInput+Listing[i])==0){
		DisplayInfo("", myProgram,
				""+Listing[i]+" file is missing");
		Errors = Errors + 1;
	}else{
		if(Listing[i]!="Settings.txt"){
			Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
		}else{
			if(File.exists(PathOutput+"Settings.txt")==1){
				DisplayInfo("", myProgram,
				"Your current settings have been preserved!");
			}else{
				Transfer=File.copy(PathFolderInput+"Settings.txt", PathOutput+"Settings.txt");
			}

	}
}

}

//Create the shortcut in IJ macro menu for the first installation
PCommandLine = PathFolderInput+ "Startup_CL.txt";
SUM = File.openAsString(PathSUM);
pos =lastIndexOf(SUM, "//End_Nuke-Break");
if(pos == -1){
	SUM = SUM + "\n\n" + File.openAsString(PCommandLine);
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}

//The program prompts the user of the success or failure of the installation.
if(Errors == 0){
DisplayInfo("", myProgram,
		"Installation has been performed sucessfully!<br>Restart your ImageJ program.");
} else {
DisplayInfo("", myProgram,
		"Files were missing!<br>Installation is incomplete.");
}


/*
================================================================================
*/

function DisplayInfo(Titre, NomProg, Message){
    showMessage(Titre, "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>" + NomProg + " INSTALLATION</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            		+"<p>" + Message + "</p>"
			);
}//END DisplayInfo

/*
================================================================================
*/

function Welcome(NomProg, myTag, myCommit){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>" + NomProg + " INSTALLATION</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            		+"Research Ingeneer,PHD<br>"
            		+"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			+"<p><font color=rgb(0,0,0)>This program will install the" + NomProg + " macro.<br>
            		+"Shortcut will be added in the Plugins/Macros menu.</p>"
			);
}//END WELCOME



}
