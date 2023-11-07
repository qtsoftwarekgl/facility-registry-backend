package com.frpr.controlller;

import com.frpr.model.Action;
import com.frpr.model.FacilityRegistry;
import com.frpr.model.User;
import com.frpr.pojo.ChangePasswordReq;
import com.frpr.repo.CustomerRepository;
import com.frpr.response.CommonResponse;
import com.frpr.service.AuditService;
import com.frpr.service.EmailService;
import com.frpr.utils.GenerateOTPUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    CustomerRepository repository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private AuditService auditService;

    @Autowired
    private EmailService emailService;

    private String EMAIL_SENT_SUCCESS = "EMAIL_SENT_SUCCESS";

    private String getLoggedInUser() {
        try {
            return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        } catch (Exception e) {

        }
        return "";
    }

    @PostMapping
    public ResponseEntity<CommonResponse> save(@RequestBody User user, HttpServletRequest request) {

        if (isAlreadyExist(null, user.getEmail(), user.getDocumentNumber(), user.getNin(), user.getNid())) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.user_already_exist_with_email_or_Document_Number"), HttpStatus.ACCEPTED);
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(new Date());
        user.setLastPasswordResetDate(new Date());
        user.setIsRequiredToResetPassword(true);
        User u = repository.save(user);
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.ADD, user, null, user, request);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<?> getProfile(Authentication authentication, Principal principal, HttpServletRequest request) throws Exception {
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.GET, authentication.getName(), null, request);
        System.out.println(authentication.getName());
        System.out.println("-----------------");
        System.out.println(principal.getName());
        return ResponseEntity.ok(new CommonResponse("ok", null).setData(repository.findAllByEmail(authentication.getName()).get(0)));

    }

    @PutMapping
    public ResponseEntity<CommonResponse> update(@RequestBody User user, HttpServletRequest request) {
        Optional<User> userOptional = repository.findById(user.get_id());

        if(!userOptional.isPresent()){
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.userNotFound").setData(user), HttpStatus.ACCEPTED);
        }
        User oldData = userOptional.get();
        if (user.getSurName() == null) {

            if (user.getStatus() == null || (user.getStatus().equalsIgnoreCase("INACTIVE") && StringUtils.isBlank(user.getDeactivateReason()))) {
                return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.deactivate status should be provided").setData(user), HttpStatus.ACCEPTED);
            }

            oldData.setStatus(user.getStatus());
            oldData.setDeactivateReason(user.getDeactivateReason());
            oldData = repository.save(oldData);
            auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, user, oldData, user, request, "");
            return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(user), HttpStatus.ACCEPTED);
        }

        if (isAlreadyExist(user.get_id(), user.getEmail(), user.getDocumentNumber(), user.getNin(), user.getNid())) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.user_already_exist_with_email_or_Document_Number"), HttpStatus.ACCEPTED);
        }




        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setLastPasswordResetDate(new Date());
            user.setIsRequiredToResetPassword(false);
        } else {
            User data = repository.findById(user.get_id()).get();
            user.setLastPasswordResetDate(data.getLastPasswordResetDate() == null ? new Date() : data.getLastPasswordResetDate());
            user.setIsRequiredToResetPassword(data.getIsRequiredToResetPassword() != null && data.getIsRequiredToResetPassword());
        }


        User updatedUser = repository.save(user);
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, user, updatedUser, oldData , request);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(updatedUser), HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse> updateStatus(@PathVariable String id, @RequestBody User user, HttpServletRequest request) {
        try {

            Optional<User> userOptional = repository.findById(id);

            if(!userOptional.isPresent()){
                return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.userNotFound").setData(user), HttpStatus.ACCEPTED);
            }

            User updatedRecord = repository.findById(id).get();


            updatedRecord.setStatus(user.getStatus());
            updatedRecord.setDeactivateReason(user.getDeactivateReason());
            updatedRecord = repository.save(updatedRecord);


            auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, user, userOptional.get(), updatedRecord, request);
            return new ResponseEntity<CommonResponse>(new CommonResponse("ok", "server_success.updated_users").setData(updatedRecord), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.updated_failed"), HttpStatus.ACCEPTED);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse> getOnce(@PathVariable String id, HttpServletRequest request) {

        Optional<User> u = repository.findById(id);
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.GET, id, u.orElse(null), request);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(u.get()), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<CommonResponse> getAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit,
                                                 @RequestParam(required = false) String email,
                                                 @RequestParam(required = false) String role,
                                                 @RequestParam(required = false) String facilityName,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String name, HttpServletRequest request) {

        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.GET, request.getQueryString(), null, request);
        Page<User> users;
        if (name != null || email != null || role != null || status != null /*|| facilityName != null*/) {
            Query query = new Query();
            if (email != null && !email.isEmpty())
                query.addCriteria(Criteria.where("email").regex(email, "i"));

            if (name != null && !name.isEmpty()) {
                Criteria criteria = new Criteria();
                criteria.orOperator(Criteria.where("surName").regex(name, "i"), Criteria.where("postNames").regex(name, "i"));

                //  query.addCriteria(Criteria.where("surName").regex(name,"i"));
                //   query.addCriteria(Criteria.where("postNames").regex(name,"i"));
                query.addCriteria(criteria);
            }


            if (role != null && !role.isEmpty())
                query.addCriteria(Criteria.where("role").regex(role, "i"));

            if (status != null && !status.isEmpty())
                query.addCriteria(Criteria.where("status").is(status));


            List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));

            users = PageableExecutionUtils.getPage(
                    list,
                    PageRequest.of(page - 1, limit, Sort.by("name")),
                    () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), FacilityRegistry.class));
        } else {
            users = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("name")));
        }
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(users.getContent()).setCount(users.getTotalElements()), HttpStatus.ACCEPTED);
    }

    @PatchMapping("/activate/{id}/{status}")
    public ResponseEntity<CommonResponse> activeInActive(@PathVariable String id, @PathVariable String status, HttpServletRequest request) {

        Optional<User> u = repository.findById(id);
        if(!u.isPresent()){
            return new ResponseEntity<CommonResponse>(new CommonResponse("error", "server_error.userNotFound"), HttpStatus.ACCEPTED);
        }
        Optional<User> old = repository.findById(id);
        u.get().setStatus(status);
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, id, old.get(), u.orElse(null), request);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(repository.save(u.get())), HttpStatus.ACCEPTED);
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable String id, HttpServletRequest request) {
        Optional<User> u = repository.findById(id);
        Optional<User> old = repository.findById(id);
        u.get().setStatus("DELETED");
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.DELETE, id, old, u.orElse(null), request);
        return new ResponseEntity<CommonResponse>(new CommonResponse("ok", null).setData(repository.save(u.get())), HttpStatus.ACCEPTED);
    }


    @PutMapping(value = "/updatePassword")
    public ResponseEntity<?> updatePassword(Authentication authentication, Principal principal, @RequestBody ChangePasswordReq changePasswordReq, HttpServletRequest request) throws Exception {

        User user = repository.findAllByEmail(authentication.getName()).get(0);
        User old = repository.findAllByEmail(authentication.getName()).get(0);

        if (bCryptPasswordEncoder.matches(changePasswordReq.getCurrentPassword(), user.getPassword())) {
            user.setPassword(bCryptPasswordEncoder.encode(changePasswordReq.getNewPassword()));
            auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, changePasswordReq, old, user, request);
        } else {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.current_password_not_match").setData(repository.findAllByEmail(authentication.getName()).get(0)));
        }
        user.setLastPasswordResetDate(new Date());
        user.setIsRequiredToResetPassword(false);
        return ResponseEntity.ok(new CommonResponse("ok", null).setData(repository.save(user)));

    }

    @PutMapping(value = "/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ChangePasswordReq request, HttpServletRequest servletRequest) throws Exception {

        List<User> userList = repository.findAllByEmail(request.getEmail());
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, request, null, servletRequest);
        if (userList.isEmpty()) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.no_user_found"));
        } else {
            User user = userList.get(0);
            String otp = generateOTP();
            if(otp.isEmpty()) {
                return ResponseEntity.ok(new CommonResponse("error", "Unable to generate the one time password at this time please try again later"));
            }
            String result = emailService.sendOTP(otp, user);
            if(Objects.equals(result, EMAIL_SENT_SUCCESS)){
                //upon successful email sent save the information into the database for later use
                user.setOtp(otp);
                repository.save(user);
                return ResponseEntity.ok(new CommonResponse("ok", "OTP is Sent to " + user.getEmail() + " Please check you email"));
            } else {
                return ResponseEntity.ok(new CommonResponse("error", "Unable to send OTP to the Email at this time please try again later"));
            }
        }
    }

    private String generateOTP(){
        boolean condition = true;
        int counter = 0;
        do{
            String otp = GenerateOTPUtils.generateOTP();
            User isOptUsed = repository.findOneByOtp(otp);

            if(isOptUsed != null){
                counter++;
            } else {
                return otp;
            }

            if(counter > 10 && condition == true){
                condition = false;
            }

        } while(condition);
        return "";
    }

    @PutMapping(value = "/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordReq request, HttpServletRequest servletRequest) throws Exception {
        auditService.saveAuditLog(getLoggedInUser(), User.class.getSimpleName(), Action.UPDATE, request, null, servletRequest);
        List<User> userList = repository.findAllByEmail(request.getEmail());
        if (userList.isEmpty()) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.no_user_found"));
        } else if (request.getCurrentPassword().equalsIgnoreCase(userList.get(0).getOtp())) {
            User user = userList.get(0);
            user.setOtp(null);
            user.setLastPasswordResetDate(new Date());
            user.setIsRequiredToResetPassword(false);
            user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
            repository.save(user);
            return ResponseEntity.ok(new CommonResponse("ok", "server_success.password_changed_successfully"));
        } else {
            return ResponseEntity.ok(new CommonResponse("error", "Please provide the valid Code received in your registered email inbox"));
        }
    }

    private boolean isAlreadyExist(String exstingId, String email, String documentNumber, String nin, String nid) {


        if (exstingId != null || email != null || nin != null || nid != null) {
            Query query = new Query();

            if (email != null && !email.isEmpty()) {
                query = new Query();
                query.addCriteria(Criteria.where("email").regex(email, "i"));
                List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));
                return isUserIdSame(list, exstingId);
            }
            if (documentNumber != null && !documentNumber.isEmpty()) {
                query = new Query();
                query.addCriteria(Criteria.where("documentNumber").regex(documentNumber, "i"));
                List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));
                return isUserIdSame(list, exstingId);
            }

            if (nin != null && !nin.isEmpty()) {
                query = new Query();
                query.addCriteria(Criteria.where("nin").regex(nin, "i"));
                List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));
                return isUserIdSame(list, exstingId);
            }

            if (nid != null && !nid.isEmpty()) {
                query = new Query();
                query.addCriteria(Criteria.where("nid").regex(nid, "i"));
                List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));
                return isUserIdSame(list, exstingId);

            }

            List<User> list = new ArrayList<>(mongoTemplate.find(query, User.class));

            return isUserIdSame(list, exstingId);
        }

        return false;
    }

    private boolean isUserIdSame(List<User> list, String exstingId) {

        if (list.isEmpty())
            return false;

        if (list.size() > 1) return true;

        User registry = list.get(0);

        if (exstingId != null) {
            return !(registry.get_id().equalsIgnoreCase(exstingId));
        } else {
            return true;
        }
    }
}
