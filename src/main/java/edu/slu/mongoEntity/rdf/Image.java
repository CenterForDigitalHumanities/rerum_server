/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.slu.mongoEntity.rdf;

import net.sf.json.JSONObject;

/**
 * Sub entity of canvas. Existing in canvas as an array
 * @author hanyan
 */
public class Image {
    private String id;
    private String type;
    private String motivation;
    private JSONObject jo_resource;

    public Image() {
    }

    public Image(String id, String type, String motivation, JSONObject jo_resource) {
        this.id = id;
        this.type = type;
        this.motivation = motivation;
        this.jo_resource = jo_resource;
    }
    
    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        jo.element("@id", this.id);
        jo.element("@type", this.type);
        jo.element("motivation", this.motivation);
        jo.element("resource", jo_resource);
        return jo;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @return the jo_resource
     */
    public JSONObject getJo_resource() {
        return jo_resource;
    }

    /**
     * @param jo_resource the jo_resource to set
     */
    public void setJo_resource(JSONObject jo_resource) {
        this.jo_resource = jo_resource;
    }


}
