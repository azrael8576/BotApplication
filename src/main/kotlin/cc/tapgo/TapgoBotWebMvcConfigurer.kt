package cc.tapgo


/**
 * Created by AlexHe on 2019-08-08.
 * Describe
 */

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class KitchenSinkWebMvcConfigurer : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        val downloadedContentUri = downloadedContentDir
            .toUri().toASCIIString()
        logger.info("downloaded dir: {}", downloadedContentUri)
        registry!!.addResourceHandler("/downloaded/**")
            .addResourceLocations(downloadedContentUri)
        registry!!.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
    }
}