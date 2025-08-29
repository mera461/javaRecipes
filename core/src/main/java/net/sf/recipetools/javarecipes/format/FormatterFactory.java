/**
 * 
 */
package net.sf.recipetools.javarecipes.format;

import net.sf.recipetools.javarecipes.format.MasterCookBinary.Mc2Reader;
import net.sf.recipetools.javarecipes.format.cookenpro.CookenProBinaryReader;
import net.sf.recipetools.javarecipes.format.cookenpro.CookenProV8BinaryReader;
import net.sf.recipetools.javarecipes.format.cookenpro.CookenProV9BinaryReader;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;

/**
 * @author Frank
 *
 */
public class FormatterFactory {
	
	public FormatterFactory() {
	}
	
	public static RecipeFormatter getPrivilegedFormatter(String name) {
		return new PriviledgedFormatter(getFormatter(name));
	}
	
	public static RecipeFormatter getFormatter(String name) {
		switch (name.toLowerCase()) {
		case "auto":
			return new AutoRecognize();
		case "crb":
			return new BigOvenBinaryReader();
		case "dvo":
			return new CookenProBinaryReader();
		case "dvo8":
			return new CookenProV8BinaryReader();
		case "dvo9":
			return new CookenProV9BinaryReader();
		case "fdx":
			return new LivingCookbookFDX();
        case "fdxz":
            return new LivingCookbookArchive();
		case "gcf":
			return new NycBinaryReader();
		case "json":
			return new JsonFormatter();
		case "mgourmet":
			return new MacGourmetXml();
		case "mm":
		case "mmf":
			return new MealMaster();
		case "mc2":
			return new Mc2Reader();
		case "mx2":
			return new MasterCookXml();
		case "mxp":
			return new MasterCookExport();
		case "mz2":
			return new MasterCookArchive();
		case "nyc":
			return new NycExport();
		case "paprika":
		case "paprikarecipes":
			return new PaprikaBinaryReader();
		case "rc":
		case "txt":
			return new McTagIt();
		case "rcp":
			return new AdvantageCooking();
		case "rpw":
			return new RecipeProcessor2000Format();
		case "rezkonv":
			return new RezKonvExport();
		default:
			throw new RecipeFoxException("Unknown formatter: name="+name);
		}
	}

}
