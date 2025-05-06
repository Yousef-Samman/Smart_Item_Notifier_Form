package com.example.formhandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.springframework.mail.javamail.MimeMessageHelper;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

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

                // Predict label using Flask server
                String imagePath = Paths.get("uploads", imageName).toString();
                String predictedLabel = predictImageLabel(imagePath);
                System.out.println("Predicted label from Flask server: " + predictedLabel);

                // Send email for all submissions
                System.out.println("Attempting to send email...");
                sendEmail(email, name, imageName);
                System.out.println("✅ Email sent to " + email);
            }

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
    }

    public void sendEmail(String toEmail, String name, String item) {
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
            MimeMessage message = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail));
            helper.setTo(toEmail);
            helper.setSubject("Your IKEA Item Notification");

            // Compose the HTML email with item image and new message
            String itemImagePath = "uploads/" + item; // 'item' parameter is the image filename
            String html = "<html><body style='background:#f7f7f7;padding:0;margin:0;'>"
                    + "<div style='max-width:500px;margin:40px auto;background:#fff;border-radius:8px;box-shadow:0 2px 8px #e0e0e0;padding:32px 32px 16px 32px;font-family:sans-serif;border:1px solid #e6e6e6;'>"
                    + "<h2 style='color:#0058a3;margin-top:0;'>IKEA Item Notification</h2>"
                    + "<p style='font-size:16px;color:#222;'>Dear <strong>" + name + "</strong>,</p>"
                    + "<p style='font-size:15px;color:#333;line-height:1.6;'>Great news! The item you submitted is now <b>available</b> at IKEA." 
                    + "<br><br><b>Come visit us as soon as possible to get your item!</b>" 
                    + "<br>If you have any questions or would like to place an order, feel free to reply to this email.</p>"
                    + "<div style='text-align:center;margin:24px 0;'><img src='cid:itemImage' style='max-width:300px;border-radius:6px;border:1px solid #eee;'></div>"
                    + "<p style='margin-top:30px;font-size:15px;color:#444;'>Best regards,<br><b>The IKEA Item-Notifier Team</b></p>"
                    + "<div style='text-align:center;margin-top:32px;'><img src='cid:logoImage' style='width:120px;'></div>"
                    + "</div>"
                    + "</body></html>";
            helper.setText(html, true);

            // Attach the item image from uploads
            java.io.File itemFile = new java.io.File("uploads", item);
            if (itemFile.exists()) {
                helper.addInline("itemImage", itemFile);
            }

            // Attach the IKEA logo from the absolute path
            java.io.File logoFile = new java.io.File("C:/Users/youse/OneDrive/Desktop/KAU/Fourth_Year_IT/Term2/CPIT-380/Project_Git_Folder/Smart_Item_Notifier_Form/Ikea_logo.png");
            helper.addInline("logoImage", logoFile);

            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Sends a JPG image to a Flask server and returns the predicted label
    public static String predictImageLabel(String imagePath) {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String LINE_FEED = "\r\n";
        String urlString = "http://localhost:5000/predict";
        HttpURLConnection conn = null;
        java.io.OutputStream output = null;
        java.io.PrintWriter writer = null;
        java.io.InputStream responseStream = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            output = conn.getOutputStream();
            writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(output, java.nio.charset.StandardCharsets.UTF_8), true);

            // Send binary file.
            java.io.File imageFile = new java.io.File(imagePath);
            String fileName = imageFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: image/jpeg").append(LINE_FEED);
            writer.append(LINE_FEED).flush();
            java.nio.file.Files.copy(imageFile.toPath(), output);
            output.flush();
            writer.append(LINE_FEED).flush();

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(LINE_FEED).flush();

            // Get response
            int status = conn.getResponseCode();
            responseStream = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder response = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(responseStream, java.nio.charset.StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }

            // Parse JSON (simple, no external lib)
            String json = response.toString();
            System.out.println("Raw Flask response: " + json); // Debug print
            String label = null;
            String confidence = null;
            int labelIdx = json.indexOf("\"label\"");
            if (labelIdx != -1) {
                int colon = json.indexOf(":", labelIdx);
                int quote1 = json.indexOf('"', colon + 1);
                int quote2 = json.indexOf('"', quote1 + 1);
                label = json.substring(quote1 + 1, quote2);
            }
            int confIdx = json.indexOf("\"confidence\"");
            if (confIdx != -1) {
                int colon = json.indexOf(":", confIdx);
                int comma = json.indexOf(',', colon + 1);
                int end = comma == -1 ? json.indexOf('}', colon + 1) : comma;
                confidence = json.substring(colon + 1, end).replaceAll("[^0-9.eE-]", "").trim();
            }
            if (label == null || confidence == null) {
                System.out.println("[AI Prediction] ERROR: Could not parse label or confidence from Flask response.");
            }
            System.out.println("Predicted label: " + label + ", confidence: " + confidence);
            return label;
        } catch (Exception e) {
            System.out.println("Error sending image to Flask server: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (writer != null) writer.close();
            try { if (output != null) output.close(); } catch (IOException ignored) {}
            try { if (responseStream != null) responseStream.close(); } catch (IOException ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
}