package com.frpr.repo;

import java.util.Date;
import java.util.List;

import com.frpr.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<User, String> {

  public List<User> findAllByEmail(String lastName);


  List<User> findAllByLastPasswordResetDateLessThanEqualAndIsRequiredToResetPassword(Date lastUpdatedDate, Boolean isRequiredUpdate);

  User findOneByOtp(String opt);

}