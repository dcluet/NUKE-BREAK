macro "openSettings"{

T = File.openAsString(getDirectory("macros")+File.separator()+"NUKE-BREAK"+File.separator()+"Settings.txt");
TL = split(T, "\n");

for (i=0; i<lengthOf(TL); i++){
	l= split(TL[i], "\t");
	for (j=0; j<lengthOf(l); j++){
		print(l[j]);

		}
	}
}
