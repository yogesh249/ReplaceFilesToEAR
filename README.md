# ReplaceFilesToEAR
This can be used to replace the .class, .js, .jsp into the EAR file


#How to open this project in Eclipse/Spring tool suite
1. Open STS/Eclipse
2. File -> Import -> Git -> Projects from Git
3. Select Clone URI
4. Provide this URL
5. https://github.com/yogesh249/ReplaceFilesToEAR.git
6. No need to give any username/passsword as this repository is public
7. Next Next Next, select main branch and click Finish

# How to run the application
1. Start the java application
2. Provide the path to the EAR fil
3. Provide the path to the Excel file (CSV)
4. CSV should have the following columns. Sample is kept at the root directory
5. SourceFolder,FileLocation,DestinationJar, Merging Required
6. The file location should be the location where the class file is to be kept inside the war/jar file.
7. Source folder is the location on your file system from where the .class/.js/.jsp will be picked
8. DestinationJar is the name of the .war/.jar inside the .ear file.



