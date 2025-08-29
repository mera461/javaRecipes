package net.sf.recipetools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.sf.recipetools.javarecipes.model.RecipeFoxException;

@Service
@RestController
public class RecipefoxController {
	
	@Autowired
	RecipefoxApi recipefoxApi;
	
	//private final Logger log = LoggerFactory.getLogger(this.getClass());

    @CrossOrigin
    @RequestMapping(value="/recipes/format", 
					method=RequestMethod.POST,
					consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
					produces = "application/octet-stream")
    public ResponseEntity<InputStreamResource>  formatRecipes(@RequestParam(value="format") String format,
								  @RequestParam(value="config") String config,
								  @RequestParam(value="recipes") String recipes) throws IOException {

    	ResponseEntity<InputStreamResource> reply = null;

   		File f = recipefoxApi.formatRecipes(format, config, recipes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

   		reply =  ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(f.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(f)));
        
        return reply;
    }

    @ExceptionHandler
    void handleRecipeFoxException(RecipeFoxException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        				   e.getMessage());
    }
    
    
    @CrossOrigin
    @RequestMapping(value="/isAlive", 
    				method=RequestMethod.GET,
    				produces = "application/json")
    public boolean isAlive() {
    	return true;
    }
}