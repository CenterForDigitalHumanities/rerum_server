/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.mongoEntity;

import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import org.bson.types.ObjectId;

/**
 *
 * @author hanyan
 */
public class ProjectUserProfile {
    private String objectID;
    private String userObjectID;
    private String alias;
    private List<String> ls_serverIP;
    private Long dateCreated;
    private Long dateUpdated;
    private String config;

    public ProjectUserProfile() {}

    public ProjectUserProfile(String objectID, String userObjectID, String alias, List<String> ls_serverIP, Long dateCreated, Long dateUpdated, String config) {
        this.userObjectID = userObjectID;
        this.alias = alias;
        this.ls_serverIP = ls_serverIP;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.config = config;
    }
    
    public ProjectUserProfile(BasicDBObject dbo) {
        this.objectID = dbo.getObjectId("_id").toString();
        if(null != dbo.getString("userObjectID")){
            this.objectID = dbo.getString("userObjectID");
        }
        if(null != dbo.getString("alias")){
            this.alias = dbo.getString("alias");
        }
        if(null != dbo.getString("server_ip")){
            JSONArray ja = (JSONArray) dbo.get("ls_serverIP");
            this.ls_serverIP = ja.subList(0, ja.size() - 1);
        }
        this.dateCreated = dbo.getLong("date_created");
        this.dateUpdated = dbo.getLong("date_updated");
        if(null != dbo.getString("config")){
            this.config = dbo.getString("config");
        }
    }

    public Map<String, Object> toMap(){
        Map<String, Object> m = new HashMap();
        m.put("_id", new ObjectId(this.objectID));
        if(null != this.userObjectID){
            m.put("userObjectID", this.userObjectID);
        }
        if(null != this.alias){
            m.put("alias", this.alias);
        }
        if(null != this.ls_serverIP){
            m.put("ls_server_ip", this.ls_serverIP);
        }
        if(null != this.dateCreated){
            m.put("date_created", this.dateCreated);
        }
        if(null != this.dateUpdated){
            m.put("date_updated", this.dateUpdated);
        }
        if(null != this.config){
            m.put("config", this.config);
        }
        return m;
    }
    
    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the dateCreated
     */
    public Long getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the dateUpdated
     */
    public Long getDateUpdated() {
        return dateUpdated;
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @return the config
     */
    public String getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * @return the userObjectID
     */
    public String getUserObjectID() {
        return userObjectID;
    }

    /**
     * @param userObjectID the userObjectID to set
     */
    public void setUserObjectID(String userObjectID) {
        this.userObjectID = userObjectID;
    }

    /**
     * @return the objectID
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * @param objectID the objectID to set
     */
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    /**
     * @return the ls_serverIP
     */
    public List<String> getLs_serverIP() {
        return ls_serverIP;
    }

    /**
     * @param ls_serverIP the ls_serverIP to set
     */
    public void setLs_serverIP(List<String> ls_serverIP) {
        this.ls_serverIP = ls_serverIP;
    }

}
