# NICU Data Explorer

## User Guide To NICU_DATA_EXPLORER

#### Version 1.0

#### Description: This project is an integrated data exploration tool for Patient Monitoring in NICU study.

### Installation


1. Download and Install Java SE Development Kit 8u181 from this [link](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
, Pick The Version Meant For Your Computer

2. Delete all other versions of java (jre and jdk) above version 8, such as 9,10 or 11. You can do this from control panel-> Uninstall Program

3. Download the jar release from github. (repository name is amente/nicu_data_explorer)



### Features: Can load up Video streams, Pressure Sensitive Mat (PSM) Data and an Annotation log

### Instructions On How To Use This Application

1. Run the jar file.

2. Upon Running this program, you will be greeted by a small window with a dropdown containing three choices, Video, PSM, Annotations. Pick whichever type of file you would like to load and press load. You can then find
the file on your computer and select it.

3. Once you have loaded in your files, please refer to the specific guide on how to use the sliders and buttons corresponding to each data stream.

4. The scaling mechanic allows for easy viewing and editing of small snippets of the session, specifically the annotations. Simply press zoom in or zoom out to change the factor you would like the annotation to scale by (Most editing can be done with scaling factor 1).Now any annotations that are double clicked from the annotation log will be scaled in on the annotation slider. The time slider that keeps track of the main timeline of the session remains the same.

![image not found](https://github.com/amente/nicu_data_explorer/blob/master/Documentation/Main%20Menu.jpg "Main Menu")

### Video/PSM

Play - Plays the video from wherever the current position of the timeslider is. If the main timeline is at the end of the annotatation, it will either loop or stop.

Loop - sets the main timeline to loop when it reaches the end of the annotation.

Time Slider - Represents the timeline of the video.

Annotation Range Slider - Represents the range of the annotation. When the range slider is not scaled, it shows the annotation respective to the timeline, however when the range slider is scaled the annotation duration is centered and widened according to the scale factor. The larger the scale factor, the wider the possible range of the annotation.

The three labels simply show the time that the slider thumbs represent. This makes adding annotations based of hand written notes easier and is just an important feature in general.

![image not found](https://github.com/amente/nicu_data_explorer/blob/master/Documentation/Video%20or%20PSM.jpg "Video/PSM")

### Annotation Log

The annotation log keeps track of all the annotations found in the session, by double clicking an annotation, the annotation will set itself to each slider.

Play All - plays all instances at the same time for synchronized data viewing

Save Updates - You can update the changes you have made by selecting save updates. The selected video/psm window is the candidate that the annotation log gets the new value from.

Add Annotation - You can add an annotation and type in the event and category. The times can be added by using the update changes button.

Delete Annotation - You can delete an annotation by selecting it and pressing the delete annotation button.

Save File - Saves the edited file after you provide a file name and location.

![image not found](https://github.com/amente/nicu_data_explorer/blob/master/Documentation/Annotation%20Log.jpg "Annotation Log")

## Development Requirements

Install the latest version of [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/#section=windows)

## Building the Application

Import the project "nicu_data_explorer" in Intellij. For testing during development run the file "Main.java".
 **IMPORTANT** : Make sure to check the box `Include dependencies with "Provided" scope` under `Run --> Edit Configurations`
