package net.sf.recipetools.cb2cb;

import net.sf.recipetools.cb2cb.Cb2cbGui
import net.sf.recipetools.javarecipes.fileprocessor.BinaryInputProcessor
import net.sf.recipetools.javarecipes.fileprocessor.FileProcessor
import net.sf.recipetools.javarecipes.fileprocessor.InputProcessor
import net.sf.recipetools.javarecipes.fileprocessor.OutputProcessor
import net.sf.recipetools.javarecipes.format.FormatterFactory
import net.sf.recipetools.javarecipes.format.RecipeFormatter
import net.sf.recipetools.javarecipes.format.TemplateWriter
import net.sf.recipetools.javarecipes.model.Configuration
import net.sf.recipetools.javarecipes.model.Recipe
import net.sf.recipetools.javarecipes.model.RecipeIngredient

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.commons.cli.PosixParser
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.spi.RootLogger

/**
 * @author Frank
 * 
 */
public class Cb2cb {
	static Logger log = org.apache.log4j.LogManager.getLogger(Cb2cb.class);
			
	boolean debug = false;

	void run(String[] args) throws FileNotFoundException, ParseException {
        if (args.length == 0) {
            // pop up the GUI
            ArrayList files = new Cb2cbGui().run()
            
            if (files == null) {
                return;
            } else {
                args = files.toArray()
            }
        }
		println "args="+args
		
        Options options = new Options();
        options.addOption("m", false, "merge all input files")
        options.addOption("d", false, "log more debug info")
        options.addOption("n", true, "max. number of recipes per output file")
        options.addOption("i", true,
                "input format:\n\tcrb: BigOven\n\tdvo: Cook'n Pro\n\tfdx: Living Cookbook\n\tfdxz: Living Cookbook (zip archive)\n\tgcf: Now Youre Cooking - binary format\n\tjson: RecipeFox JSON formatn\tmmf: mealmaster\n\tmc2: Mastercook binary cookbook\n\tmxp: Mastercook Export format\n\tmx2: Mastercook xml (default))\n\tmz2: Mastercook archive\n\tnyc: Now You are cooking text format")
        options.addOption("o", true,
                "output format:\n\tmmf: mealmaster\n\tmxp: Mastercook Export format\n\tmx2: Mastercook xml (default))\n\tmz2: Mastercook archive\n\tfdx: Living Cookbook\n\tjson: RecipeFox JSON format\n\t<template name>")
        CommandLineParser parser = new PosixParser()
        CommandLine cmd = parser.parse(options, args)
        
        // get leftover args
        args = cmd.getArgs()
        if (args.length == 0 || args.length > 2) {
            throw new RuntimeException('''
Invalid number of arguments:
\t-i <auto|crb|dvo|fdx|fdxz|gcf|json|mgourmet|mmf|mx2|mxp|mc2|mz2|nyc|rcp|rpw|txt> : input format\n
\t-o <fdx|json|mgourmet|mmf|mx2|mxp|mz2|txt> : output format\n
<input file>
<output file> (optional)
''')
        }
     
		println "reading config file"   
        // read the configuration file
        Configuration.loadFromFile('cb2cb.ini');
        
        // read the configurations
		println "reading config items"   
        def props = loadConfig()
        
        // get the default formats and output file from the config file
        String inputFile = args[0]
		debug = cmd.hasOption('d')
        String inputFormat = cmd.hasOption('i') ? cmd.getOptionValue('i') : props.getProperty('INPUTFORMAT', 'TXT')
        inputFormat = inputFormat.toLowerCase()
        String outputFormat = cmd.hasOption('o') ? cmd.getOptionValue('o') : props.getProperty('OUTPUTFORMAT', 'MX2')
        outputFormat = outputFormat.toLowerCase()
        String outputFile = args.length>1 ? args[1] : props.getProperty('OUTPUTFILE', null)
        if (outputFile == null) {
            outputFile = inputFile
            outputFile = outputFile.replaceAll(/\.\w+?$/, '.'+outputFormat)
        }
                
        // load the configuration with "safe" comma structures.
		println "loading commas.txt"
        RecipeIngredient.loadSafeCommaStructuresFromFile('commas.txt')

		try {
			if (new File(args[0]).isFile()) {
				println "converting file"
				convertFile(inputFormat, outputFormat, inputFile, outputFile)
			} else {
				convertDirectory(inputFormat, outputFormat, inputFile, outputFile, cmd.hasOption('m'))
			}
		} catch (Exception e) {
			log.error("ERROR while converting the recipes: "+e.message, e)
		}
    }

	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void main(String[] args)  {
        new Cb2cb().run(args)
	}
	
	static RecipeFormatter createFormatter(String format) {
		RecipeFormatter formatter = null;
		if (format.contains('/') || format.contains('\\')) {
        	def template = new File("templates/$format")
            if (! template.exists()) {
            	throw new RuntimeException("Unknown recipe format: "+format)
            }
            formatter = new TemplateWriter(template)
		} else {
			formatter = FormatterFactory.getFormatter(format);
			if (format.equals("mx2") || format.equals("mz2")) {
				formatter.setTheMasterCookProgram(Configuration.getStringProperty('MASTERCOOK_PROGRAM'))
			}
		}
 
		return formatter
	}

