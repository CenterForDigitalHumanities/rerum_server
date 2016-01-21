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
public class Annotation_old {
    private String objectID;
    //Namespace of user from client. e.g. "tpen/pcuba" tpen is the server name, pcuba is the username on that server. 
    //annotation server trusts servers, so it doesn't store any password. 
    private String namespace;
    //Content of annotation. In String format. e.g. "aaaaa"
    private String content;
    //All selectors are in String format. e.g. "x=0,y=100,w=200,h=300"
    private String selector;
    
    private String title;
    
    //To be annotationed object
    private String resource;
    //could be url, object, image, ...
    private String resourceType;
    
    //The annotation realted source outside of annotation store. e.g. if the annotation belongs to a project, then here is "projectID:2112". 
    //if the annotation belongs to a folio (in transcription), then here is "folio:205". 
    private String outterRelative;
    
    private Long addedTime;
    
    private String fontColor;
    private String fontType;
    
    private Integer permission;//use permission constant
    
    //version
    //versionID is the objectID of first version (version 0) of an annotation. If this is the first version, it's empty String.
    private String originalAnnoID;
    //numbers that record version. 0-infinity
    private Integer versionNum;
    //fork
    //the original annotation ID (forked annotation ID)
    private String forkFromID;
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Map<String, Object> m_anno = toMap();
        for(Map.Entry<String, Object> anno : m_anno.entrySet()){
            if(null != anno.getValue()){
                sb.append("annotation." + anno.getKey() + "=" + anno.getValue() + "&");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf("&"));
        return sb.toString();
    }
    
    public Annotation_old() {
    }

    public Annotation_old(String objectID, String namespace, String content, String selector, String title, String resource, String resourceType,
            String outterRelative, Long addedTime, String fontColor, String fontType, Integer permission, String originalAnnoID, Integer versionNum, String forkFromID) {
        this.objectID = objectID;
        this.namespace = namespace;
        this.content = content;
        this.selector = selector;
        this.title = title;
        this.resource = resource;
        this.resourceType = resourceType;
        this.outterRelative = outterRelative;
        this.addedTime = addedTime;
        this.fontColor = fontColor;
        this.fontType = fontType;
        this.permission = permission;
        this.originalAnnoID = originalAnnoID;
        this.versionNum = versionNum;
        this.forkFromID = forkFromID;
    }
    
    public Annotation_old(BasicDBObject dbo) {
        this.objectID = dbo.getObjectId("_id").toString();
        this.namespace = dbo.getString("namespace");
        this.content = dbo.getString("content");
        this.selector = dbo.getString("selector");
        this.title = dbo.getString("title");
        this.resource = dbo.getString("resource");
        this.resourceType = dbo.getString("resourceType");
        this.outterRelative = dbo.getString("outterRelative");
        this.addedTime = dbo.getLong("addedTime");
        this.fontColor = dbo.getString("fontColor");
        this.fontType = dbo.getString("fontType");
        this.permission = dbo.getInt("permission");
        this.originalAnnoID = dbo.getString("originalAnnoID");
        this.versionNum = dbo.getInt("versionNum");
        this.forkFromID = dbo.getString("forkFromID");
    }
    
    public Map<String, Object> toMap(){
        Map<String, Object> m = new HashMap();
        if(null != this.objectID){
            m.put("_id", this.objectID);
        }
        m.put("namespace", this.namespace);
        m.put("content", this.content);
        m.put("selector", this.selector);
        m.put("title", this.title);
        m.put("resource", this.resource);
        m.put("resourceType", this.resourceType);
        m.put("outterRelative", this.outterRelative);
        m.put("addedTime", this.addedTime);
        m.put("fontColor", this.fontColor);
        m.put("fontType", this.fontType);
        m.put("permission", this.permission);
        m.put("originalAnnoID", this.originalAnnoID);
        m.put("versionNum", this.versionNum);
        m.put("forkFromID", this.forkFromID);
        return m;
    }
    
    /**
     * Update data except objectID, versionNum and forkFromID. Because these values cannot be changed when it is saved.  
     * @param a 
     */
    public void toUpdate(Annotation_old a) {
//        if(null != a.getNamespace() && !"".equals(a.getNamespace())){
//            this.namespace = a.getNamespace();
//        }
        if(null != a.getContent()){
            this.content = a.getContent();
        }
        if(null != a.getSelector()){
            this.selector = a.getSelector();
        }
        if(null != a.getTitle()){
            this.title = a.getTitle();
        }
        if(null != a.getResource()){
            this.resource = a.getResource();
        }
        if(null != a.getResourceType()){
            this.resourceType = a.getResourceType();
        }
//        if(null != a.getOutterRelative()){
//            this.outterRelative = a.getOutterRelative();
//        } no need to update
//        if(null != a.getAddedTime()){
//            this.addedTime = a.getAddedTime();
//        }
        if(null != a.getFontColor()){
            this.fontColor = a.getFontColor();
        }
        if(null != a.getFontType()){
            this.fontType = a.getFontType();
        }
        if(null != a.getPermission() && a.getPermission() < 4){
            this.permission = a.getPermission();
        }
//        this.originalAnnoID = a.getOriginalAnnoID(); no need to update
//        this.versionNum = versionNum; no need to update
//        this.forkFromID = forkFromID; no need to update
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.objectID);
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
        final Annotation_old other = (Annotation_old) obj;
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
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * @param selector the selector to set
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * @return the addedTime
     */
    public Long getAddedTime() {
        return addedTime;
    }

    /**
     * @param addedTime the addedTime to set
     */
    public void setAddedTime(Long addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * @return the fontColor
     */
    public String getFontColor() {
        return fontColor;
    }

    /**
     * @param fontColor the fontColor to set
     */
    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    /**
     * @return the fontType
     */
    public String getFontType() {
        return fontType;
    }

    /**
     * @param fontType the fontType to set
     */
    public void setFontType(String fontType) {
        this.fontType = fontType;
    }

    /**
     * @return the permission
     */
    public Integer getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(Integer permission) {
        this.permission = permission;
    }

    /**
     * @return the versionNum
     */
    public Integer getVersionNum() {
        return versionNum;
    }

    /**
     * @param versionNum the versionNum to set
     */
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    /**
     * @return the forkFromID
     */
    public String getForkFromID() {
        return forkFromID;
    }

    /**
     * @param forkFromID the forkFromID to set
     */
    public void setForkFromID(String forkFromID) {
        this.forkFromID = forkFromID;
    }

    /**
     * @return the outterRelative
     */
    public String getOutterRelative() {
        return outterRelative;
    }

    /**
     * @param outterRelative the outterRelative to set
     */
    public void setOutterRelative(String outterRelative) {
        this.outterRelative = outterRelative;
    }

    /**
     * @return the originalAnnoID
     */
    public String getOriginalAnnoID() {
        return originalAnnoID;
    }

    /**
     * @param originalAnnoID the originalAnnoID to set
     */
    public void setOriginalAnnoID(String originalAnnoID) {
        this.originalAnnoID = originalAnnoID;
    }
}
