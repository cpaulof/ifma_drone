package edu.ifma.ifmadrone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
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
    TextView resultLabel2;

    Button btnDance;
    Button btnDetect;
    Button btnGesture;

    private boolean isDancing = false;
    private boolean isDetecting = false;
    private boolean isGesture = false;

//    Button btnOne;
//    Button btnTwo;
//    Button btnThree;

    private DroneManualControl droneManualControl = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);
        videoFeedView = findViewById(R.id.textureView);
        resultLabel = findViewById(R.id.resultLabel);
        resultLabel2 = findViewById(R.id.resultLabel2);

        btnDance = findViewById(R.id.btnDance);
        btnDetect = findViewById(R.id.btnDetect);
        btnGesture = findViewById(R.id.btnGesture);

//        btnOne = findViewById(R.id.btnOne);
//        btnTwo = findViewById(R.id.btnTwo);
//        btnThree = findViewById(R.id.btnThree);

        if(droneManualControl == null){
            droneManualControl = new DroneManualControl();
            resultLabel2.setText("Inicado drone manual control");
        }

        try {
            model = Movenet.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();
    }

    public void toggleDetecting(View view) {
        isDetecting = !isDetecting;
        if(droneManualControl!=null){
            droneManualControl.setDetecting(isDetecting);
        }
        btnDetect.setText(isDetecting?"Parar": "Detectar");
    }

    public void toggleGesture(View view){
        isGesture = !isGesture;
        if(droneManualControl!=null){
            droneManualControl.setGestureActionActive(isGesture);
        }
        btnGesture.setText(isGesture?"Parar": "Gestos");
    }

    public void toggleDance(View view){
        isDancing = !isDancing;

        if(isDancing){
            btnDance.setText("Parar");
            if(droneManualControl!=null){
                droneManualControl.setDancing(true);
            }
        }
        else{
            btnDance.setText("Dancar");
            droneManualControl.setDancing(false);
        }
    }

    public void ativar(View view){
        if(droneManualControl!=null){
            droneManualControl.enableVirtualStick();
        }
    }

    public void desativar(View view){
        if(droneManualControl!=null){
            droneManualControl.disableVirtualStick();
        }
    }

    public void takeOff(View view){
        if(droneManualControl!=null){
            droneManualControl.takeOff();

        }
    }

    public void land(View view){
        if(droneManualControl!=null){
            droneManualControl.testRoll();
        }
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
                    float nose_y = r[0];
                    float nose_x = r[1];
                    float score  = r[2];


                    resultLabel.setText("X:"+nose_x+" Y:"+nose_y+" Score:"+score);

                    if(droneManualControl!=null){
                        droneManualControl.calcMovement(nose_x, nose_y, score);
                        resultLabel2.setText(droneManualControl.debugGetAttrs());
                        droneManualControl.updateGestures(r);
                    }
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