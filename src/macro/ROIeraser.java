macro "ROIeraser"{

version = "1.0a 2016/02/12";

nROI= roiManager("count");

if(nROI >0){
	for(i=0; i<nROI; i++){
	roiManager("Select", 0);
	roiManager("Delete");
	}
}

}
