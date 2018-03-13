macro "reconstruction stack" {

/*
Stable version 2016-10-03

!!Some rounding of the resolution values??

*/



	Pathlsm = getArgument();

	Path = replace(Pathlsm, ".lsm", ".tif");

	PathFolder = Path + "_files" + File.separator;

	//Opening lsm and retrieve original resolution
	open(Pathlsm);
	T = getTitle();
	Nom = File.nameWithoutExtension();

	H = getHeight();
	W = getWidth();
	getPixelSize(unit, pixelWidth, pixelHeight);

	getVoxelSize(width, height, depth, unit);

	//Find which canal

	LUT0 ="";
	LUT1 ="";

	//Separating the channels
	run("Split Channels");

	//Renaming the channels
	selectWindow("C1-"+T);
	getLut(reds, greens, blues);
	if (reds[255]>blues[255]){
		LUT0 = "Red";
		LUT1 = "Blue";
	}else{
		LUT0 = "Blue";
		LUT1 = "Red";
	}





	close("C1-"+T);
	close("C2-"+T);	

 
	F0 = File.open(Path+"_C0.txt");
	File.close(F0);

	F1 = File.open(Path+"_C1.txt");
	File.close(F1);

	listFiles(PathFolder, "c0x0", Path+"_C0.txt", ".tif");
	listFiles(PathFolder, "c1x0", Path+"_C1.txt", ".tif");

	L0 = File.openAsString(Path+"_C0.txt");
	L1 = File.openAsString(Path+"_C1.txt");

	T0 = split(L0, "\n");
	T1 = split(L1, "\n");

	T0b = newArray(lengthOf(T0));
	T1b = newArray(lengthOf(T1));

	reclasse(T0, T0b);
	reclasse(T1, T1b);
	
	n = lengthOf(T0);

	newImage("C0", "RGB black", W, H, n);

	for(c0=0; c0<lengthOf(T0b); c0++){
		open(T0b[c0]);
		Ti = getTitle();
		makeRectangle(0,0, W,H);
		run("Copy");
		selectWindow("C0");
		setSlice(c0+1);
		makeRectangle(0,0, W,H);
		run("Paste");
		selectWindow(Ti);
		close();
	}

	newImage("C1", "RGB black", W, H, lengthOf(T0));

	for(c0=0; c0<lengthOf(T1b); c0++){
		open(T1b[c0]);
		Ti = getTitle();
		makeRectangle(0,0, W,H);
		run("Copy");
		selectWindow("C1");
		setSlice(c0+1);
		makeRectangle(0,0, W,H);
		run("Paste");
		selectWindow(Ti);
		close();
	}

selectWindow("C0");
run("8-bit");
run(LUT0);
selectWindow("C1");
run("8-bit");
run(LUT1);

run("Merge Channels...", "c1=C0 c3=C1 create keep");
run("Stack to Hyperstack...", "order=xyczt(default) channels=2 slices="+n +" frames=1 display=Color");

run("Properties...", "channels=2 slices="+n +" frames=1 unit="+unit+" pixel_width="+pixelWidth+" pixel_height="+pixelHeight+" voxel_depth="+depth);

saveAs(".tif", Path);
close();

selectWindow("C0");
close();

selectWindow("C1");
close();

function reclasse(T, Tb){

	for (i=0; i<lengthOf(T); i++){
		for (j=0; j<lengthOf(T); j++){
			if (lastIndexOf(T[j], "_z"+i+"c")!=-1){
				Tb[i]=T[j];	
				j = 1000*j;
			}
		} 
	}
	
	/*
	for(k=0; k<lengthOf(Tb); k++){
		print(Tb[k]);
	}
	*/

}

function listFiles(folder, SubS, F, extension) {

	
	list = getFileList(folder);
	for (i=0; i<list.length; i++) {
			
			//print (list[i]));

		
       		if (File.isDirectory(folder+list[i])	){
           	listFiles(""+folder+list[i], Subs, F,extension);
       		}
		
		if (endsWith(list[i], extension)&&(lastIndexOf(list[i], SubS)!=-1)){
		File.append(""+folder+list[i], F);
			
			}
		}
	}

waitForUser("Conversion is over!");

}







