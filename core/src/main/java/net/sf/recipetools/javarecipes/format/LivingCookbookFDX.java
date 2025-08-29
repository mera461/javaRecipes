/*
 * Created on 25-10-2004
 *
 */
package net.sf.recipetools.javarecipes.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.recipetools.javarecipes.model.Folder;
import net.sf.recipetools.javarecipes.model.Recipe;
import net.sf.recipetools.javarecipes.model.RecipeFoxException;
import net.sf.recipetools.javarecipes.model.RecipeIngredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Frank
 *
 */
public class LivingCookbookFDX extends RecipeTextFormatter implements AllFileInOneGo {
	private static final Logger log = LoggerFactory.getLogger(LivingCookbookFDX.class);
	
	private XMLReader xr = null;
	private LivingCookbookXmlParser handler = new LivingCookbookXmlParser();
	private int id = 1;

	/**
	 * 
	 */
	public LivingCookbookFDX() {
		super();
		try {
			xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(handler);
			// Turn off validation
			xr.setFeature("http://xml.org/sax/features/validation", false);
			xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
		} catch (SAXException e) {
			log.error("SAX parsing error.", e);
			throw new RecipeFoxException(e);
		}
		id = 1;
	}

	// ---------------------------------------------------------------
	//
	//          reading 
	//
	// ---------------------------------------------------------------
	
	private LineNumberReader lastInput = null;
	
    @Override
	public List<Recipe> readRecipes(LineNumberReader in) {
		// dont use the same stream twice
		if (lastInput!=null && in==lastInput) {
			// Reading same input so stop it.
			return new ArrayList<Recipe>();
		}
		lastInput = in;
		handler.getAllRecipes().clear();
		// set image dir for FDXZ
		handler.setImageDir(getImageDir());
		
		try {
			// test and skip the BOM (Byte-Order-Mark): 
			// http://en.wikipedia.org/wiki/Byte_order_mark
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
			in.mark(10);
			char[] cbuf = new char[1];
			in.read(cbuf);
			if (cbuf[0] == '\ufeff') { // 0xEF 0xBB 0xBF
				// just skip them
			} else {
				in.reset();
			}
			
			xr.parse(new InputSource(in));
		} catch (IOException e) {
			log.error("IO exception.", e);
			throw new RecipeFoxException(e);
		} catch (SAXException e) {
			log.error("SAX parsing error.", e);
			throw new RecipeFoxException(e);
		}
		
		return handler.getAllRecipes();
	}

	// ---------------------------------------------------------------
	//
	//          writing 
	//
	// ---------------------------------------------------------------
	
    @Override
	public String getDefaultCharacterSet() {
		return "UTF-8";
	}

	public void writeCookbooks(PrintWriter out) {
		if (Folder.getAll().size() > 0) {
			out.println("  <Cookbooks>");
			for (Folder i : Folder.getAll()) {
				if (i.isRoot()) {
					out.print("      <Cookbook Name=\""+escapeXml(i.getName())+"\" ID=\""+i.getId()+"\" ");
					if (i.getDescription() != null && i.getDescription().length()>0) out.print("Comments=\""+escapeLinebreaks(escapeXml(i.getDescription()))+"\"");
					out.println("/>");
				}
			}
			out.println("  </Cookbooks>");
		}
	}

	public void writeChapters(PrintWriter out) {
		if (Folder.getAll().size() > 0) {
			out.println("  <CookbookChapters>");
			for (Folder i : Folder.getAll()) {
				if (! i.isRoot()) {
					out.print("      <CookbookChapter Name=\""+escapeXml(i.getName())+
							  "\" ID=\""+i.getId()+
							  "\" CookbookID=\""+i.getRoot().getId()+
							  "\" ParentChapterID=\""+((i.getParent()==null || i.getParent().isRoot()) ? 0 : i.getParent().getId())+"\" ");
					if (i.getDescription() != null && i.getDescription().length()>0) out.print("Comments=\""+escapeLinebreaks(escapeXml(i.getDescription()))+"\"");
					out.println("/>");
				}
			}
			out.println("  </CookbookChapters>");
		}
	}
	
    @Override
	public void writeFileHeader(PrintWriter out) {
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		out.println("<fdx Source=\"Living Cookbook 2.0\" FileVersion=\"1.2\" date=\""+date+"\">");
		writeCookbooks(out);
		writeChapters(out);
		out.println("<Recipes>");
		// start id numbering from 1
		id = 1;
	}

    @Override
	public void writeFileTail(PrintWriter out) {
		out.println("</Recipes>");
		out.println("</fdx>");
	}
	
