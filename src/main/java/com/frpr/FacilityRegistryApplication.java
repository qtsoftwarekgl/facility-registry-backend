package com.frpr;

import com.frpr.model.User;
import com.frpr.repo.CustomerRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
@EnableMongoAuditing
@OpenAPIDefinition(info = @Info(title = "Facility Registry API", version = "2.0", description = "Facility Registry Information"))
public class FacilityRegistryApplication implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(FacilityRegistryApplication.class, args);
    }


    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @PostConstruct
    public void init(){
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("CAT"));
    }

    @Override
    public void run(String... args) throws Exception {
        // repository.deleteAll();

        // save a couple of customers
        User customer = new User();
        customer.setName("ADMIN");
        customer.setEmail("admin@frbr.com");
        customer.setPassword(bCryptPasswordEncoder.encode("admin"));
        customer.setRole("ADMIN");
        customer.setValue("ADMIN");
        customer.setMinistry("MOH");
        customer.setStatus("ACTIVE");
        customer.setStatus("ACTIVE");

        List<User> customers = repository.findAllByEmail("admin@frbr.com");

        if (customers.isEmpty()) {
            repository.save(customer);
        } else {
            //reset password
          /*  User user =    customers.get(0);
            user.setPassword(bCryptPasswordEncoder.encode("admin"));
            repository.save(customer);*/
        }


        /*for (User customer1 : repository.findAllByEmail("admin@frbr.com")) {
            System.out.println(customer1);
        }*/


        // fetch all customers
        /*System.out.println("Customers found with findAll():");
        System.out.println("-------------------------------");
        for (User customer1 : repository.findAll()) {
            System.out.println(customer1);
        }
        System.out.println();*/

        // fetch an individual customer
        // System.out.println("Customer found with findByFirstName('Alice'):");
        // System.out.println("--------------------------------");
       /* System.out.println(repository.findAllByEmail("Alice"));

        System.out.println("Customers found with findByLastName('Smith'):");
        System.out.println("--------------------------------");
        for (User customer1 : repository.findByLastName("Smith")) {
            System.out.println(customer1);
        }*/

    }


    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("fr-async-");
        executor.initialize();
        return executor;
    }
}