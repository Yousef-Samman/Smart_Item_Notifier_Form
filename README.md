# 🧠 Smart Item Notifier Form

A smart Java-based web application that allows users to submit item images, automatically identifies them using AI-powered image recognition, and notifies administrators with customized emails. Designed with modularity and scalability in mind, this system uses Flask + PyTorch for classification and Spring Boot for form handling and backend services.

---

## 🚀 Features

- 📋 User-friendly form for uploading item images
- 🧠 AI-powered image classification using a PyTorch model via a Flask API
- 🗂 Preprocessing of all reference images at server startup
- 📤 Image classification on every form submission
- 📬 Conditional email notifications sent only when the uploaded image matches a reference
- ✉️ Professionally formatted HTML emails with IKEA branding and logo
- 🧩 Clean MVC structure and modular service integration
- 📁 Uploaded and reference image handling with efficient file management

---

## 📂 Project Structure

```
Smart_Item_Notifier_Form/
├── form-handler/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/formhandler/  # Controllers and Services
│   │   │   └── resources/
│   │   │       ├── static/                    # Includes Ikea_logo.png
│   ├── reference/                             # Reference images
│   ├── uploads/                               # Uploaded user images
│   ├── data/submissions.csv                   # Log of all submissions
│   └── pom.xml                                # Maven config
├── user-form.html
├── admin-dashboard.html
├── admin-login.html
├── index.html
├── script.js
├── style.css
└── README.md
```

---

## 🛠 Technologies Used

- **Java 17+**, **Spring Boot**, **JavaMailSender**
- **Python (Flask)**, **PyTorch**, **torchvision**
- **Apache Maven**
- **HTML/CSS/JS**, JSP
- **REST API communication**

---

## 🧠 AI Integration Overview

- A local Flask API loads a pre-trained PyTorch model (e.g., ResNet18)
- All reference images are processed at startup and stored in memory
- Each user-submitted image is classified by the same API
- The system checks whether the predicted label matches one from the reference set

---

## ✉️ Email Notification Logic

- Admins can manually trigger an email from the dashboard
- If the uploaded image matches a known reference:
  - A professional, HTML-formatted email is sent to the user
  - The email includes the IKEA logo, a thank-you message, and branding
- If the image is **not recognized**, no email is sent

---

## 🏃‍♂️ Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/Yousef-Samman/Smart_Item_Notifier_Form.git
   ```

2. **Start the Flask AI server**
   ```bash
   python image_classifier_api.py
   ```

3. **Build and run the Java backend**
   ```bash
   cd form-handler
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - User Form: `http://localhost:8080/user-form.html`
   - Admin Panel: `http://localhost:8080/admin-dashboard.html`

---

## 📧 Configuration

Edit `application.properties` for email settings:
```
spring.mail.username=your-email@example.com
spring.mail.password=your-app-password
```

Place `Ikea_logo.png` in `form-handler/src/main/resources/static/`.

---

