package net.sf.recipetools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootApplication
public class RecipefoxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipefoxServerApplication.class, args);
	}

	
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
    	TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    	factory.addConnectorCustomizers(connector -> connector.setMaxPostSize(100000000)); // 100 MB
    	return factory;
    }
    

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(-1);
        return multipartResolver;
    }    
    
/*
	@Bean
	public TomcatEmbeddedServletContainerFactory tomcatEmbedded() {
	    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
	    tomcat.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
	        if ((connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>)) {
	            //-1 for unlimited
	            ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
	        }
	    });
	    return tomcat;
	}
*/
	
	// Set maxPostSize of embedded tomcat server to 10 megabytes (default is 2 MB, not large enough to support file uploads > 1.5 MB)
/*
	@Bean
	EmbeddedServletContainerCustomizer containerCustomizer() throws Exception {
	    return (ConfigurableEmbeddedServletContainer container) -> {
	        if (container instanceof TomcatEmbeddedServletContainerFactory) {
	            TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
	            tomcat.addConnectorCustomizers(
	                (connector) -> {
	                    connector.setMaxPostSize(100000000); // 100 MB
	                }
	            );
	        }
	    };
	}
*/
}
