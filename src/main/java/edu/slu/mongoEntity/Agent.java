/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.mongoEntity;

import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;

/**
 *
 * @author hanyan
 */
public class Agent {
    private String objectID;
    private String aID;
    private String mbox;//personal mail box
    private String mbox_sha1sum;
    private String type = "Agent";
    private Long created;
    private Long modified;
    private JSONArray group;
    private String personID;//collection person's objectID
    private JSONArray organization;

    public Agent() {
    }

    public Agent(String objectID, String aID, String mbox, String mbox_sha1sum, Long created, Long modified, JSONArray group, String personID, JSONArray organization) {
        this.objectID = objectID;
        this.aID = aID;
        this.mbox = mbox;
        this.mbox_sha1sum = mbox_sha1sum;
        this.created = created;
        this.modified = modified;
        this.group = group;
        this.personID = personID;
        this.organization = organization;
    }
    
    public Agent(BasicDBObject dbo){
        this.objectID = dbo.getObjectId("_id").toString();
        this.aID = dbo.getString("@id");
        this.mbox = dbo.getString("mbox");
        this.mbox_sha1sum = dbo.getString("mbox_sha1sum");
        this.type = dbo.getString("type");
        this.created = dbo.getLong("created");
        this.modified = dbo.getLong("modified");
        this.group = JSONArray.fromObject(dbo.get("group"));
        this.personID = dbo.getString("personID");
        this.organization = JSONArray.fromObject(dbo.get("organization"));
    }
    
    public Map<String, Object> toMap(){
        Map<String, Object> m = new HashMap();
        if(null != this.objectID){
            m.put("_id", new ObjectId(this.objectID));
        }
        if(null != this.aID){
            m.put("@id", this.aID);
        }
        if(null != this.mbox){
            m.put("mbox", this.mbox);
        }
        if(null != this.mbox_sha1sum){
            m.put("mbox_sha1sum", this.mbox_sha1sum);
        }
        if(null != this.type){
            m.put("type", this.type);
        }
        if(null != this.created){
            m.put("created", this.created);
        }
        if(null != this.modified){
            m.put("modified", this.modified);
        }
        if(null != this.group){
            m.put("group", this.group);
        }
        if(null != this.personID){
            m.put("personID", this.personID);
        }
        if(null != this.organization){
            m.put("organization", this.organization);
        }
        return m;
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
     * @return the aID
     */
    public String getaID() {
        return aID;
    }

    /**
     * @param aID the aID to set
     */
    public void setaID(String aID) {
        this.aID = aID;
    }

    /**
     * @return the mbox
     */
    public String getMbox() {
        return mbox;
    }

    /**
     * @param mbox the mbox to set
     */
    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    /**
     * @return the mbox_sha1sum
     */
    public String getMbox_sha1sum() {
        return mbox_sha1sum;
    }

    /**
     * @param mbox_sha1sum the mbox_sha1sum to set
     */
    public void setMbox_sha1sum(String mbox_sha1sum) {
        this.mbox_sha1sum = mbox_sha1sum;
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
     * @return the created
     */
    public Long getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    /**
     * @return the modified
     */
    public Long getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(Long modified) {
        this.modified = modified;
    }

    /**
     * @return the group
     */
    public JSONArray getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(JSONArray group) {
        this.group = group;
    }

    /**
     * @return the organization
     */
    public JSONArray getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(JSONArray organization) {
        this.organization = organization;
    }

    /**
     * @return the personID
     */
    public String getPersonID() {
        return personID;
    }

    /**
     * @param personID the personID to set
     */
    public void setPersonID(String personID) {
        this.personID = personID;
    }

}
