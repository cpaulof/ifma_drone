# Movenet Usage

```
import edu.ifma.tfmodel.Movenet;

// 1. Initialize the Model
Movenet model = null;

try {
    model = Movenet.newInstance(context);  // android.content.Context
} catch (IOException e) {
    e.printStackTrace();
}

if (model != null) {

    // 2. Set the inputs
    // Prepare tensor "image" from a Bitmap with ARGB_8888 format.
    Bitmap bitmap = ...;
    TensorImage image = TensorImage.fromBitmap(bitmap);
    // Alternatively, load the input tensor "image" from pixel values.
    // Check out TensorImage documentation to load other image data structures.
    // int[] pixelValues = ...;
    // int[] shape = ...;
    // TensorImage image = new TensorImage();
    // image.load(pixelValues, shape);

    // 3. Run the model
    Movenet.Outputs outputs = model.process(image);

    // 4. Retrieve the results
    TensorBuffer keypoints = outputs.getKeypointsAsTensorBuffer();
}
```
