/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.mongoEntity;

import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 *
 * @author hanyan
 */
public class Person {
    private String objectID;
    private String email;
    private String surname;
    private String familyname;
    private String pwd;
    private String aID;
    private Long dateCreated;
    private Long dateUpdated;

    public Person() {}

    public Person(String objectID, String email, String surname, String familyname, String pwd, String aID, Long dateCreated, Long dateUpdated) {
        this.objectID = objectID;
        this.email = email;
        this.surname = surname;
        this.familyname = familyname;
        this.pwd = pwd;
        this.aID = aID;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }
    
    public Person(BasicDBObject dbo) {
        this.objectID = dbo.getObjectId("_id").toString();
        this.email = dbo.getString("email");
        this.surname = dbo.getString("surname");
        this.familyname = dbo.getString("familyname");
        this.pwd = dbo.getString("pwd");
        this.aID = dbo.getString("@id");
        this.dateCreated = dbo.getLong("date_created");
        this.dateUpdated = dbo.getLong("date_updated");
    }
    
    public Map<String, Object> toMap(){
        Map<String, Object> m = new HashMap();
        m.put("_id", new ObjectId(this.objectID));
        if(null != this.email){
            m.put("email", this.email);
        }
        if(null != this.surname){
            m.put("surname", this.surname);
        }
        if(null != this.familyname){
            m.put("familyname", this.familyname);
        }
        if(null != this.pwd){
            m.put("pwd", this.pwd);
        }
        if(null != this.aID){
            m.put("@id", this.aID);
        }
        if(null != this.dateCreated){
            m.put("date_created", this.dateCreated);
        }
        if(null != this.dateUpdated){
            m.put("date_updated", this.dateUpdated);
        }
        return m;
    }
    
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
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
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the familyname
     */
    public String getFamilyname() {
        return familyname;
    }

    /**
     * @param familyname the familyname to set
     */
    public void setFamilyname(String familyname) {
        this.familyname = familyname;
    }

}
