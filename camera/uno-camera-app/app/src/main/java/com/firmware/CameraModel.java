package com.firmware;

/**
 * Created by CVision on 10/29/2015.
 */
public class CameraModel {

    private String modelId;
    private String nameModel;
    private boolean state;

    public CameraModel(){

    }

    public CameraModel(String nameModel, String modelId){
        this.modelId = modelId;
        this.nameModel = nameModel;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setNameModel(String nameModel) {
        this.nameModel = nameModel;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getModelId() {
        return modelId;
    }

    public String getNameModel() {
        return nameModel;
    }

    public boolean getState() {
        return state;
    }
}
