package batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("batch")
public class SpringBootJspApplication{


    public static void main(String[] args)  {
        SpringApplication.run(SpringBootJspApplication.class,args);
    }
}