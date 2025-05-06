from flask import Flask, request, jsonify
from PIL import Image
import torch
import torchvision.transforms as transforms
from torchvision import models
import json

app = Flask(__name__)

# Load ImageNet class labels
LABELS_URL = "https://raw.githubusercontent.com/pytorch/hub/master/imagenet_classes.txt"
import urllib.request
with urllib.request.urlopen(LABELS_URL) as f:
    categories = [line.strip().decode('utf-8') for line in f.readlines()]

# Load pretrained model
model = models.resnet18(pretrained=True)
model.eval()

# Image preprocessing
preprocess = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']
    img = Image.open(file.stream).convert('RGB')
    input_tensor = preprocess(img).unsqueeze(0)

    with torch.no_grad():
        output = model(input_tensor)
        probabilities = torch.nn.functional.softmax(output[0], dim=0)
        confidence, predicted_idx = torch.max(probabilities, 0)
        label = categories[predicted_idx]

    return jsonify({
        'label': label,
        'confidence': float(confidence)
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)