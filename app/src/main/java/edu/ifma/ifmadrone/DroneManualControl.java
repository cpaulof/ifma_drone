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

    private boolean isMoving = false;
    private boolean isDetecting = false;

    private int gestureAction = 0;
    private boolean gestureActionActive = false;

    private boolean isDancing = false;
    private int dacingCommandsSentCount = 0;
    private int dacingDirection = 1;

    private boolean canTakeControl = true;

    DroneManualControl(){
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        flightController = aircraft.getFlightController();

        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);


    }

    public void setDancing(boolean t){
        isDancing = t;
        setNoMovement();
        startSendDataTaskTimer();
    }

    public void setDetecting(boolean t){
        setNoMovement();
        isDetecting = t;
    }

    public void setGestureActionActive(boolean t){
        setNoMovement();
        gestureActionActive = t;
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
        canTakeControl = true;
        if(flightController!=null){
            flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    //
                }
            });
        }
    }

    public void updateGestures(float[] r){
        int leftWrist = 27;
        int rightWrist = 30;
        int nose = 0;

        float yLeftWrist = r[leftWrist+1];
        float leftWristScore = r[leftWrist+2];

        float yRightWrist = r[rightWrist+1];
        float rightWristScore = r[rightWrist+2];

        float yNose = r[nose+1];
        float noseScore = r[nose+2];

        // Gesture Action
        // 0 -> Nada
        // 1 -> Subir
        // 2 -> Descer
        if(noseScore<0.5 || rightWristScore < 0.5 || leftWristScore < 0.5){
            gestureAction = 0;
            return;
        }
        if(yLeftWrist > yNose && yRightWrist < yNose)
            gestureAction = 2;
        else if(yRightWrist > yNose && yLeftWrist < yNose)
            gestureAction = 1;
        else
            gestureAction = 0;
    }

    public void disableVirtualStick(){
        canTakeControl = false;
        setNoMovement();
        if(flightController!=null){
            flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    //
                }
            });
        }
    }

    private void setNoMovement(){
        pitch = .0f;
        yaw = .0f;
        //throttle = .0f;
        roll = .0f;
    }

    public void calcMovement(float x, float y, float score){
        setNoMovement();
        if(score < 0.5 || !isDetecting) {
         return;
        }

        float xOffset = x - 0.5f;
        float yOffset = Math.abs(y - 0.5f);

        if(xOffset > 0.04)
            roll = 0.2f;
        else if(xOffset < -0.04)
            roll = -0.2f;

        startSendDataTaskTimer();
    }

    private void startSendDataTaskTimer(){
        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 100);
        }
    }

    public String debugGetAttrs(){
        return "Pitch:"+pitch+" Yaw:"+yaw+" Throttle:"+throttle+" Roll:"+roll+" Dacing:"+
                isDancing+" D:"+dacingDirection+" Count:"+dacingCommandsSentCount+
                " Gesto:"+gestureAction;
    }

    public void testRoll(){
        pitch = .0f;
        yaw = .0f;
        throttle = .2f;
        roll = .0f;

        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 100);
        }
    }

    private class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            if (flightController != null && canTakeControl) {


                //danca
                if(isDancing){
                    if(dacingCommandsSentCount == 5){
                        dacingCommandsSentCount = 0;
                        dacingDirection = dacingDirection > 0? -1: 1;
                    }
                    throttle = 0.2f * dacingDirection;
                    dacingCommandsSentCount++;
                } else throttle = 0.0f;

                //gestos

                if(gestureActionActive){
                    if(gestureAction == 1)
                        throttle = 0.2f;
                    else if(gestureAction == 2)
                        throttle = -0.2f;
                    else
                        throttle = 0;
                }
                else throttle = 0;


                flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            //task.cancel();
                        }
                    }
                });
            }
        }
    }



}
