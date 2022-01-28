/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.slu.mongoEntity.rdf;

import net.sf.json.JSONObject;

/**
 * Sub entity of canvas otherContent.
 * @author hanyan
 */
public class TransLine {
    private String objectId;
    private String canvasObjectId;
    private String type;
    private String motivation;
    //In original JSON, the position parameters are in "on" property. This is the format: 
    //"on": "WebService#xywh=339,511,3325,215"
    private Integer x;
    private Integer y;
    private Integer w;
    private Integer h;
    
    public TransLine() {
    }

    public TransLine(String objectId, String canvasObjectId, String type, String motivation, Integer x, Integer y, Integer w, Integer h) {
        this.objectId = objectId;
        this.canvasObjectId = canvasObjectId;
        this.type = type;
        this.motivation = motivation;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        jo.element("_id", this.objectId);
        jo.element("@id", this.canvasObjectId);
        jo.element("@type", this.type);
        jo.element("motivation", this.motivation);
        jo.element("on", "WebService#xywh=" + this.x + "," + this.y + "," + this.w + "," + this.h);
        return jo;
    }


    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the motivation
     */
    public String getMotivation() {
        return motivation;
    }

    /**
     * @param motivation the motivation to set
     */
    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    /**
     * @return the x
     */
    public Integer getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(Integer x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public Integer getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(Integer y) {
        this.y = y;
    }

    /**
     * @return the w
     */
    public Integer getW() {
        return w;
    }

    /**
     * @param w the w to set
     */
    public void setW(Integer w) {
        this.w = w;
    }

    /**
     * @return the h
     */
    public Integer getH() {
        return h;
    }

    /**
     * @param h the h to set
     */
    public void setH(Integer h) {
        this.h = h;
    }

    /**
     * @return the objectId
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @param objectId the objectId to set
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * @return the canvasObjectId
     */
    public String getCanvasObjectId() {
        return canvasObjectId;
    }

    /**
     * @param canvasObjectId the canvasObjectId to set
     */
    public void setCanvasObjectId(String canvasObjectId) {
        this.canvasObjectId = canvasObjectId;
    }
}