    @Override
	public void writeRecipe(PrintWriter out, Recipe recipe) {
		if (recipe.getFileSource()!=null && recipe.getFileSource().length()>0) {
			out.println("<!-- Original file: "+recipe.getFileSource()+" -->");
		}
		
		// TODO: ID?, CreateDate is optionally
		out.print("<Recipe ID=\""+(id++)+
				   "\" Name=\""+escapeXml(recipe.getTitle())+
				   "\" Servings=\""+recipe.getServings()+"\"");
		String comments = FormattingUtils.join(recipe.getDescription(), "\n\n", recipe.getNote());
		if (comments!=null && comments.length()>0) {
			out.print(" Comments=\""+escapeLinebreaks(escapeXml(comments))+"\"");
		}
		if (recipe.getAuthor()!=null && recipe.getAuthor().length()>0) {
			out.print(" Author=\""+escapeXml(recipe.getAuthor())+"\"" );
		}
		if (recipe.getPreparationTime()>0) {
			out.print(" PreparationTime=\""+recipe.getPreparationTime()+"\"");
		}
		if (recipe.getTotalTime()>0) {
			out.print(" ReadyInTime=\""+recipe.getTotalTime()+"\"");
		}
		if (recipe.getCookTime()>0) {
			out.print(" CookingTime=\""+recipe.getCookTime()+"\"");
		}
		// TODO: Inactive time?
		if (recipe.getTimes().keySet().contains("INACTIVE")) {
			out.print(" InactiveTime=\""+recipe.getTime("INACTIVE")+"\"");
		}
		if (recipe.getCategories()!=null && recipe.getCategories().size()>0 
			|| recipe.getCuisine()!=null && recipe.getCuisine().length()>0) {
			StringBuilder types = new StringBuilder();
			if (recipe.getCategories()!=null && recipe.getCategories().size()>0) {
				types.append(recipe.getCategoriesAsString().replace(';', ','));
			}
			if (recipe.getCuisine()!=null && recipe.getCuisine().length()>0) {
				if (types.length()>0) types.append(',');
				types.append(recipe.getCuisine());
			}
			out.print(" RecipeTypes=\""+escapeXml(types.toString())+"\"");
		}
		if (recipe.getSource()!=null && recipe.getSource().length()>0) {
			out.print(" Source=\""+escapeXml(recipe.getSource())+"\"");
		}
		if (recipe.getCopyright()!=null && recipe.getCopyright().length()>0) {
			out.print(" Copyright=\""+escapeXml(recipe.getCopyright())+"\"");
		}
		if (recipe.getUrl()!=null && recipe.getUrl().length()>0) {
			out.print(" WebPage=\""+escapeXml(recipe.getUrl())+"\"");
		}
		// Yield
		if (recipe.getYield()!=null && recipe.getYield().length()>0) {
			out.print(" Yield=\""+escapeXml(recipe.getYield())+"\"");
		}
		//Yield="8  3/4 cup servings"
		
		// Chapter and cookbook
		if (recipe.getFolder()!=null) {
			long chapterId = recipe.getFolder().getId();
			long bookId = recipe.getFolder().getRoot().getId();
			out.print(" CookbookID=\""+bookId+"\" CookbookChapterID=\""+chapterId+"\"");
		}
		out.println('>');
		
		// ingredients
		if (recipe.getIngredients().size() > 0) {
			out.println("<RecipeIngredients>");
		}
		for (RecipeIngredient ingr : recipe.getIngredients()) {
			out.print("<RecipeIngredient ");
			if (ingr.hasAmount()) {
				out.print("Quantity=\""+FormattingUtils.formatNumber(ingr.getAmount())+"\" ");
			} else {
				out.print("Quantity=\"\" ");
			}
			if (ingr.hasUnit()) {
				String unit = ingr.getPluralisedUnitName(); 
				out.print("Unit=\""+escapeXml(unit)+"\" ");
			} else {
				out.print("Unit=\"\" ");
			}
			StringBuilder txt = new StringBuilder();
			if (ingr.hasIngredient()) txt.append(ingr.getIngredient().getName());
			if (ingr.hasProcessing()) txt.append(" -- "+ingr.getProcessing());
			if (txt.length() > 0) {
				out.print("Ingredient=\""+escapeXml(txt.toString())+"\" ");
			} else {
				out.print("Ingredient=\"\" ");
			}
			out.print("Heading=\""+(ingr.getType() == RecipeIngredient.TYPE_SUBTITLE ? "Y" : "N")+"\" ");
			out.println("/>");
		}		
		if (recipe.getIngredients().size() > 0) {
			out.println("</RecipeIngredients>");
		}
		
		// directions
		if (recipe.getDirections()!=null && recipe.getDirections().size()>0) {
			out.println("<RecipeProcedures>");
			for (int i=0; i<recipe.getDirections().size(); i++) {
				out.println("<RecipeProcedure Heading=\"N\">");
				out.println("<ProcedureText>"+escapeXml(recipe.getDirections().get(i)));
				out.println("</ProcedureText>");
				if (isWriteImages()
					&& recipe.getDirectionImage(i)!= null) {
					out.print("<ProcedureImage xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"bin.base64\" FileType=\""+recipe.getDirectionImage(i).getImageType()+"\">");
					out.print(recipe.getDirectionImage(i).encodeAsBase64());
					out.println("</ProcedureImage>");
				}
				out.println("</RecipeProcedure>");
			}
			out.println("</RecipeProcedures>");
		}
		
		// tips
		if (recipe.getWine()!=null && recipe.getWine().length()>0
			|| recipe.getServingIdeas()!=null && recipe.getServingIdeas().length()>0
			|| recipe.getTips()!=null && recipe.getTips().size() > 0
			|| recipe.getNutritionalInfo()!=null && recipe.getNutritionalInfo().length() > 0) {
			out.println("<RecipeTips>");
			if (recipe.getWine()!=null && recipe.getWine().length()>0) {
				out.println("<RecipeTip>Wine is "+escapeXml(recipe.getWine()));
				out.println("</RecipeTip>");
			}
			if (recipe.getServingIdeas()!=null && recipe.getServingIdeas().length()>0) {
				out.println("<RecipeTip>Serving idea:");
				out.println("</RecipeTip>");
				for (String str : recipe.getServingIdeas().split("\n\n")) {
					out.println("<RecipeTip>"+escapeXml(str));
					out.println("</RecipeTip>");
				}
			}
			if (recipe.getTips()!=null && recipe.getTips().size()>0) {
				for (String str : recipe.getTips()) {
					out.print("<RecipeTip>"+escapeXml(str)+"</RecipeTip>");
					out.println();
				}
			}
			if (recipe.getNutritionalInfo()!=null && recipe.getNutritionalInfo().length()>0) {
				out.println("<RecipeTip>Nutritional Info: ");
				out.println(escapeXml(recipe.getNutritionalInfo()));
				out.println("</RecipeTip>");
			}
			out.println("</RecipeTips>");
		}

		// image
		// save the image
		if (isWriteImages()
			&& recipe.getImages()!= null
			&& recipe.getImages().size()>0
			&& recipe.getImages().get(0).isValid()) {
			out.print("<RecipeImage xmlns:dt=\"urn:schemas-microsoft-com:datatypes\" dt:dt=\"bin.base64\" FileType=\""+recipe.getImages().get(0).getImageType()+"\">");
			out.print(recipe.getImages().get(0).encodeAsBase64());
			out.println("</RecipeImage>");
		}
		
// TODO: REVIEWS:
		//<RecipeReviews><RecipeReview Rating="5" Reviewer="Peter" ReviewDate="11/19/01">Loved it!</RecipeReview>
		//               <RecipeReview Rating="4" Reviewer="Melissa" ReviewDate="11/19/01">It tried it with snow peas. It worked out great.</RecipeReview></RecipeReviews>
		if (recipe.getRatings().size() > 0) {
			out.println("<RecipeReviews>");
			String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

			int rating = 3;
			if (recipe.getRatings().size() == 1) {
				rating = Math.round(recipe.getRatings().values().toArray(new Float[1])[0] * 5);
			}
			out.print("<RecipeReview Rating=\""+rating+"\" Reviewer=\"Mastercook\" ReviewDate=\""+today+"\">");
			for (String ratingLabel : recipe.getRatings().keySet()) {
				out.println(ratingLabel+": "+ recipe.getRating(ratingLabel, 5));
			}
			out.println("</RecipeReview>");
			out.println("</RecipeReviews>");
		}
		
		
		
		// -----------------------------------------------------
/**** Missing fields to export 		
		// Alternative Source
		if (recipe.getTextAttribute(Recipe.TEXTATT_ID)!=null) {
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					   Recipe.TEXTATT_ID,
					   escapeXml(recipe.getTextAttribute(Recipe.TEXTATT_ID)));
			out.println();
		} else if (recipe.getUrl()!=null) {
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					   Recipe.TEXTATT_URL,
					   escapeXml(recipe.getUrl()));
			out.println();
		} else if (recipe.getAltSourceLabel()!=null){
			out.printf("<AltS label=\"%s\" source=\"%.255s\" />",
					   escapeXml(recipe.getAltSourceLabel()),
					   escapeXml(recipe.getAltSourceText()));
			out.println();
		}
		
		// ALT time
		if (recipe.getAltTimeLabel() != null) {
			out.printf("<AltT label=\"%.31s\" elapsed=\"%s\"/>",
					escapeXml(recipe.getAltTimeLabel()),
					escapeXml(recipe.getAltTimeText()));
			out.println();
		}
		
		// Ratings
		if (recipe.getRatings()!=null && recipe.getRatings().size() > 0) {
			out.println("<RatS>");
			int no = 0;
			for(String label: recipe.getRatings().keySet()) {
				out.printf("<RatE name=\"%.255s\" value=\"%d\"/>",
						escapeXml(label),
						recipe.getRating(label));
				out.println();
				no++;
				// max 3 ratings
				if (no>=3) break;
			}
			out.println("</RatS>");
		}
		
		*/
		// end of recipe
		out.println("</Recipe>");
	}

	/**
	 * Replace hardcoded returns with a coded linebreak for attributes.
	 * @param str
	 * @return
	 */
	public String escapeLinebreaks(String str) {
		return str.replaceAll("(\n\r?|\r\n?)", "&#xD;&#xA;");
	}
	
	/**
	 * @return a pattern to recognize this type of recipes.
	 */
    @Override
	public Pattern getRecognizePattern() {
		return Pattern.compile("<fdx\\s+source\\s*=", Pattern.CASE_INSENSITIVE+Pattern.MULTILINE);
	}

	
}
