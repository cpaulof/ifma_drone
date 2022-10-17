package edu.ifma.ifmadrone;

public class TFModel {

    Object model;

    public void TFModel(String path){
        loadModel();
    }

    public void loadModel(){
        model = null;
    }

    public Box[] inference(float[] data){
        Box[] result = new Box[10];
        return result;
    }
}
