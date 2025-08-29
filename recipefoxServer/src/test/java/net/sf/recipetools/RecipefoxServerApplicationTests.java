package net.sf.recipetools;

import net.sf.recipetools.javarecipes.format.JsonFormatter;
import net.sf.recipetools.javarecipes.format.LivingCookbookArchive;
import net.sf.recipetools.javarecipes.format.MasterCookBinary.Mc2Reader;
import net.sf.recipetools.javarecipes.model.Recipe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/*import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
*/
//import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RecipefoxServerApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testIsAlive() {
		String body = this.restTemplate.getForObject("/isAlive", String.class);
		assertThat(body).isEqualTo("true");
	}
	
	public String formatRecipes(String format, Recipe recipe) {
		return formatRecipes(format, "{}", recipe);
	}
	public String formatRecipes(String format, String config, Recipe recipe) {
		return formatRecipes(format, config, new ArrayList<Recipe>(Arrays.asList(recipe)));
	}
	public String formatRecipes(String format, String config, List<Recipe> recipes) {
		JsonFormatter jf = new JsonFormatter();
		MultiValueMap<String, Object> req = new LinkedMultiValueMap<String, Object>();
		req.add("format", format);
		req.add("config", config);
		req.add("recipes", jf.recipeToJson(recipes));
		
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(req, header);
		return this.restTemplate.postForObject("/recipes/format", httpEntity, String.class);
	}
	
	@Test
	public void testSimpleRecipeAsFDX() {
		Recipe recipe = new Recipe("title");
		String result = formatRecipes("FDX", recipe);
/* 		
		assertThat(result).isEqualTo("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
+"<fdx Source=\"Living Cookbook 2.0\" FileVersion=\"1.2\" date=\"2017-02-17\">\n"
+"<Recipes>\n"
+"<Recipe ID=\"1\" Name=\"title\" Servings=\"0\">\n"
+"</Recipe>\n"
+"</Recipes>\n"
+"</fdx>\n");
*/
		assertThat(result).contains("<Recipe ID=\"1\" Name=\"title\" Servings=\"0\">");
	}
	
	@Test
	public void testSimpleRecipeInVariousFormats() {
		Recipe recipe = new Recipe("title");
		String[][] formats = new String[][] {
			{"FDX", "<Recipe ID=\"1\" Name=\"title\" Servings=\"0\">"},
			{"MMF", "Title: title"},
			{"MXP", "Exported from  MasterCook"},
			{"MX2", "<RcpE name=\"title\" author=\"\">"},
			{"MZ2", "PK"},
		};
		for (int i=0; i<formats.length; i++) {
			String result = formatRecipes(formats[i][0], recipe);
			assertThat(result).contains(formats[i][1]);
		}
	}


	@Test
	public void testSimpleRecipeWithConfigs() {
		Recipe recipe = new Recipe("tItLe");
		String[][] configs = new String[][] {
			{"{\"TITLE_CASE_PREFERENCE\":2}", "<Recipe ID=\"1\" Name=\"TITLE\" Servings=\"0\">"},
			{"{\"TITLE_CASE_PREFERENCE\":1}", "<Recipe ID=\"1\" Name=\"Title\" Servings=\"0\">"},
			{"{\"TITLE_CASE_PREFERENCE\":0}", "<Recipe ID=\"1\" Name=\"tItLe\" Servings=\"0\">"},
		};
		for (int i=0; i<configs.length; i++) {
			String result = formatRecipes("FDX", configs[i][0], recipe);
			assertThat(result).as("config=%s",configs[i][0]).contains(configs[i][1]);
		}
	}
	
	@Test
	public void testWithManyRecipes() {
		LivingCookbookArchive formatter = new LivingCookbookArchive(); 
    	List<Recipe> recipes = formatter.read(new File("../core/src/test/data/FDXZ/Chili.fdxz"));
        assertEquals(93, recipes.size());
        
		String result = formatRecipes("FDX", "{}", recipes);
		assertThat(result).startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
	}
	
	@Test
	public void testWithManyRecipesAndPhotos() {
		Mc2Reader formatter = new Mc2Reader();
		long start = System.currentTimeMillis();
    	List<Recipe> recipes = formatter.read(new File("../core/src/test/data/MC/My Cookbook.mc2"));
    	System.out.println("**** Reading time="+(System.currentTimeMillis()-start));
        assertEquals(8666, recipes.size());
        
		start = System.currentTimeMillis();
		String result = formatRecipes("FDX", "{}", recipes);
    	System.out.println("**** Formatting time="+(System.currentTimeMillis()-start));
		assertThat(result).startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		formatter.close();
	}
	
	void testConfigs() {
		Recipe recipe = new Recipe("tItLe");
		String[][] configs = new String[][] {
			{"{\"TITLE_CASE_PREFERENCE\":2}", "<Recipe ID=\"1\" Name=\"TITLE\" Servings=\"0\">"},
			{"{\"TITLE_CASE_PREFERENCE\":1}", "<Recipe ID=\"1\" Name=\"Title\" Servings=\"0\">"},
			{"{\"TITLE_CASE_PREFERENCE\":0}", "<Recipe ID=\"1\" Name=\"tItLe\" Servings=\"0\">"},
		};
		for (int i=0; i<100; i++) {
			int index = i % 3;
			String result = formatRecipes("FDX", configs[index][0], recipe);
			assertThat(result).as("config=%s",configs[index][0]).contains(configs[index][1]);
		}
	}

	@Test
	public void testMultiThreaded() throws InterruptedException {
		Thread[] threads = new Thread[30];
		for (int i=0; i<threads.length; i++) {
			threads[i] = new Thread(this::testConfigs);
			threads[i].start();
		}
		// wait for them to finish
		for (int i=0; i<threads.length; i++) {
			threads[i].join();
		}
	}
	
}
