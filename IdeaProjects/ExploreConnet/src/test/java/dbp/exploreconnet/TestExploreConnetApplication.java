package dbp.exploreconnet;

import org.springframework.boot.SpringApplication;

public class TestExploreConnetApplication {

    public static void main(String[] args) {
        SpringApplication.from(ExploreConnetApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
