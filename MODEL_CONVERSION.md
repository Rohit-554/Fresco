# ML Model Guide — EfficientNet-B0 for Fresco

> How to download, convert, and integrate EfficientNet-B0 into your Fresco project

---

## Quick Links

| Resource | Link |
|---|---|
| **Hugging Face — EfficientNet-B0** | https://huggingface.co/huggingface/efficientnet-b0 |
| **PyTorch Official Docs** | https://pytorch.org/vision/stable/models/efficientnet.html |
| **ONNX Official** | https://onnx.ai |
| **Apple Core ML Tools** | https://coremltools.readme.io |
| **ONNX Model Zoo** | https://github.com/onnx/models |

---

## Architecture Overview

```
┌──────────────────────────────────────────────┐
│              Fresco App                       │
├──────────────────────────────────────────────┤
│                                               │
│  commonMain: FruitClassifier interface       │
│                                               │
│  ┌─────────────────┐  ┌──────────────────┐   │
│  │   Android       │  │      iOS         │   │
│  │                 │  │                  │   │
│  │ ONNX Runtime    │  │  Core ML         │   │
│  │ .onnx format    │  │  .mlmodelc       │   │
│  │                 │  │  format          │   │
│  └─────────────────┘  └──────────────────┘   │
│                                               │
└──────────────────────────────────────────────┘
```

| Platform | Format | Location | How to Add |
|---|---|---|---|
| **Android** | `.onnx` | `composeApp/src/androidMain/assets/model/efficientnet_b0.onnx` | Drop file in directory |
| **iOS** | `.mlmodelc` | Xcode project bundle | Add via Xcode → Add Files |

---

## Option 1: Download Pre-converted ONNX Model (Recommended)

### From Hugging Face

Hugging Face hosts pre-trained models that can be exported to ONNX:

```bash
# Install required packages
pip install transformers optimum[onnxruntime]

# Download and export EfficientNet-B0 from Hugging Face
# Note: Hugging Face has ResNet-50 and other models optimized for ONNX
optimum-cli export onnx \
  --model microsoft/resnet-50 \
  --task image-classification \
  --opset 11 \
  ./onnx_model/
```

### From ONNX Model Zoo

The ONNX Model Zoo provides pre-converted models:

```bash
# Download pre-converted ONNX model
wget https://github.com/onnx/models/raw/main/validated/vision/classification/efficientnet/model/efficientnet-12.onnx

# Rename and place in your project
mkdir -p composeApp/src/androidMain/assets/model/
mv efficientnet-12.onnx composeApp/src/androidMain/assets/model/efficientnet_b0.onnx
```

---

## Option 2: Convert from PyTorch (Full Control)

This is the recommended approach if you want to use a specific model checkpoint or fine-tuned weights.

### Step 1: Install Dependencies

```bash
pip install torch torchvision coremltools onnx
```

### Step 2: PyTorch → ONNX Conversion Script

Create `convert_to_onnx.py`:

```python
import torch
import torchvision
import os

def convert_efficientnet_to_onnx(output_path="efficientnet_b0.onnx"):
    """
    Convert pre-trained EfficientNet-B0 from torchvision to ONNX format.
    """
    print("📥 Loading pre-trained EfficientNet-B0...")
    
    # Load pre-trained model
    model = torchvision.models.efficientnet_b0(
        weights=torchvision.models.EfficientNet_B0_Weights.IMAGENET1K_V1
    )
    model.eval()
    
    # Create dummy input (batch_size=1, channels=3, height=224, width=224)
    dummy_input = torch.randn(1, 3, 224, 224)
    
    # Export to ONNX
    print(f"🔄 Converting to ONNX: {output_path}")
    torch.onnx.export(
        model,
        dummy_input,
        output_path,
        export_params=True,
        opset_version=11,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={
            'input': {0: 'batch_size'},
            'output': {0: 'batch_size'}
        }
    )
    
    print(f"✅ ONNX model saved to: {output_path}")
    
    # Verify the model
    import onnx
    onnx_model = onnx.load(output_path)
    onnx.checker.check_model(onnx_model)
    print("✅ ONNX model validation passed!")
    
    return output_path

if __name__ == "__main__":
    convert_efficientnet_to_onnx()
```

### Step 3: Run Conversion

```bash
python convert_to_onnx.py

# Move to project
mkdir -p composeApp/src/androidMain/assets/model/
mv efficientnet_b0.onnx composeApp/src/androidMain/assets/model/
```

---

## Option 3: ONNX → Core ML (.mlmodelc) for iOS

### Step 1: Install Core ML Tools

```bash
pip install coremltools onnx
```

### Step 2: Conversion Script

Create `convert_to_coreml.py`:

