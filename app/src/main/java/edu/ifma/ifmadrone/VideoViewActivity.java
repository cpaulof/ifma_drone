package edu.ifma.ifmadrone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dji.common.airlink.PhysicalSource;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.sdkmanager.DJISDKManager;


public class VideoViewActivity extends AppCompatActivity {
    private BaseProduct product;
    private VideoFeeder.PhysicalSourceListener sourceListener;
    private VideoFeedView videoFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        initVideoFeed();
    }


    private void initVideoFeed(){
        videoFeed = findViewById(R.id.videoFeed);
//        product = DJISDKManager.getInstance().getProduct();
//        Camera cam = product.getCamera();
//        VideoFeeder videoFeeder = VideoFeeder.getInstance();
//        VideoFeeder.VideoFeed feed = videoFeeder.getPrimaryVideoFeed();



//        sourceListener = new VideoFeeder.PhysicalSourceListener() {
//            @Override
//            public void onChange(VideoFeeder.VideoFeed videoFeed, PhysicalSource newPhysicalSource) {
//            }
//        };

        VideoFeeder.VideoDataListener primaryVideoDataListener =
                videoFeed.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);

    }
}