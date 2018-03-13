macro "Table_Analysis"{

version = "1.0a 2016/04/29";

//HISTORY_________________________________________________________________________________________________________
/*
2016/04/29	Creation of the drawing function
		Distance limite as variable
		XLS file
*/



/*
	MAIN PROBLEMS
		NO 2D ARRAYS IN IJ
		NO APPEND FUNCTION
*/

setBatchMode(true);





//Specify file
pathFile = File.openDialog("TXT file to process");

runMacro(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Table.java", pathFile);









}