```python
import coremltools as ct
import os

def convert_onnx_to_coreml(onnx_path="efficientnet_b0.onnx", 
                           output_path="EfficientNetB0"):
    """
    Convert ONNX model to Apple Core ML compiled format (.mlmodelc).
    """
    print(f"📥 Loading ONNX model: {onnx_path}")
    
    # Load ONNX model
    onnx_model = ct.converters.onnx.convert(onnx_path)
    
    # Set model metadata
    onnx_model.author = "Fresco Team"
    onnx_model.license = "MIT"
    onnx_model.short_description = "EfficientNet-B0 for fruit and vegetable classification"
    
    # Set input/output descriptions
    onnx_model.input_description['input'] = 'Input image (224x224 RGB)'
    onnx_model.output_description['output'] = 'Classification probabilities'
    
    # Save as .mlmodel
    mlmodel_path = f"{output_path}.mlmodel"
    print(f"🔄 Converting to Core ML: {mlmodel_path}")
    onnx_model.save(mlmodel_path)
    
    # Compile to .mlmodelc (compiled format)
    print(f"🔨 Compiling to .mlmodelc...")
    compiled_path = f"{output_path}.mlmodelc"
    ct.models.MLModel.compileModel(mlmodel_path, output_path=compiled_path)
    
    print(f"✅ Core ML compiled model saved to: {compiled_path}/")
    
    return compiled_path

if __name__ == "__main__":
    convert_onnx_to_coreml()
```

### Step 3: Run Conversion

```bash
python convert_to_coreml.py
```

### Step 4: Add to Xcode Project

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Right-click on the project navigator → **Add Files to "iosApp"**
3. Select `EfficientNetB0.mlmodelc` folder
4. Ensure **Copy items if needed** is checked
5. Ensure the file is added to your app target

---

## Complete Conversion Pipeline (One Script)

Create `convert_model.py` for end-to-end conversion:

```python
#!/usr/bin/env python3
"""
Complete conversion pipeline: PyTorch → ONNX → Core ML (.mlmodelc)
For Fresco fruit and vegetable classifier
"""

import torch
import torchvision
import coremltools as ct
import onnx
import os
import shutil

# Configuration
MODEL_NAME = "efficientnet_b0"
INPUT_SIZE = 224
BATCH_SIZE = 1
CHANNELS = 3
OPSET_VERSION = 11

def pytorch_to_onnx(model, output_path):
    """Convert PyTorch model to ONNX format."""
    print("=" * 60)
    print("🔄 Step 1: PyTorch → ONNX")
    print("=" * 60)
    
    model.eval()
    dummy_input = torch.randn(BATCH_SIZE, CHANNELS, INPUT_SIZE, INPUT_SIZE)
    
    torch.onnx.export(
        model,
        dummy_input,
        output_path,
        export_params=True,
        opset_version=OPSET_VERSION,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={
            'input': {0: 'batch_size'},
            'output': {0: 'batch_size'}
        }
    )
    
    # Validate
    onnx_model = onnx.load(output_path)
    onnx.checker.check_model(onnx_model)
    print(f"✅ ONNX model saved and validated: {output_path}")

def onnx_to_coreml(onnx_path, output_name):
    """Convert ONNX model to Core ML compiled format."""
    print("\n" + "=" * 60)
    print("🔄 Step 2: ONNX → Core ML (.mlmodelc)")
    print("=" * 60)
    
    # Convert to Core ML
    coreml_model = ct.converters.onnx.convert(onnx_path)
    
    # Set metadata
    coreml_model.author = "Fresco Team"
    coreml_model.license = "MIT"
    coreml_model.short_description = "EfficientNet-B0 for fruit/vegetable classification"
    coreml_model.input_description['input'] = f'RGB image ({INPUT_SIZE}x{INPUT_SIZE})'
    coreml_model.output_description['output'] = 'Classification probabilities (1000 classes)'
    
    # Save .mlmodel
    mlmodel_path = f"{output_name}.mlmodel"
    coreml_model.save(mlmodel_path)
    print(f"✅ Core ML model saved: {mlmodel_path}")
    
    # Compile to .mlmodelc
    compiled_path = f"{output_name}.mlmodelc"
    if os.path.exists(compiled_path):
        shutil.rmtree(compiled_path)
    
    ct.models.MLModel.compileModel(mlmodel_path, output_path=compiled_path)
    print(f"✅ Core ML compiled model: {compiled_path}/")
    
    return compiled_path

def main():
    """Main conversion pipeline."""
    print("\n🚀 Fresco Model Conversion Pipeline")
    print("=" * 60)
    
    # Load pre-trained model
    print("\n📥 Loading EfficientNet-B0 (pre-trained on ImageNet)...")
    model = torchvision.models.efficientnet_b0(
        weights=torchvision.models.EfficientNet_B0_Weights.IMAGENET1K_V1
    )
    
    # Convert to ONNX
    onnx_path = f"{MODEL_NAME}.onnx"
    pytorch_to_onnx(model, onnx_path)
    
    # Convert to Core ML
    coreml_path = onnx_to_coreml(onnx_path, "EfficientNetB0")
    
    # Summary
    print("\n" + "=" * 60)
    print("✅ Conversion Complete!")
    print("=" * 60)
    print(f"\n📁 Generated files:")
    print(f"   • {onnx_path}")
    print(f"   • EfficientNetB0.mlmodel")
    print(f"   • EfficientNetB0.mlmodelc/")
    print(f"\n📱 Next steps:")
    print(f"   Android: Copy {onnx_path} to")
    print(f"            composeApp/src/androidMain/assets/model/")
    print(f"   iOS:     Add EfficientNetB0.mlmodelc to Xcode project")
    print(f"            via Add Files → Ensure it's in app bundle\n")

if __name__ == "__main__":
    main()
```

