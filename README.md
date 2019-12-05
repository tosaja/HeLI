# HeLI
Language identifier based on HeLI, a Word-Based Backoff Method for Language Identification.

If you are using the identifier on scientific work, please refer to the following article:

@InProceedings{jauhiainen2016,
  author    = {Jauhiainen, Tommi and Lindén, Krister and Jauhiainen, Heidi},
  title     = {HeLI, a Word-Based Backoff Method for Language Identification},
  booktitle = {Proceedings of the 3rd Workshop on Language Technology for Closely Related Languages, Varieties and Dialects (VarDial)},
  address   = {Osaka, Japan},  
  year      = {2016}
}

The HeLI identifier uses the Google guava library. You have to download it from: "https://github.com/google/guava" and add it to your classpath. The identifier has been tested only in a linux/unix environment.

You can also download the guava from maven.org: https://repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar

Place the training files in a "Training" folder, each with a file ending ".train". The filename before ".train" is used as the language identification code. Create an empty folder named "Models".

We read the file languagelist which includes list of the languages to be included in the repertoire of the language identifier. You can use `ls -la Models/ | egrep 'Model' | gawk '{print $9}' | sed 's/\..*//' | sort | uniq > languagelist` to include all the languages which have models in the Models directory.

Run the "createmodels" program. If you have large training files this might take a long time. I use a parallelized version so that training files are processed at the same time (if you need this you have to modify the program, or you could ask me to do it).

Place the text file to be identified in a "Test" folder under the name "test.txt". Run the "HeLI" program. It will read the "test.txt" file line by line and print the same lines plus tab and the language identification code.
