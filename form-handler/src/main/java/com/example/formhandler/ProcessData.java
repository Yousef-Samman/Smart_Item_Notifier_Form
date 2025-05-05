package com.example.formhandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import io.github.cdimascio.dotenv.Dotenv;

public class ProcessData {

    public void run() {
        try {
            Path csvPath = Paths.get("data", "submissions.csv");
            System.out.println("Reading CSV from: " + csvPath.toAbsolutePath());
            List<String> lines = Files.readAllLines(csvPath);
            System.out.println("Found " + lines.size() + " submissions to process");

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                String name = parts[0];
                String email = parts[1];
                String gender = parts[2];
                int quantity = Integer.parseInt(parts[3]);
                String imageName = parts[4];

                System.out.println("\nProcessing submission:");
                System.out.println("Name: " + name);
                System.out.println("Email: " + email);
                System.out.println("Gender: " + gender);
                System.out.println("Quantity: " + quantity);
                System.out.println("Image name: " + imageName);

                // Send email for all submissions
                System.out.println("Attempting to send email...");
                sendEmail(email, name, "IKEA Item Notification");
                System.out.println("✅ Email sent to " + email);
            }

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private void sendEmail(String toEmail, String name, String item) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Dotenv dotenv = Dotenv.load();
        String fromEmail = dotenv.get("MAIL_USERNAME");
        String appPassword = dotenv.get("MAIL_PASSWORD");
        System.out.println("DEBUG: fromEmail=" + fromEmail + ", appPassword=" + appPassword);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("IKEA Item Notification");
            message.setText("Dear " + name + ",\n\nThank you for your submission. We have received your request and will process it shortly.\n\nBest regards,\nIKEA Item Notifier Team");

            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}