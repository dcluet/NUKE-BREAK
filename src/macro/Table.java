macro "Table"{

Args = getArgument();
Arguments = split(Args, "\t");
Path = Arguments[0];
Option = Arguments[1];

if (endsWith(Path, "_nuclei.txt")){
	FNuclei(Path);
}

if (endsWith(Path, "_foci.txt")){
	FFoci(Path);
}

function FNuclei(pathFile){
N = "";
P = "";
F = "";
D = "";
S = "";
nfoci = 0;

if (Option == "Mitose"){
LimitFrancesca = 90;
}else{
LimitFrancesca = 150;
}

//Open the file
Text = File.openAsString(pathFile);

//Create a recipient txt file
temp =replace(pathFile, ".txt", "");
R = File.open(temp+"_4Excel.txt");
File.close(R);

Header = "Nuclei number" + "\t" + "Distance from tip (µm)" + "\t" + "Surface (µm2)" + "\t" + "Density" + "\t"+ "Number of foci";
File.append(Header, temp+"_4Excel.txt");


//Create a table of lines
lines = split(Text, "\n");

//Extract distance and number of foci

for(l =0; l<lengthOf(lines); l++){
	colonnes = split(lines[l], "\t");
if(lengthOf(colonnes) >0){
	if ((lengthOf(colonnes) == 6) && (colonnes[0] != "Nuclei number") ){

		if(parseFloat(colonnes[1])<=LimitFrancesca ){
 		N = N +"\t" + colonnes[0];
		P = P +"\t" + colonnes[1];
		F = F +"\t" + colonnes[5];
		D = D +"\t" + colonnes[4];
		S = S +"\t" + colonnes[2];	
		}	
	}
}

}

//Create the arrays of position and number of foci
Noyaux = split(N, "\t");
Positions = split(P, "\t");
Foci = split(F, "\t");
Density = split(D, "\t");
Surface = split(S, "\t");

//Converte string to number
for (i =0; i< lengthOf(Positions); i++){
	Noyaux[i] = parseFloat(Noyaux[i]);
	Positions[i] = parseFloat(Positions[i]);	
	Foci[i] = parseFloat(Foci[i]);
	Density[i] = parseFloat(Density[i]);
	Surface[i] = parseFloat(Surface[i]);
	//Calculate total number of Foci
	nfoci = nfoci + Foci[i];
}

//Obtain the index of the nuclei in increasing distance from the tip
RPositions = Array.rankPositions(Positions);

//Create the reorganized nuclei position and foci number
SortedPositions = Array.copy(Positions);
Array.sort(SortedPositions);
SortedNuclei = newArray(lengthOf(Noyaux));
SortedFoci = newArray(lengthOf(Foci));
SortedDensity = newArray(lengthOf(Density));
SortedSurface = newArray(lengthOf(Surface));


//Loop to sort foci using position ranking
for (index=0; index<lengthOf(RPositions); index++){
	IndexInFoci = RPositions[index]; 
	SortedNuclei[index] = Noyaux[IndexInFoci];
	SortedFoci[index] = Foci[IndexInFoci];
	SortedDensity[index] = Density[IndexInFoci];
	SortedSurface[index] = Surface[IndexInFoci];
}

//Cumulated curve (standardized)
Cumul = newArray(lengthOf(SortedFoci));

Cumul[0] = 100*SortedFoci[0]/nfoci;

for (index=1; index<lengthOf(RPositions); index++){
	Cumul[index] = Cumul[index-1] + (100*SortedFoci[index]/nfoci); 
	
}

for (ligne = 0; ligne<lengthOf(RPositions); ligne++){
	T = ""+ SortedNuclei[ligne] + "\t" + SortedPositions[ligne] + "\t" + SortedSurface[ligne] + "\t" + SortedDensity[ligne] + "\t" + SortedFoci[ligne];
	File.append(T, temp+"_4Excel.txt");
}


Array.getStatistics(SortedFoci, minF, maxF, meanF, stdDevF);
Array.getStatistics(SortedDensity, minD, maxD, meanD, stdDevD);
Array.getStatistics(SortedSurface, minS, maxS, meanS, stdDevS);



//Creation and saving scatter plot
MyPlot(SortedPositions, 
	"Distance um", 
	0, 
	LimitFrancesca, 
	SortedFoci, 
	"Number of Foci", 
	-1, 
	10, 
	"red", 
	"circle", 
	"Scatter "+nfoci+" foci for "+ lengthOf(SortedPositions) + " nuclei" , 
	pathFile+"_Scatter_Distance_Foci.jpg");

//Creation and saving scatter plot
MyPlot(SortedPositions, 
	"Distance um", 
	0, 
	LimitFrancesca, 
	SortedDensity, 
	"Density", 
	0, 
	maxD, 
	"blue", 
	"circle", 
	"Scatter of "+ lengthOf(SortedPositions) + " nuclei" , 
	pathFile+"_Scatter_Distance_Density.jpg");

//Creation and saving scatter plot
MyPlot(SortedDensity, 
	"Density", 
	0, 
	maxD, 
	SortedFoci, 
	"Foci", 
	-1, 
	10, 
	"green", 
	"circle", 
	"Scatter of "+ lengthOf(SortedPositions) + " nuclei" , 
	pathFile+"_Scatter_Density_Foci.jpg");

//Creation and saving scatter plot
MyPlot(SortedSurface, 
	"Surface", 
	0, 
	maxS, 
	SortedFoci, 
	"Foci", 
	-1, 
	10, 
	"orange", 
	"circle", 
	"Scatter of "+ lengthOf(SortedPositions) + " nuclei", 
	pathFile+"_Scatter_Surface_Foci.jpg");

//Creation and saving scatter plot
MyPlot(SortedPositions, 
	"Distance um", 
	0, 
	LimitFrancesca,
	SortedSurface, 
	"Surface", 
	0, 
	maxS, 
	"magenta", 
	"circle", 
	"Scatter of "+ lengthOf(SortedPositions) + " nuclei", 
	pathFile+"_Scatter_Distance_Surface.jpg");

//Creation and saving Cumulated plot
MyPlot(SortedPositions, 
	"Distance um", 
	0, 
	LimitFrancesca,
	Cumul, 
	"Number of foci", 
	0, 
	100, 
	"red", 
	"line", 
	"Cumul "+nfoci+" foci for "+ lengthOf(SortedPositions) + " nuclei", 
	pathFile+"_Cumulative_Foci.jpg");



R2 = File.rename(temp+"_4Excel.txt", temp+".xls"); 
}

















function FFoci(pathFile){
P1 = "";// distance par rapport à l'extrémité de la gonade
P2 = "";

//F1 = "";

S1 = "";
S2 = "";
N1 = "";
N2 = "";
nfoci = 0;
if (Option == "Mitose"){
LimitFrancesca = 90;
}else{
LimitFrancesca = 150;
}

//Open the file
Text = File.openAsString(pathFile);


//Create a recipient txt file
temp =replace(pathFile, ".txt", "");
R = File.open(temp+"_4Excel.txt");
File.close(R);

Header = "Nuclei number" + "\t" + "Distance from tip (µm)" + "\t" + "Surface (µm2)" ; 
File.append(Header, temp+"_4Excel.txt");




//Create a table of lines
lines = split(Text, "\n");


//Extract distance and foci size

for(l =0; l<lengthOf(lines); l++){
	colonnes = split(lines[l], "\t");
	if(lengthOf(colonnes) >0){
	if ((lengthOf(colonnes) >=2) && (colonnes[0] != "Nuclei number")){ 
		if(parseFloat(colonnes[1])<=LimitFrancesca ){
		if (lengthOf(colonnes)==2){
			
			P1 = P1+"\t"+colonnes[1];
			S1= S1 +"\t"+ 0;
			N1= N1 + "\t" + colonnes[0];
			}else{
				for( c=2; c<lengthOf(colonnes); c++){
					P1 = P1+"\t"+colonnes[1];
					P2 = P2+"\t"+colonnes[1];
			
					S1= S1+"\t" +colonnes[c];
					S2= S2+"\t" +colonnes[c];
					N1= N1 + "\t" + colonnes[0];
					N2= N2 + "\t" + colonnes[0];
				
					}
			}	
				}
			}
	}

}
		
		
//Create the arrays of distances and foci size

Positions1 = split(P1, "\t");
Positions2 = split(P2, "\t");


Surface1 = split(S1, "\t");
Surface2 = split(S2, "\t");

Nuclei1 = split(N1, "\t");
Nuclei2 = split(N2, "\t");

//Converte string to number
for (i =0; i< lengthOf(Positions1); i++){
	
	Positions1[i] = parseFloat(Positions1[i]);	
	Surface1[i] = parseFloat(Surface1[i]);
	Nuclei1[i]=parseFloat(Nuclei1[i]);
}

for (i =0; i< lengthOf(Positions2); i++){
		
	Positions2[i] = parseFloat(Positions2[i]);
	Surface2[i] = parseFloat(Surface2[i]);
	Nuclei2[i]=parseFloat(Nuclei2[i]);
}


//Obtain the index of the size of foci in increasing distance from the tip
RPositions1 = Array.rankPositions(Positions1);
RPositions2 = Array.rankPositions(Positions2);

//Create the reorganized distances and foci size
SortedPositions1 = Array.copy(Positions1);
SortedPositions2 = Array.copy(Positions2);
Array.sort(SortedPositions1);
Array.sort(SortedPositions2);
SortedSurface1 = newArray(lengthOf(Surface1));
SortedSurface2 = newArray(lengthOf(Surface2));
SortedNuclei1 = newArray(lengthOf(Nuclei1));
SortedNuclei2 = newArray(lengthOf(Nuclei2));


//Loop to sort size using position ranking
for (index=0; index<lengthOf(RPositions1); index++){
	IndexInSurface = RPositions1[index]; 
	SortedSurface1[index] = Surface1[IndexInSurface];
	SortedNuclei1[index] = Nuclei1[IndexInSurface];
}

for (index=0; index<lengthOf(RPositions2); index++){
	IndexInSurface = RPositions2[index]; 
	SortedSurface2[index] = Surface2[IndexInSurface];
	SortedNuclei2[index] = Nuclei2[IndexInSurface];
}


Array.getStatistics(SortedPositions1, minP1, maxP1, meanP1, stdDevP1);
Array.getStatistics(SortedPositions2, minP2, maxP2, meanP2, stdDevP2);
Array.getStatistics(SortedSurface1, minS1, maxS1, meanS1, stdDevS1);
Array.getStatistics(SortedSurface2, minS2, maxS2, meanS2, stdDevS2);
Array.getStatistics(SortedNuclei1, minN1, maxN1, meanN1, stdDevN1);
Array.getStatistics(SortedNuclei2, minN2, maxN2, meanN2, stdDevN2);

for (ligne = 0; ligne<lengthOf(RPositions2); ligne++){
	T = ""+ SortedNuclei2[ligne] + "\t" + SortedPositions2[ligne] + "\t" + SortedSurface2[ligne] ;
	File.append(T, temp+"_4Excel.txt");
}


//Creation and saving scatter plot
MyPlot(SortedPositions1, 
	"Distance um", 
	-1, 
	LimitFrancesca,
	SortedSurface1, 
	"foci size", 
	0, 
	maxS1, 
	"red", 
	"circle", 
	"Scatter of foci size for "+ lengthOf(SortedPositions1) + " nuclei", 
	pathFile+"_Scatter_Distance_Foci-Size_All.jpg");



	//Creation and saving scatter plot
MyPlot(SortedPositions2, 
	"Distance um", 
	-1, 
	LimitFrancesca,
	SortedSurface2, 
	"foci size", 
	0, 
	maxS2, 
	"red", 
	"circle", 
	"Scatter of foci size for "+ lengthOf(SortedPositions2) + " nuclei", 
	pathFile+"_Scatter_Distance_Foci-Size.jpg");




R2 = File.rename(temp+"_4Excel.txt", temp+".xls");

}

//Creation and saving a plot
function MyPlot(Xarray, Xlabel, Xmin, Xmax, Yarray, Ylabel, Ymin, Ymax, color,shape, text, pathOutput){

Plot.create("Graph", Xlabel, Ylabel);
Plot.setLimits(Xmin, Xmax, Ymin, Ymax);
Plot.setColor(color, color);
Plot.add(shape, Xarray, Yarray);
Plot.addText(text, 0, 0);
Plot.show;

im = getTitle();
H=getHeight();
W=getWidth();
run("Copy");
newImage("Plot","RGB",W,H,1);
run("Paste");
selectWindow(im);
run("Close");
selectWindow("Plot");
saveAs("Jpeg", pathOutput);
close();

}



}
