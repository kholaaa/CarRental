package com.example.carrental;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public final class MailService {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = MailService.class.getResourceAsStream("/mail.properties")) {
            if (in != null) {
                CONFIG.load(in);
            }
        } catch (Exception ignored) {
        }
    }

    private MailService() {
    }

    public static boolean isConfigured() {
        String host = CONFIG.getProperty("mail.smtp.host");
        return host != null && !host.isBlank();
    }

    public static boolean sendOtp(String to, String code) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        String username = CONFIG.getProperty("mail.smtp.username");
        String password = CONFIG.getProperty("mail.smtp.password");

        Properties props = new Properties();
        props.put("mail.smtp.host", CONFIG.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", CONFIG.getProperty("mail.smtp.port", "587"));
        props.put("mail.smtp.auth", CONFIG.getProperty("mail.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable", CONFIG.getProperty("mail.smtp.starttls.enable", "true"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Car Rental System - Password Reset Code");
        message.setText("Your password reset code is: " + code + "\n\n"
                + "This code expires in 10 minutes and can only be used once.\n"
                + "If you did not request a password reset, you can safely ignore this email.");

        Transport.send(message);
        return true;
    }
}
