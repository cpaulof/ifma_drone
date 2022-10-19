package edu.ifma.ifmadrone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;
import dji.midware.usb.P3.UsbAccessoryService;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import edu.ifma.tfmodel.Movenet;


public class VideoFeedActivity extends AppCompatActivity {
    private TextureView videoFeedView;
    DJICodecManager codecManager;
    VideoFeeder.VideoDataListener videoDataListener;
    Movenet model = null;
    TextView resultLabel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);
        videoFeedView = findViewById(R.id.textureView);
        resultLabel = findViewById(R.id.resultLabel);

        try {
            model = Movenet.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();


    }

    private void init(){
        Context context = this;
        videoFeedView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){

            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                if(codecManager== null){
                    codecManager = new DJICodecManager(
                            context,
                            surfaceTexture,
                            i,
                            i1,
                            UsbAccessoryService.VideoStreamSource.Camera);

                }

            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                if(model!=null){
                    TensorImage tensorImage = TensorImage.fromBitmap(videoFeedView.getBitmap());
                    Movenet.Outputs results = model.process(tensorImage);
                    float[] r = results.getKeypointsAsTensorBuffer().getFloatArray();
                    float nose_x = r[0];
                    float nose_y = r[1];
                    float score = r[2];
                    resultLabel.setText("X:"+nose_x+" Y:"+nose_y+" Score:"+score);
                }

            }
        });

        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                //lastReceivedFrameTime.set(System.currentTimeMillis());

                if (codecManager != null) {
                    codecManager.sendDataToDecoder(videoBuffer,
                            size,
                            UsbAccessoryService.VideoStreamSource.Camera.getIndex());
                }
            }
        };
        VideoFeeder.VideoFeed videoFeed = VideoFeeder.getInstance().getPrimaryVideoFeed();
        videoFeed.addVideoDataListener(videoDataListener);
    }

}