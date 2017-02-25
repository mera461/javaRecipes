ECHO OFF
REM #######################################################
REM #
REM # Extract all images from a Mastercook MCX file
REM # Give the MCX filename as argument to the script
REM # 
REM ####################################################################
java -cp cb2cb.jar net.sf.recipetools.javarecipes.bin.ExtractImagesFromMcx %1

