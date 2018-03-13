setForegroundColor (255,255,255);

seuil = 10;
for (n =0; n< roiManager("count"); n++){
roiManager("Select", n);
Erase=0;

List.setMeasurements;
Xr = List.getValue("X");
Yr = List.getValue("Y");
Ar = List.getValue("Area");


	for(N=n+1; N<roiManager("count"); N++){
		roiManager("Select", N);
		List.setMeasurements;
		X = List.getValue("X");
		Y = List.getValue("Y");
		A = List.getValue("Area");
		
		d = sqrt( (X-Xr)*(X-Xr) + (Y-Yr)*(Y-Yr) );			
		if (d<seuil){
			if(A>Ar){
			Xr = X;
			Yr = Y;
			Ar = A;
			Erase = 1;			
			}
			if(A<Ar){
			roiManager("Select", N);
			run("Fill", "slice");
			roiManager("Delete");
			N = N -1;			
			}


		}

	}
if(Erase==1){
roiManager("Select", n);
run("Fill", "slice");
roiManager("Delete");
n=n-1;
N=N-1;
}	

}


