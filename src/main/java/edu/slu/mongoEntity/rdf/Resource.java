/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.slu.mongoEntity.rdf;

import net.sf.json.JSONObject;

/**
 * Sub entity of image. 
 * @author hanyan
 */
public class Resource {
    //the image I asked you to parse.jpg
    private String id;
    private String type;
    private String format;
    private Integer height;
    private Integer width;

    public Resource(String id, String type, String format, Integer height, Integer width) {
        this.id = id;
        this.type = type;
        this.format = format;
        this.height = height;
        this.width = width;
    }
    
    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        jo.element("@id", this.id);
        jo.element("@type", this.type);
        jo.element("format", this.format);
        jo.element("height", this.height);
        jo.element("width", this.width);
        return jo;
    }

    public Resource() {
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
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }
}
