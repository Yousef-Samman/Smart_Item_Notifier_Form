package com.example.formhandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import io.github.cdimascio.dotenv.Dotenv;

@RestController
public class AdminController {

    private static final Dotenv dotenv = Dotenv.load();

    @PostMapping("/admin/login")
    public ResponseEntity<?> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {
        
        String adminUsername = dotenv.get("ADMIN_USERNAME");
        String adminPassword = dotenv.get("ADMIN_PASSWORD");
        System.out.println("DEBUG: adminUsername=" + adminUsername + ", adminPassword=" + adminPassword);
        if (adminUsername != null && adminUsername.equals(username) &&
            adminPassword != null && adminPassword.equals(password)) {
            session.setAttribute("adminAuthenticated", true);
            return ResponseEntity.ok().body(Map.of("message", "Login successful"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/admin/run-email-process")
    public ResponseEntity<?> runEmailProcess(HttpSession session) {
        if (session.getAttribute("adminAuthenticated") == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        try {
            ProcessData app = new ProcessData();
            app.run();
            return ResponseEntity.ok().body(Map.of("message", "Email process completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error running email process: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/upload-reference-image")
    public ResponseEntity<?> uploadReferenceImage(
            @RequestParam("image") MultipartFile image,
            HttpSession session) {
        
        if (session.getAttribute("adminAuthenticated") == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        try {
            // Create reference directory if it doesn't exist
            Path referenceDir = Paths.get("reference");
            Files.createDirectories(referenceDir);

            // Save the image
            String fileName = image.getOriginalFilename();
            Path filePath = referenceDir.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok().body(Map.of("message", "Image uploaded successfully"));
        } catch (IOException e) { // Add error logging
            // Add error logging
            return ResponseEntity.internalServerError().body(Map.of("message", "Error uploading image: " + e.getMessage()));
        }
    }
} 