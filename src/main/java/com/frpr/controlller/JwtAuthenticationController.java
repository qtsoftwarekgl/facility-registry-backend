package com.frpr.controlller;

import com.frpr.config.JwtTokenUtil;
import com.frpr.config.JwtUserDetailsService;
import com.frpr.model.Action;
import com.frpr.model.User;
import com.frpr.repo.CustomerRepository;
import com.frpr.response.CommonResponse;
import com.frpr.service.AuditService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    CustomerRepository repository;

    @Autowired
    AuditService auditService;

    @Value("${facility.maxWrongPasswordRetry:5}")
    private int maxRetry;

    private String getLoggedInUser() {

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            try {
                return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            } catch (Exception ignored) {

            }
        }
        return "N/A";
    }


    @RequestMapping(value = "/admin/email", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken1(@RequestBody JwtRequest authenticationRequest, HttpServletRequest request) throws Exception {
        auditService.saveAuditLog(authenticationRequest.getUsername(), User.class.getSimpleName(), Action.LOGIN, authenticationRequest.getUsername(), null, request);
        List<User> userList = repository.findAllByEmail(authenticationRequest.getUsername());
        if (StringUtils.isBlank(authenticationRequest.getUsername())) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.Please enter valid email"));
        }
        if (!userList.isEmpty()) {
            if (userList.size() > 1) {
                return ResponseEntity.ok(new CommonResponse("error", "server_error.More than one user found with same email"));
            }
            return ResponseEntity.ok(new CommonResponse("ok", "server_error.email_already_exists"));
        } else {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.no_records_found"));
        }
    }

    @RequestMapping(value = "/admin/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody JwtRequest authenticationRequest, HttpServletRequest request) throws Exception {

        auditService.saveAuditLog(authenticationRequest.getUsername(), User.class.getSimpleName(), Action.LOGIN, authenticationRequest.getUsername(), null, request);

        if (StringUtils.isBlank(authenticationRequest.getUsername())) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.Please enter valid email"));
        }

        if (StringUtils.isBlank(authenticationRequest.getPassword())) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.Please enter valid password"));
        }

        return createAuthenticationToken(authenticationRequest);
    }

    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        List<User> userList = repository.findAllByEmail(authenticationRequest.getUsername());
        if (userList.isEmpty()) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.no_user_exists_with_the_Email"));
        }

        if (userList.size() > 1) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.More than one user found with same email"));
        }

        User u = userList.get(0);

        if (u.getStatus() == null || !"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            return ResponseEntity.ok(new CommonResponse("error", "server_error.user_is_inactive_Please contact your administrator"));
        }

        if (bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), u.getPassword())) {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());

            final String token = jwtTokenUtil.generateToken(userDetails);
            u.setWrongPasswordCount(0);
            repository.save(u);
            return ResponseEntity.ok(new CommonResponse("ok", null).setData(new JwtResponse(token, u)));
        } else if ((u.getWrongPasswordCount() + 1) < maxRetry) {
            u.setWrongPasswordCount(u.getWrongPasswordCount() + 1);
            repository.save(u);
            return ResponseEntity.ok(new CommonResponse("error", "server_error.invalid_email_or_password. You have " + ( maxRetry - (u.getWrongPasswordCount())) + " more attempt before your account gets locked"));
        } else {
            return doInactivateUser(u);
        }
    }

    private ResponseEntity<?> doInactivateUser(User u) {
        u.setWrongPasswordCount(0);
        u.setStatus("INACTIVE");
        repository.save(u);
        return ResponseEntity.ok(new CommonResponse("error", "server_error.max_retry_count_reached. Account got blocked. Please contact Administrator"));
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<?> getProfile(Authentication authentication, Principal principal, HttpServletRequest request) throws Exception {
        auditService.saveAuditLog(authentication.getName(), User.class.getSimpleName(), Action.LOGIN, authentication, null, request);
        System.out.println(authentication.getName());
        System.out.println("-----------------");
        System.out.println(principal.getName());
        return ResponseEntity.ok(new CommonResponse("ok", null).setData(repository.findAllByEmail(authentication.getName()).get(0)));

    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}