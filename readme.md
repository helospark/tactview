# Tactview

Video editor

## Local build

Get required dependencies

	cd tactview-native
	./build_dependencies.sh

Compile native code

	cd tactview-native
	./build.sh


Compile Tactview Java code

	mvn clean install

Get Javafx-sdk

	http://gluonhq.com/download/javafx-11-0-2-sdk-linux/

Run Tactview

	
	java --module-path=~/SOMEWHERE/javafx-sdk-11.0.2/lib --add-modules=javafx.controls -jar tactview-ui-0.0.1-SNAPSHOT.jar
