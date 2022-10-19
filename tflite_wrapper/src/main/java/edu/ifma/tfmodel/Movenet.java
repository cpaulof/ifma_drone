// Generated by TFLite Support.
package edu.ifma.tfmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.Tensor.QuantizationParams;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.metadata.schema.NormalizationOptions;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

/** Wrapper class of model Movenet pose detector (Version: v1) */
public class Movenet {
  private final Metadata metadata;
  private final Model model;
  private static final String MODEL_NAME = "movenet_singlepose.tflite";
  private ImageProcessor imagePreprocessor;
  private TensorProcessor keypointsPostprocessor;

  /** Output wrapper of {@link Movenet} */
  public static class Outputs {
    private final TensorBuffer keypoints;
    private final TensorProcessor keypointsPostprocessor;

    public TensorBuffer getKeypointsAsTensorBuffer() {
      return postprocessKeypoints(keypoints);
    }

    Outputs(Metadata metadata, TensorProcessor keypointsPostprocessor) {
      keypoints = TensorBuffer.createFixedSize(metadata.getKeypointsShape(), metadata.getKeypointsType());
      this.keypointsPostprocessor = keypointsPostprocessor;
    }

    Map<Integer, Object> getBuffer() {
      Map<Integer, Object> outputs = new HashMap<>();
      outputs.put(0, keypoints.getBuffer());
      return outputs;
    }

    private TensorBuffer postprocessKeypoints(TensorBuffer tensorBuffer) {
      return keypointsPostprocessor.process(tensorBuffer);
    }
  }

  /** Metadata accessors of {@link Movenet} */
  public static class Metadata {
    private final int[] imageShape;
    private final DataType imageDataType;
    private final QuantizationParams imageQuantizationParams;
    private final float[] imageMean;
    private final float[] imageStddev;
    private final int[] keypointsShape;
    private final DataType keypointsDataType;
    private final QuantizationParams keypointsQuantizationParams;

    public Metadata(ByteBuffer buffer, Model model) throws IOException {
      MetadataExtractor extractor = new MetadataExtractor(buffer);
      Tensor imageTensor = model.getInputTensor(0);
      imageShape = imageTensor.shape();
      imageDataType = imageTensor.dataType();
      imageQuantizationParams = imageTensor.quantizationParams();
      NormalizationOptions imageNormalizationOptions =
          (NormalizationOptions) extractor.getInputTensorMetadata(0).processUnits(0).options(new NormalizationOptions());
      FloatBuffer imageMeanBuffer = imageNormalizationOptions.meanAsByteBuffer().asFloatBuffer();
      imageMean = new float[imageMeanBuffer.limit()];
      imageMeanBuffer.get(imageMean);
      FloatBuffer imageStddevBuffer = imageNormalizationOptions.stdAsByteBuffer().asFloatBuffer();
      imageStddev = new float[imageStddevBuffer.limit()];
      imageStddevBuffer.get(imageStddev);
      Tensor keypointsTensor = model.getOutputTensor(0);
      keypointsShape = keypointsTensor.shape();
      keypointsDataType = keypointsTensor.dataType();
      keypointsQuantizationParams = keypointsTensor.quantizationParams();
    }

    @SuppressLint("NewApi")
    public int[] getImageShape() {
      return Arrays.copyOf(imageShape, imageShape.length);
    }

    public DataType getImageType() {
      return imageDataType;
    }

    public QuantizationParams getImageQuantizationParams() {
      return imageQuantizationParams;
    }

    @SuppressLint("NewApi")
    public float[] getImageMean() {
      return Arrays.copyOf(imageMean, imageMean.length);
    }

    @SuppressLint("NewApi")
    public float[] getImageStddev() {
      return Arrays.copyOf(imageStddev, imageStddev.length);
    }

    @SuppressLint("NewApi")
    public int[] getKeypointsShape() {
      return Arrays.copyOf(keypointsShape, keypointsShape.length);
    }

    public DataType getKeypointsType() {
      return keypointsDataType;
    }

    public QuantizationParams getKeypointsQuantizationParams() {
      return keypointsQuantizationParams;
    }
  }

  public Metadata getMetadata() {
    return metadata;
  }

  /**
   * Creates interpreter and loads associated files if needed.
   *
   * @throws IOException if an I/O error occurs when loading the tflite model.
   */
  public static Movenet newInstance(Context context) throws IOException {
    return newInstance(context, MODEL_NAME, new Model.Options.Builder().build());
  }

  /**
   * Creates interpreter and loads associated files if needed, but loading another model in the same
   * input / output structure with the original one.
   *
   * @throws IOException if an I/O error occurs when loading the tflite model.
   */
  public static Movenet newInstance(Context context, String modelPath) throws IOException {
    return newInstance(context, modelPath, new Model.Options.Builder().build());
  }

  /**
   * Creates interpreter and loads associated files if needed, with running options configured.
   *
   * @throws IOException if an I/O error occurs when loading the tflite model.
   */
  public static Movenet newInstance(Context context, Model.Options runningOptions) throws IOException {
    return newInstance(context, MODEL_NAME, runningOptions);
  }

  /**
   * Creates interpreter for a user-specified model.
   *
   * @throws IOException if an I/O error occurs when loading the tflite model.
   */
  public static Movenet newInstance(Context context, String modelPath, Model.Options runningOptions) throws IOException {
    Model model = Model.createModel(context, modelPath, runningOptions);
    Metadata metadata = new Metadata(model.getData(), model);
    Movenet instance = new Movenet(model, metadata);
    instance.resetImagePreprocessor(
        instance.buildDefaultImagePreprocessor());
    instance.resetKeypointsPostprocessor(
        instance.buildDefaultKeypointsPostprocessor());
    return instance;
  }


  public void resetImagePreprocessor(ImageProcessor processor) {
    imagePreprocessor = processor;
  }

  public void resetKeypointsPostprocessor(TensorProcessor processor) {
    keypointsPostprocessor = processor;
  }

  /** Triggers the model. */
  public Outputs process(TensorImage image) {
    Outputs outputs = new Outputs(metadata, keypointsPostprocessor);
    Object[] inputBuffers = preprocessInputs(image);
    model.run(inputBuffers, outputs.getBuffer());
    return outputs;
  }

  /** Closes the model. */
  public void close() {
    model.close();
  }

  public Movenet(Model model, Metadata metadata) {
    this.model = model;
    this.metadata = metadata;
  }

  private ImageProcessor buildDefaultImagePreprocessor() {
    ImageProcessor.Builder builder = new ImageProcessor.Builder()
        .add(new ResizeOp(
            metadata.getImageShape()[1],
            metadata.getImageShape()[2],
            ResizeMethod.NEAREST_NEIGHBOR))
        .add(new NormalizeOp(metadata.getImageMean(), metadata.getImageStddev()))
        .add(new QuantizeOp(
            metadata.getImageQuantizationParams().getZeroPoint(),
            metadata.getImageQuantizationParams().getScale()))
        .add(new CastOp(metadata.getImageType()));
    return builder.build();
  }

  private TensorProcessor buildDefaultKeypointsPostprocessor() {
    TensorProcessor.Builder builder = new TensorProcessor.Builder()
        .add(new DequantizeOp(
            metadata.getKeypointsQuantizationParams().getZeroPoint(),
            metadata.getKeypointsQuantizationParams().getScale()));
    return builder.build();
  }

  private Object[] preprocessInputs(TensorImage image) {
    image = imagePreprocessor.process(image);
    return new Object[] {image.getBuffer()};
  }
}
