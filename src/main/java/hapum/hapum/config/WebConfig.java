package hapum.hapum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

//import hapum.hapum.interceptor.AdminInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor

public class WebConfig implements WebMvcConfigurer {

	@Value("${file.upload-dir}")
	private String uploadDir;

//    @Autowired
//    private AdminInterceptor adminInterceptor;

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}

	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		// 템플릿 리졸버 설정
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setPrefix("classpath:/templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		return templateResolver;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// TODO Auto-generated method stub
		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**").allowedOrigins("*");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/temp/**").addResourceLocations("file:///" + uploadDir + "/temp/");

		// /uploads/news/** 요청은 C:/upload/news/ 에 있는 파일로 매핑
		registry.addResourceHandler("/uploads/news/**").addResourceLocations("file:///" + uploadDir + "/news/");

		// /uploads/program/** 요청은 C:/upload/program/ 에 있는 파일로 매핑
		registry.addResourceHandler("/uploads/program/**").addResourceLocations("file:///" + uploadDir + "/program/");
		
		registry.addResourceHandler("/uploads/organization/**").addResourceLocations("file:///" + uploadDir + "/organization/");
		registry.addResourceHandler("/uploads/organizationPost/**").addResourceLocations("file:///" + uploadDir + "/organizationPost/");

		// /uploads/post/** 요청은 C:/upload/post/ 에 있는 파일로 매핑
		registry.addResourceHandler("/uploads/post/**").addResourceLocations("file:///" + uploadDir + "/post/");
		// 정적 리소스 매핑
		registry.addResourceHandler("/img/**", "/css/**", "/js/**"

		).addResourceLocations("classpath:/static/img/", "classpath:/static/css/", "classpath:/static/js/"

		);
	}

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(adminInterceptor)
//                .addPathPatterns("/admin/**");
//    }

}
