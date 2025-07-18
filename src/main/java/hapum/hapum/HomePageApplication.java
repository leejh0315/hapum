package hapum.hapum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@MapperScan("hapum.hapum.mapper")
public class HomePageApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomePageApplication.class, args);
	}

}
