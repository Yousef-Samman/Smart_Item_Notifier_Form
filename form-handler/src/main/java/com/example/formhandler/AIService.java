package com.example.formhandler;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AIService {
    private final Map<String, String> referenceLabels = new HashMap<>(); // filename -> label
    private final Set<String> referenceLabelSet = new HashSet<>(); // unique labels

    @PostConstruct
    public void initReferenceLabels() {
        File refDir = new File("reference");
        if (!refDir.exists() || !refDir.isDirectory()) {
            System.out.println("Reference directory not found.");
            return;
        }
        for (File img : Objects.requireNonNull(refDir.listFiles())) {
            if (img.isFile() && img.getName().toLowerCase().endsWith(".jpg")) {
                String label = ProcessData.predictImageLabel(img.getAbsolutePath());
                if (label != null) {
                    referenceLabels.put(img.getName(), label);
                    referenceLabelSet.add(label);
                    System.out.println("Reference image: " + img.getName() + " -> " + label);
                } else {
                    System.out.println("Failed to predict label for reference image: " + img.getName());
                }
            }
        }
        System.out.println("Reference labels loaded: " + referenceLabelSet);
    }

    public String predictLabelForImage(String imagePath) {
        return ProcessData.predictImageLabel(imagePath);
    }

    public boolean isLabelInReference(String label) {
        return referenceLabelSet.contains(label);
    }

    public Map<String, String> getReferenceLabels() {
        return referenceLabels;
    }
} 