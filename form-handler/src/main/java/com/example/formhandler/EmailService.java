package com.example.formhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendItemAvailableEmail(String to, String name, String item) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your item is now available!");

            // ✅ HTML body
            String html = "<html><body>"
                    + "<img src='cid:logoImage' style='width:120px;'><br><br>"
                    + "<p>Hello <strong>" + name + "</strong>,</p>"
                    + "<p>Good news! The item you requested <strong>(" + item + ")</strong> is now back in stock and ready for you.</p>"
                    + "<p>If you have any questions or would like to place an order, feel free to reply to this email.</p>"
                    + "<p style='margin-top:30px;'>Best regards,<br>The IKEA Auto-Notifier Team</p>"
                    + "</body></html>";

            helper.setText(html, true);

            // ✅ Attach the IKEA logo (place it in src/main/resources)
            ClassPathResource logo = new ClassPathResource("C:\\Users\\youse\\OneDrive\\Desktop\\KAU\\Fourth_Year_IT\\Term2\\CPIT-380\\Project-Code\\Ikea_logo.png");
            helper.addInline("logoImage", logo);

            mailSender.send(message);
            System.out.println("✅ Email with image sent to " + to);

        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email to " + to);
        }
    }
}
