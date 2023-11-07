package com.frpr.service;

import com.frpr.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

@Service
public class EmailService {
    @Value("${email.smtp.host}")
    private String smtpHost;

    @Value("${email.smtp.port}")
    private String smtpPort;

    @Value("${email.smtp.username}")
    private String smtpUsername;

    @Value("${email.smtp.password}")
    private String smtpPassword;

    private String EMAIL_SENT_SUCCESS = "EMAIL_SENT_SUCCESS";
    private String EMAIL_SENT_FAILED = "EMAIL_SENT_FAILED";
    private Session getSession(){
        //Create the SMTP session
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.socketFactory.port", smtpPort);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", smtpPort);
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        };

        return Session.getDefaultInstance(properties, auth);
    }

    public String generateEmail(String toEmail, String subject, String body, String personal){
        try
        {
            Session session = getSession();
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("no_reply@moh.gov.rw", personal));

            msg.setReplyTo(InternetAddress.parse("no_reply@moh.gov.rw", false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setContent(body, "text/html; charset=utf-8");

            Transport.send(msg);

            System.out.println("Email Sent Successfully to " + toEmail + "!!");
            return EMAIL_SENT_SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return EMAIL_SENT_FAILED;
        }
    }

    public String sendOTP(String otp, User user) {
        String body = "Hi, <strong>" + user.getSurName() + " " + user.getPostNames() + "</strong><br />";
        body += "We have received a forgot password request from your email: " + user.getEmail() + "<br />";
        body += "<br />Please use the below code to change your password:<br />" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<strong style='font-size: 32px; letter-spacing: 10px;'>" + otp + "</strong></center>";
        body += "<br /><hr /><p style='color:red'>If you have not initiated the forgot password process please ignore this email!</p>";
        String toEmail = user.getEmail();
        String subject = "Facility Registry - OTP";
        String personal = "Facility Registry";
        String res = generateEmail(toEmail, subject, body, personal);
        if(Objects.equals(res, EMAIL_SENT_SUCCESS)) {
            return EMAIL_SENT_SUCCESS;
        }
        else {
            return EMAIL_SENT_FAILED;
        }
    }
}
