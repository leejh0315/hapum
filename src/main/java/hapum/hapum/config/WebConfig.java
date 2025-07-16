package hapum.hapum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import hapum.hapum.interceptor.AdminInterceptor;
import hapum.hapum.interceptor.UserInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;
    private final UserInterceptor userInterceptor;
    
    

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${summernote.upload.temp-dir}")
    private String tempVideoDir;

    @Value("${summernote.upload.video-dir}")
    private String uploadVideoDir;

    @Value("${doc.program.output.dir}")
    private String outputDir;

    
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return templateResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*"); // 필요 시 특정 도메인만 허용 가능
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	 String path = outputDir.replace("\\", "/");
         if (!path.endsWith("/")) {
             path += "/";
         }
         String resourceLocation = "file:///" + path;

         registry
           .addResourceHandler("/result/**")       // URL 패턴
           .addResourceLocations(resourceLocation) // 실제 파일 시스템 위치
           .setCachePeriod(0);                     // 개발 중이면 캐시 끄기
    

    	
        registry.addResourceHandler("/temp/videos/**")
                .addResourceLocations("file:" + tempVideoDir + "/")
                .setCachePeriod(0);

        registry.addResourceHandler("/uploads/videos/**")
                .addResourceLocations("file:" + uploadVideoDir + "/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/uploads/notification/**")
                .addResourceLocations("file:///C:/upload/notification/");

        registry.addResourceHandler("/uploads/temp/**")
                .addResourceLocations("file:///" + uploadDir + "/temp/");

        
        
        registry.addResourceHandler("/uploads/news/**")
                .addResourceLocations("file:///" + uploadDir + "/news/");
        
        registry.addResourceHandler("/uploads/program/**")
                .addResourceLocations("file:///" + uploadDir + "/program/");

        registry.addResourceHandler("/uploads/organization/**")
                .addResourceLocations("file:///" + uploadDir + "/organization/");

        registry.addResourceHandler("/uploads/organizationPost/**")
                .addResourceLocations("file:///" + uploadDir + "/organizationPost/");

        registry.addResourceHandler("/uploads/post/**")
                .addResourceLocations("file:///" + uploadDir + "/post/");

        registry.addResourceHandler("/img/**", "/css/**", "/js/**")
                .addResourceLocations(
                        "classpath:/static/img/",
                        "classpath:/static/css/",
                        "classpath:/static/js/"
                );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");

        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/main/mypage/**");
    }
}
