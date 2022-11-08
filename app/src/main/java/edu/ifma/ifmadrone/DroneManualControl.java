package edu.ifma.ifmadrone;

import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class DroneManualControl {
    private FlightController flightController;

    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;

    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;

    DroneManualControl(){
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        flightController = aircraft.getFlightController();

        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);


    }

    public void takeOff(){
        if(flightController!=null){
            flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    //text.setText(djiError.getDescription());
                }
            });
        }
    }

    public void enableVirtualStick(){
        if(flightController!=null){
            flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    //
                }
            });
        }
    }

    public void disableVirtualStick(){
        if(flightController!=null){
            flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    //
                }
            });
        }
    }

    public void calcMovement(float x, float y, float score){
        if(score < 0.4) return;

        float xOffset = x - 0.5f;
        float yOffset = Math.abs(y - 0.5f);

        pitch = .0f;
        yaw = .0f;
        throttle = .0f;
        roll = .0f;

        if(xOffset > 0.1)
            roll = 0.2f;
        else if(xOffset < -0.1)
            roll = -0.2f;

        if (null == sendVirtualStickDataTimer) {

            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 100);
        }
    }

    public void testRoll(){
        pitch = .0f;
        yaw = .0f;
        throttle = .0f;
        roll = .3f;

        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 100);
        }
    }

    private class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            // asd asd
                        }
                    }
                });
            }
        }
    }



}
