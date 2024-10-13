package dbp.exploreconnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class ExploreConnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExploreConnetApplication.class, args);
    }

}