	def setLogFile(logfile) {
		// delete the file
		new File(logfile).delete()
		// set the file appender
		FileAppender fa = new FileAppender();
		fa.setName("file");
		fa.setFile(logfile);
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setAppend(true);
		fa.activateOptions();
		LogManager.getRootLogger().addAppender(fa)
		Level l = debug ? Level.DEBUG : Level.INFO
		fa.setThreshold(l)
		((RootLogger)LogManager.getRootLogger()).setLevel(l)
	}
	
	    
    void convertFile(String inputFormat, String outputFormat, String inputFile, String outputFile) {
        // set the logger filename
        String logfile = inputFile
        logfile = logfile.replaceAll(/\.\w+?$/, '.log')
		setLogFile(logfile)
        
        String inputParent = new File(inputFile).getParent()
        if (inputParent == null) inputParent = '.'
        
        log.info("Converting the file: $inputFile")
		log.debug("inputFormat: $inputFormat, outputFormat: $outputFormat, inputFile: $inputFile, outputFile: $outputFile")
        
		// get the input reader
		RecipeFormatter inputReader = createFormatter(inputFormat)
		if (inputReader.metaClass.respondsTo(inputReader, 'setImageDir')) {
			inputReader.setImageDir(inputParent)
		}
        
        // read all the recipes.
        List<Recipe> recipes = inputReader.read(new File(inputFile))
		log.debug("read: "+recipes.size());
        
        // normalize the recipes
        for (Recipe r : recipes) {
            r.normalize();
        }
		log.debug("normalized the recipes");
		
        // output format
        RecipeFormatter outputFormatter = createFormatter(outputFormat)
		log.debug("got output formatter");
        if (outputFormat.contains("\\")) { // template ?
			def writer = (TemplateWriter) outputFormatter 
            writer.outputFilename = new File(outputFile).absoluteFile.name 
            writer.outputDirectory = new File(outputFile).absoluteFile.parent 
        }
        
        // write the output file
		if (outputFormatter.metaClass.respondsTo(outputFormatter, 'setImageDir')) {
			outputFormatter.setImageDir(inputParent)
		}
		log.debug("start writing");
		outputFormatter.startFile(new File(outputFile));
        outputFormatter.write(recipes)
        outputFormatter.endFile()
        
        log.info("Converted ${recipes.size()} recipes.")
    }
    
    void convertDirectory(String inputFormat, String outputFormat, String inputDir, String outputDir, boolean merge) {
        // set the logger filename
        String logfile = "$outputDir\\cb2cb.log"
		setLogFile(logfile)
		
        // get the input reader
        String inputParent = new File(inputDir).getParent()
        if (inputParent == null) inputParent = '.'
        RecipeFormatter inputReader = createFormatter(inputFormat)
		if (inputReader.metaClass.respondsTo(inputReader, 'setImageDir')) {
			inputReader.setImageDir(inputParent)
		}

        // output format
        RecipeFormatter outputFormatter = createFormatter(outputFormat)
        if (outputFormat.contains("\\")) { // template ?
			def writer = (TemplateWriter) outputFormatter 
            writer.outputFilename = new File(outputDir).absoluteFile.name 
            writer.outputDirectory = new File(outputDir).absoluteFile.parent 
        }
        
        def mergedRecipes = []
		
		InputProcessor ip = new BinaryInputProcessor() {
			public List<Recipe> read(File f) {
				return inputReader.read(f)
			}
		}
        
		OutputProcessor op = new OutputProcessor() {
			public void write(List<Recipe> recipes) {
				log.info("FOUND: ${recipes.size()}")
				// normalize the recipes
				for (Recipe r : recipes) {
					r.normalize()
                    if (merge) mergedRecipes << r
				}
                if (! merge) {
                	outputFormatter.write(recipes)
                }
			}
			public void endFile() {
				if (merge) return
				outputFormatter.endFile()
			}
			public void startFile(String name) {
				startFile(new File(name));
			}
			public void startFile(File f) {
				def name = f.getAbsolutePath()
				if (! name.toLowerCase().endsWith(inputFormat)) return
				log.info("Opening: $name")
                if (merge) return
				String newFilename = name 
				newFilename = newFilename.replaceAll('(?i)\\.'+inputFormat+'$', ".$outputFormat")
				outputFormatter.startFile(new File(newFilename))
			}
		}
		
		FileProcessor fileProcessor = new FileProcessor(ip, op)
		fileProcessor.setNamePatternInclude(~"(?i)\\.${inputFormat}\$|\\.zip\$")
		fileProcessor.process(inputDir)
		if (merge) {
			outputFormatter.startFile(new File("$outputDir\\merged.$outputFormat"))
            List<Recipe> all = mergedRecipes
			outputFormatter.write(all)
			outputFormatter.endFile()
        }
    }    
    
    Properties loadConfig() {
        // load the default input/output file
        def props = new Properties();
        new File('cb2cb.ini').withInputStream { is ->
          	props.load(is)
        }
        return props
    }
}
