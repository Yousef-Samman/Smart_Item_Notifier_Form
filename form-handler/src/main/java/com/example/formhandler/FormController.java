package com.example.formhandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class FormController {

    private final List<Map<String, Object>> submittedData = new CopyOnWriteArrayList<>();

    @Autowired
    private AIService aiService;

    @PostMapping("/submit")
    public ResponseEntity<String> handleForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("gender") String gender,
            @RequestParam("quantity") int quantity,
            @RequestParam("photo") MultipartFile photo) throws IOException {

        System.out.println("=== /submit called ===");

        // Save image to /uploads
        String imageName = System.currentTimeMillis() + "-" + photo.getOriginalFilename();
        Path imagePath = Paths.get("uploads", imageName);
        Files.createDirectories(imagePath.getParent());
        Files.copy(photo.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Saved image: " + imagePath.toAbsolutePath());

        // AI prediction
        String predictedLabel = aiService.predictLabelForImage(imagePath.toString());
        System.out.println("[AI Prediction] Label: " + predictedLabel);

        // Save to CSV (add label as last column)
        String csvLine = String.join(",", name, email, gender, String.valueOf(quantity), imageName, predictedLabel) + "\n";
        Path csvPath = Paths.get("data", "submissions.csv");
        Files.createDirectories(csvPath.getParent());
        Files.write(csvPath, csvLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // Add to in-memory list (include label)
        Map<String, Object> submission = Map.of(
                "name", name,
                "email", email,
                "gender", gender,
                "quantity", quantity,
                "photo", imageName,
                "label", predictedLabel
        );
        submittedData.add(submission);

        System.out.println("======================");
        return ResponseEntity.ok("Submission saved successfully");
    }

    @PostMapping("/upload-submissions")
    public ResponseEntity<String> uploadSubmissions(@RequestBody List<Map<String, Object>> submissions) {
        System.out.println("Received submissions: " + submissions.size());
        submittedData.addAll(submissions);
        return ResponseEntity.ok("Stored " + submissions.size() + " submissions on server.");
    }

    @GetMapping("/view-submissions")
    public List<Map<String, Object>> getSubmissions() {
        return submittedData;
    }
}
