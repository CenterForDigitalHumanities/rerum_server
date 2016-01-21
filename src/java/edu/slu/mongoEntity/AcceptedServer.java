/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.mongoEntity;

import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author hanyan
 */
public class AcceptedServer {
    private String objectID;
    private String name;
    private String ip;
    private String contact;

    public AcceptedServer() {
    }

    public AcceptedServer(String objectID, String name, String ip, String contact) {
        this.objectID = objectID;
        this.name = name;
        this.ip = ip;
        this.contact = contact;
    }
    
    public AcceptedServer(BasicDBObject dbo) {
        this.objectID = dbo.getObjectId("_id").toString();
        this.name = dbo.getString("name");
        this.ip = dbo.getString("ip");
        this.contact = dbo.getString("contact");
    }
    
    public Map<String, Object> toMap(){
        Map<String, Object> m = new HashMap();
        if(null != this.objectID){
            m.put("objectID", this.objectID);
        }
        m.put("name", this.name);
        m.put("ip", this.ip);
        m.put("contact", this.contact);
        return m;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.objectID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AcceptedServer other = (AcceptedServer) obj;
        if (!Objects.equals(this.objectID, other.objectID)) {
            return false;
        }
        return true;
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
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

}
