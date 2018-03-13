macro "createSettings"{

Nom = "New";
Extension = ".lsm";
Type = "Mitose"
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


