package com.frpr.service;

import com.frpr.model.User;
import com.frpr.repo.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
@Service
@Configuration
@EnableScheduling
public class CronJobService {

    @Autowired
    CustomerRepository customerRepository;


    @Scheduled(cron = "0 */10 * * * *")
    private void lastPasswordRestJob() {
        List<User> users = customerRepository.findAllByLastPasswordResetDateLessThanEqualAndIsRequiredToResetPassword(lastResetDate(), false);

        for (User u : users) {
            u.setIsRequiredToResetPassword(true);
        }
        customerRepository.saveAll(users);

    }

    private Date lastResetDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
