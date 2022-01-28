/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.slu.mongoEntity.rdf;

import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Sub entity of Canvas
 * @author hanyan
 */
public class OtherContent {
    private String objectId;
    private String canvasObjectId;
    private String type;
    private String context;
    //this is named as resources in OAI, I name it ls_transLine because it is more readable in programming. 
    private List<String> ls_transLineObjectId;

    public OtherContent() {
    }

    public OtherContent(String objectId, String canvasObjectId, String type, String context, List<String> ls_transLineObjectId) {
        this.objectId = objectId;
        this.canvasObjectId = canvasObjectId;
        this.type = type;
        this.context = context;
        this.ls_transLineObjectId = ls_transLineObjectId;
    }

    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        jo.element("_id", this.objectId);
        jo.element("@id", this.canvasObjectId);
        jo.element("@type", this.type);
        jo.element("context", this.context);
        JSONArray ja = new JSONArray();
        for(String oid : ls_transLineObjectId){
            ja.add(oid);
        }
        jo.element("resources", ja);
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
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
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

    /**
     * @return the ls_transLineObjectId
     */
    public List<String> getLs_transLineObjectId() {
        return ls_transLineObjectId;
    }

    /**
     * @param ls_transLineObjectId the ls_transLineObjectId to set
     */
    public void setLs_transLineObjectId(List<String> ls_transLineObjectId) {
        this.ls_transLineObjectId = ls_transLineObjectId;
    }

}
