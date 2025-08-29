
/**
 * 
 */
package net.sf.recipetools.javarecipes.format

import groovy.text.SimpleTemplateEngine

import java.io.File
import java.io.LineNumberReader
import java.io.PrintWriter
import java.util.Properties

import net.sf.recipetools.javarecipes.model.Recipe
import net.sf.recipetools.javarecipes.model.RecipeFoxException

/**
 * @author Frank
 *
 */
public class TemplateWriter extends RecipeTextFormatter implements AllFileInOneGo {
    
    File templateFile = null
    Properties templateProperties = new Properties()
    
    public String outputDirectory = "" 
    public String outputFilename = ""
	
	def allRecipes = [] 
    
    int idCounter = 0
    
    public TemplateWriter() {
    }
	
    public TemplateWriter(String template) {
        loadTemplate(template)
    }
    
    public TemplateWriter(File template) {
        loadTemplate(template)
    }
    
    public void loadTemplate(String template) {
    	loadTemplate(new File(template))
    }
            
    public void loadTemplate(File template) {
        if (! template.exists() || ! template.canRead()) {
            throw new RecipeFoxException("Invalid template file (cannot be read): ${template.name}")
        }
        templateProperties = new Properties()
        template.withInputStream { templateProperties.load(it) }
        templateFile = template
    }
    
    public String getDefaultCharacterSet() {
		return "UTF-8";
	}

	public List<Recipe> readRecipes(LineNumberReader input) {
		throw new RecipeFoxException('Reading from a TemplateWriter not supported')
	}
	
	public void writeFileHeader(PrintWriter out) {
        // nothing to write
	}
	
	public void writeFileTail(PrintWriter out) {
        // set RecipeFormatter fields
        writeImages = true
        imageDir = new File(outputDirectory)
        
        if (Boolean.parseBoolean(templateProperties.getProperty('AllInOne', 'true'))) {
            writeAllInOne(out)
        } else {
        	writeIndividualRecipes(out)
        }
	}
    
    void writeAllInOne(PrintWriter out) {
        writeAll(out)
        copySharedFiles()
    }    
	
    void writeIndividualRecipes(PrintWriter out) {
        writeAll(out) // the index file
        writeRecipeFiles()
        copySharedFiles()
    }
    
    void writeAll(PrintWriter out) {
        def f = getPropertyFile('Template')
        
        def engine = new SimpleTemplateEngine()
        def binding = ['allRecipes' : allRecipes]
        use (XmlEscapeCategory, FormatNumberCategory) {
            def result = engine.createTemplate(f).make(binding)
            out.write(result)
        }
    }
    
    void writeRecipeFiles() {
        def f = getPropertyFile('RecipeTemplate')
        for (Recipe recipe in allRecipes) {
            writeSingleRecipe(f, recipe)
        }
    }
    
    File getPropertyFile(String name) {
        def filename = templateProperties.getProperty(name)
        if (! filename) {
            throw new RecipeFoxException("No $name file given in the template definition")
        }
        def f = new File(templateFile.parent, filename)	
        if (! f.exists() || ! f.canRead()) {
            throw new RecipeFoxException("Invalid $name file (cannot be read): $filename")
        }
        return f
    }
    
    void writeSingleRecipe(File template, Recipe recipe) {
        
        // find the name of the prev and next recipe
        def prevLink = idCounter ==0 ? null : "${allRecipes[idCounter-1].titleAsFilename()}-${idCounter-1}.html"
        def nextLink = idCounter >= (allRecipes.size()-1) ? null : "${allRecipes[idCounter+1].titleAsFilename()}-${idCounter+1}.html"
                
        def filename = recipe.titleAsFilename()
        // save the image
        def imageName = saveMainImage(recipe)?.name
        // save the fdx file with the recipe
        String downloadFilename = "$outputDirectory/${filename}-${idCounter}.fdx"
        String downloadName = "${filename}-${idCounter}.fdx"
        File outFile = new File(downloadFilename)
        PrintWriter out = new PrintWriter(new FileWriter(outFile))
        def formatter = new LivingCookbookFDX();
        formatter.writeRecipe(out, recipe)
        out.close()
        
        // generate the html file
        outFile = new File("$outputDirectory/${filename}-${idCounter++}.html")
        out = new PrintWriter(new FileWriter(outFile))
        def engine = new SimpleTemplateEngine()
        def binding = ['recipe' : recipe, 'imageName': imageName, 'prevLink':prevLink, 'nextLink':nextLink, 'downloadName': downloadName, 'index': outputFilename]
        use (XmlEscapeCategory, FormatNumberCategory) {
            def result = engine.createTemplate(template).make(binding)
            out.write(result)
        }
        out.close()
    }
    
    /**
     * Copy all shared files to the output directory 
     */
    void copySharedFiles() {
        def sharedFiles = templateProperties.getProperty('SharedDirectory')
        if (! sharedFiles) return
        File f = new File(templateFile.parent, sharedFiles)
        if (! f.exists() || ! f.isDirectory()) {
        	throw new RecipeFoxException("The SharedDirectory ($sharedFiles) in the template must point to an existing directory.")
        }
        copyDirectory(f, new File(outputDirectory, sharedFiles))
    }
    
    public void copyDirectory(File sourceLocation , File targetLocation) {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                new File(targetLocation, children[i]));
            }
        } else {
            InputStream inp = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = inp.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            inp.close();
            out.close();
        }
    }    
    
    public void writeRecipe(PrintWriter out, Recipe recipe) {
		allRecipes << recipe
	}	
}

class XmlEscapeCategory {
	public static String escapeXml(String self) {
		self ? RecipeTextFormatter.escapeXml(self) : ''
	}
    public static String toHtml(String self) {
        self ? new HtmlStringBuffer(self).toHtmlChars().toString() : ''
    }
    }

class FormatNumberCategory {
	public static String formatNumber(Float self) {
		self ? FormattingUtils.formatNumber(self) : ''
	}
}
