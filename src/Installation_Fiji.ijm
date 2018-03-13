macro "Installation_2016-09-23"{

version = "1.0a 2016/09/23";


//IJ version verification and close the macro's window
//selectWindow("Installation.ijm");			
//run("Close");
requires("1.49g");

//Initialisation of the error counter
Errors=0;

//GUI Message
Dialog.create("Installation wizard for the NUKE-BREAK macro");
Dialog.addMessage("Version\n" + version);
Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
Dialog.addMessage("This program will install the NUKE-BREAK macro.\nShortcuts will be added in the Plugins/Macros menu.");
Dialog.show();

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
	waitForUser(""+Listing[i]+" file is missing");
	Errors = Errors + 1;
	}else{
		if(Listing[i]!="Settings.txt"){
			Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
		}else{
			if(File.exists(PathOutput+"Settings.txt")==1){
				waitForUser("Your current settings have been preserved!");
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
waitForUser("Installation has been performed sucessfully!\nRestart your ImageJ program.");
} else {
waitForUser("Files were missing!\nInstallation is incomplete.");
}

}