### Run Complete Pipeline

```bash
# Install dependencies
pip install torch torchvision coremltools onnx

# Run conversion
python convert_model.py
```

---

## Alternative Models

If you want to try different architectures:

### ResNet-50

```python
# PyTorch
model = torchvision.models.resnet50(
    weights=torchvision.models.ResNet50_Weights.IMAGENET1K_V1
)

# Hugging Face
optimum-cli export onnx \
  --model microsoft/resnet-50 \
  --task image-classification \
  ./resnet50_onnx/
```

### MobileNetV3 (Smaller, Faster)

```python
model = torchvision.models.mobilenet_v3_small(
    weights=torchvision.models.MobileNet_V3_Small_Weights.IMAGENET1K_V1
)
```

### ConvNeXt

```python
model = torchvision.models.convnext_tiny(
    weights=torchvision.models.ConvNeXt_Tiny_Weights.IMAGENET1K_V1
)
```

---

## Model Comparison

| Model | Parameters | Size (ONNX) | Inference Speed | Accuracy (ImageNet) |
|---|---|---|---|---|
| **EfficientNet-B0** | 5.3M | ~20 MB | Fast | 77.1% |
| ResNet-50 | 25.6M | ~98 MB | Medium | 80.9% |
| MobileNetV3-Small | 2.5M | ~10 MB | Very Fast | 67.4% |
| ConvNeXt-Tiny | 28.6M | ~110 MB | Medium | 82.1% |

---

## Testing Your Model

### Verify ONNX Model

```python
import onnxruntime as ort
import numpy as np

# Load model
session = ort.InferenceSession("efficientnet_b0.onnx")

# Test with random input
input_data = np.random.randn(1, 3, 224, 224).astype(np.float32)
input_name = session.get_inputs()[0].name

# Run inference
outputs = session.run(None, {input_name: input_data})
print(f"Output shape: {outputs[0].shape}")
print(f"Top 5 predictions: {np.argsort(outputs[0][0])[-5:][::-1]}")
```

### Verify Core ML Model

```python
import coremltools as ct
import numpy as np

# Load compiled model
model = ct.models.MLModel("EfficientNetB0.mlmodelc")

# Test with random input
input_data = {model.input_description['input']: 
              np.random.randn(1, 3, 224, 224).astype(np.float32)}

# Run prediction
output = model.predict(input_data)
print(f"Output keys: {output.keys()}")
```

---

## Troubleshooting

### ONNX Export Errors

**Problem:** `RuntimeError: Failed to export an ONNX attribute`

**Solution:** Update opset version:
```python
torch.onnx.export(..., opset_version=13)  # Try 12, 13, or 14
```

### CoreML Conversion Errors

**Problem:** `RuntimeError: Unsupported ONNX opset`

**Solution:** Use a lower opset during ONNX export:
```python
torch.onnx.export(..., opset_version=11)  # Core ML prefers 11-13
```

### Model Size Too Large

**Problem:** ONNX file is >50MB

**Solution:** Enable optimization:
```python
# Install onnx-simplifier
pip install onnx-simplifier

# Simplify model
python -m onnxsim efficientnet_b0.onnx efficientnet_b0_simple.onnx
```

### iOS Bundle Issues

**Problem:** `Model 'EfficientNetB0.mlmodelc' not found in app bundle`

**Solution:** 
1. In Xcode, verify the file is in **Build Phases → Copy Bundle Resources**
2. Clean build folder: `Product → Clean Build Folder`
3. Verify file is added to correct target

---

## Performance Tips

1. **Quantization** (reduces model size):
```python
# ONNX quantization
from onnxruntime.quantization import quantize_dynamic
quantize_dynamic("efficientnet_b0.onnx", 
                 "efficientnet_b0_quantized.onnx")
```

2. **Core ML Optimization**:
```python
# During conversion
coreml_model = ct.convert(
    onnx_path,
    compute_units=ct.ComputeUnit.CPU_AND_GPU,  # Use GPU if available
    minimum_deployment_target=ct.target.iOS16
)
```

3. **Input Preprocessing**: Ensure input normalization matches training:
```python
# ImageNet normalization
mean = [0.485, 0.456, 0.406]
std = [0.229, 0.224, 0.225]
```

---

## References

- [PyTorch EfficientNet Docs](https://pytorch.org/vision/stable/models/efficientnet.html)
- [ONNX Export Tutorial](https://pytorch.org/tutorials/advanced/super_resolution_with_onnxruntime.html)
- [Core ML Tools Docs](https://coremltools.readme.io/docs)
- [Hugging Face Optimum](https://huggingface.co/docs/optimum/index)
- [ONNX Model Zoo](https://github.com/onnx/models)
- [Apple Core ML Documentation](https://developer.apple.com/documentation/coreml)

---

## License

This guide is part of the Fresco project (MIT License).
