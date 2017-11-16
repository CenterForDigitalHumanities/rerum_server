/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.action;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.opensymphony.xwork2.ActionSupport;
import edu.slu.common.Constant;
import edu.slu.mongoEntity.AcceptedServer;
import edu.slu.service.MongoDBService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.bson.types.ObjectId;

/**
 *
 * @author hanyan
 */
public class AnnotationAction extends ActionSupport implements ServletRequestAware, ServletResponseAware{
//    private Annotation_old annotation;
    private String content;
    private String oid;
    private AcceptedServer acceptedServer;
    
    
    private MongoDBService mongoDBService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder bodyString;
    private BufferedReader bodyReader;

    private PrintWriter out;
    
    /*
    * Request processor so that data doesn't have to be passed through the API like {contet:{}}
    * It is backwards compatible to support this behavior. 
    */
    public String processRequestBody(HttpServletRequest http_request) throws IOException, ServletException, Exception{
        String cType = http_request.getContentType();
        String requestBody;
        JSONObject test;
        JSONArray test2;
        
        System.out.println(System.getProperty("line.separator"));
        System.out.println(System.getProperty("line.separator"));
        System.out.println("Processing request...");
        System.out.println("Content at the top of processing is "+content);

        /* This means the type was application/x-www-form-urlencoded and they passed it like {content:{data}} so I already have content, just go forward using that.  This is backwards compatability */
        if(null != content && !content.equals("")){ 
            System.out.println("Content is already set, so return it");           
//            System.out.println(System.getProperty("line.separator"));
//            System.out.println(System.getProperty("line.separator"));          
            try{ //Try to parse as a JSONObject
                test = JSONObject.fromObject(content);
            }
            catch(Exception ex){ //was not a JSONObject but might be a JSONArray
                //System.out.println("Was not an object...");
                try{ //Try to parse as a JSONArray
                    test2 = JSONArray.fromObject(content);
                }
                catch(Exception ex2){ //Was not a JSONObject or a JSONArray.  Not valid JSON.  Throw error. 
                    throw new Exception("{error: 'The data passed was not valid JSON.'}");
                }
            }
            //System.out.println(content);
            return content;
        }
        if(cType.contains("application/x-www-form-urlencoded")){ //They passed this content type but did not follow the {content:{data}} format.
            //TODO: Throw improper request body error!!@@
            //System.out.println("application/x-www-form-urlencoded type not in proper {content:{data}} format ");
            requestBody = "{error: 'Improper request body.  Must use {content:{data}} format for content type application/x-www-form-urlencoded or instead use application/json or application/ld+json Content Type with valid JSON.'}";
            throw new Exception(requestBody);
        }
        else if(cType.contains("application/json") || cType.contains("application/ld+json")){
            //System.out.println("content was not set, check the request body...");
            //System.out.println(System.getProperty("line.separator"));
            bodyReader = http_request.getReader();
            bodyString = new StringBuilder();
            String line="";
            //System.out.println("See lines from the reader on the body...");
            while ((line = bodyReader.readLine()) != null)
            {
             //System.out.println("line is "+line);
              bodyString.append(line + "\n");
            }
           // System.out.println(System.getProperty("line.separator"));
            requestBody = bodyString.toString();
            try{ //Try to parse as a JSONObject
              test = JSONObject.fromObject(requestBody);
              }
              catch(Exception ex){ //was not a JSONObject but might be a JSONArray
                  //System.out.println("Was not an object...");
                  try{ //Try to parse as a JSONArray
                      test2 = JSONArray.fromObject(requestBody);
                  }
                  catch(Exception ex2){ //Was not a JSONObject or a JSONArray.  Not valid JSON.  Throw error. 
                      throw new Exception("{error: 'The data passed was not valid JSON.'}");
                  }
              }
//            System.out.println("See string built...");
//            System.out.println(requestBody);
//            System.out.println(System.getProperty("line.separator"));
//            System.out.println(System.getProperty("line.separator"));
            return requestBody;
        }
        else{ //I do not understand the content type being passed.
            //System.out.println("Weird content type.   ");
            requestBody = "{error: 'Improper request body.  Must use application/json or application/ld+json Content Type'}";
            throw new Exception(requestBody);
        }

    }
    
    /*
    *The batch save to intended to work with Broken Books, but coud be applied elsewhere.  This batch will use the save() mongo function instead of insert() to determine whether 
    to do an update() or insert() for each item in the batch.  
    
        The content is from an HTTP request posting in an array filled with annotations to copy.  
        
        @see MongoDBAbstractDAO.bulkSaveMetadataForm(String collectionName, BasicDBList entity_array);
        @see MongoDBAbstractDAO.bulkSetIDProperty(String collectionName, BasicDBObject[] entity_array);
    */
    
    public void batchSaveMetadataForm() throws UnsupportedEncodingException, IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){
            //System.out.println("Batch save!!!!!");
            JSONArray received_array = JSONArray.fromObject(content);
            //System.out.println(received_array);
            BasicDBObject serverQuery = new BasicDBObject();
            serverQuery.append("ip", request.getRemoteAddr());
//            System.out.println("333333333333333333333333333333");
            DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
//            System.out.println("444444444444444444444444444444");
            BasicDBObject asbdbo = (BasicDBObject) asdbo;
            for(int b=0; b<received_array.size(); b++){
                JSONObject received = received_array.getJSONObject(b);
                received.accumulate("addedTime", System.currentTimeMillis());
//            set the version to empty String
                received.accumulate("originalAnnoID", "");//set versionID for a new fork
                received.accumulate("version", 1);
                if(!received.containsKey("permission")){
                    received.accumulate("permission", Constant.PERMISSION_PRIVATE);
                }
                if(null == received.get("forkFromID") || "".equals(received.get("forkFromID"))){
                    received.accumulate("forkFromID", "");
                }  
                //received.accumulate("addedTime", System.currentTimeMillis());
//            set the version to empty String
                received.accumulate("originalAnnoID", "");//set versionID for a new fork
                received.accumulate("version", 1);
                if(!received.containsKey("permission")){
                    received.accumulate("permission", Constant.PERMISSION_PRIVATE);
                }
                if(null == received.get("forkFromID") || "".equals(received.get("forkFromID"))){
                    received.accumulate("forkFromID", "");
                }
                received.accumulate("serverName", asbdbo.get("name"));
                received.accumulate("serverIP", asbdbo.get("ip"));
                received_array.set(b,received);
            }
            
            //System.out.println(received_array);
            BasicDBList dbo = (BasicDBList) JSON.parse(received_array.toString());
            //System.out.println("Go into bulk save from batch save with this: ");
            //System.out.println(dbo);
            JSONArray reviewedResources = new JSONArray();
            //if the size is 0, no need to bulk save.  Nothing is there.
            //System.out.println("dbo size:  "+dbo.size());
            if(dbo.size() > 0){
                reviewedResources = mongoDBService.bulkSaveMetadataForm(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else{
             //   System.out.println("Skipping bulk save on account of empty array.");
            }
            //bulk save will automatically call bulk update so there is no real need to return these values.  We will for later use.
            JSONObject jo = new JSONObject();
//            System.out.println("cccccccccccccccccccccccccccccc");
            jo.element("code", HttpServletResponse.SC_CREATED);
//            System.out.println("dddddddddddddddddddddddddddddd");
            jo.element("reviewed_resources", reviewedResources);
            String locations = "";
            for(int j=0; j<reviewedResources.size(); j++){
                JSONObject getMyID = reviewedResources.getJSONObject(j);
                if(j == reviewedResources.size()-1){
                    locations += getMyID.getString("@id");
                }
                else{
                    locations += getMyID.getString("@id")+",";
                }
            }
            try {
                out = response.getWriter();
                response.addHeader("Location", locations);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
            jo.element("msg", "Didn't receive any data. ");
            try {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    /* 
        Each canvas has an annotation list with 0 - infinity annotations.  A copy requires a new annotation list with the copied annotations and a new @id.
        Mongo allows us to bulk save.  
    
    `   The content is from an HTTP request posting in an array filled with annotations to copy.  
        
        @see MongoDBAbstractDAO.bulkSaveFromCopy(String collectionName, BasicDBList entity_array);
        @see MongoDBAbstractDAO.bulkSetIDProperty(String collectionName, BasicDBObject[] entity_array);
        
    */ 
    public void batchSaveFromCopy() throws UnsupportedEncodingException, IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){
            //System.out.println("Batch save!!!!!");
            JSONArray received_array = JSONArray.fromObject(content);
            //System.out.println(received_array);
            BasicDBObject serverQuery = new BasicDBObject();
            serverQuery.append("ip", request.getRemoteAddr());
//            System.out.println("333333333333333333333333333333");
            DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
//            System.out.println("444444444444444444444444444444");
            BasicDBObject asbdbo = (BasicDBObject) asdbo;
            for(int b=0; b<received_array.size(); b++){
                JSONObject received = received_array.getJSONObject(b);
                received.accumulate("addedTime", System.currentTimeMillis());
//            set the version to empty String
                received.accumulate("originalAnnoID", "");//set versionID for a new fork
                received.accumulate("version", 1);
                if(!received.containsKey("permission")){
                    received.accumulate("permission", Constant.PERMISSION_PRIVATE);
                }
                if(null == received.get("forkFromID") || "".equals(received.get("forkFromID"))){
                    received.accumulate("forkFromID", "");
                }  
                //received.accumulate("addedTime", System.currentTimeMillis());
//            set the version to empty String
                received.accumulate("originalAnnoID", "");//set versionID for a new fork
                received.accumulate("version", 1);
                if(!received.containsKey("permission")){
                    received.accumulate("permission", Constant.PERMISSION_PRIVATE);
                }
                if(null == received.get("forkFromID") || "".equals(received.get("forkFromID"))){
                    received.accumulate("forkFromID", "");
                }
                received.accumulate("serverName", asbdbo.get("name"));
                received.accumulate("serverIP", asbdbo.get("ip"));
                received_array.set(b,received);
            }
            
            //System.out.println(received_array);
            BasicDBList dbo = (BasicDBList) JSON.parse(received_array.toString());
            //System.out.println("Go into bulk save from batch save with this: ");
            //System.out.println(dbo);
            JSONArray newResources = new JSONArray();
            //if the size is 0, no need to bulk save.  Nothing is there.
            //System.out.println("dbo size:  "+dbo.size());
            if(dbo.size() > 0){
                newResources = mongoDBService.bulkSaveFromCopy(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else{
             //   System.out.println("Skipping bulk save on account of empty array.");
            }
            //bulk save will automatically call bulk update so there is no real need to return these values.  We will for later use.
            JSONObject jo = new JSONObject();
//            System.out.println("cccccccccccccccccccccccccccccc");
            jo.element("code", HttpServletResponse.SC_CREATED);
//            System.out.println("dddddddddddddddddddddddddddddd");
            jo.element("new_resources", newResources);
            String locations = "";
            for(int j=0; j<newResources.size(); j++){
                JSONObject getMyID = newResources.getJSONObject(j);
                if(j == newResources.size()-1){
                    locations += getMyID.getString("@id");
                }
                else{
                    locations += getMyID.getString("@id")+",";
                }
            }
            try {
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.addHeader("Location", locations);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
            jo.element("msg", "Didn't receive any data. ");
            try {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get all my annotations. Include latest version of original annotations, all forks and revisions. 
     * @param annotation.content("namespace","...")
     */
    public void getAllMyAnnotations() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("namespace")){
            BasicDBObject query = new BasicDBObject();
            query.append("namespace", received.getString("namespace"));
            List<DBObject> ls_myAnno = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != ls_myAnno && ls_myAnno.size() > 0){
                JSONArray ja = new JSONArray();
                for(DBObject dbo : ls_myAnno){
                    BasicDBObject bdbo = (BasicDBObject) dbo;
                    ja.add(bdbo.toMap());
                }
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    out = response.getWriter();
                    out.print(ja);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                //JSONObject jo = new JSONObject();
                //jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                //try {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               //     out = response.getWriter();
               //     out.print(jo);
                //} catch (IOException ex) {
                //    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
        }else{
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
            jo.element("msg", "Could not find field 'namespace' in object.");
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.addHeader("Access-Control-Allow-Origin", "*");
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get all version of annotations by ObjectID. 
     * @param annotation.objectID
     */
    public void getAllVersionsOfAnnotationByObjectID() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("objectID")){
            //find one version by objectID
            BasicDBObject query = new BasicDBObject();
            query.append("_id", new ObjectId(received.getString("objectID")));
            DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != myAnno){
                //find the versionID
                BasicDBObject bMyAnno = (BasicDBObject) myAnno;
                //find by versionID to get all versions
                BasicDBObject queryOfAllVersion = new BasicDBObject();
                queryOfAllVersion.append("originalAnnoID", bMyAnno.getString("originalAnnoID"));
                List<DBObject> ls_versions = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, queryOfAllVersion);
                ls_versions.add(myAnno);//add the original annotation (because its orginalAnnoID is empty String)
                JSONArray ja = JSONArray.fromObject(ls_versions);
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(ja);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                //JSONObject jo = new JSONObject();
                //jo.accumulate("code", HttpServletResponse.SC_NOT_FOUND);
                //try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                 //   out = response.getWriter();
                //   out.print(jo);
                //} catch (IOException ex) {
                //    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
        }
        else{
          JSONObject jo = new JSONObject();
            jo.accumulate("code", HttpServletResponse.SC_BAD_REQUEST);
            jo.accumulate("msg", "Object did not contain key objectID.");
            try {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out = response.getWriter();
               out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
    }
    
    /**
     * Get annotation by objectiD
     * @param objectID (oid)
     * @return annotation object
     */
    public void getAnnotationByObjectID() throws IOException, ServletException, Exception{
        //content = processRequestBody(request);
        if(null != oid){
            //find one version by objectID
            BasicDBObject query = new BasicDBObject();
            query.append("_id", new ObjectId(oid));
            DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != myAnno){
                BasicDBObject bdbo = (BasicDBObject) myAnno;
                String oid = bdbo.getObjectId("_id").toString();
                JSONObject jo = JSONObject.fromObject(myAnno.toMap());
                jo.remove("_id");
                jo.accumulate("_id", oid);
                try {
                    response.addHeader("Content-Type", "application/json");
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                //JSONObject jo = new JSONObject();
                //jo.accumulate("code", HttpServletResponse.SC_NOT_FOUND);
                //try {
                //    response.addHeader("Content-Type", "application/json");
                //    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                //    out = response.getWriter();
               //     out.print(jo);
               // } catch (IOException ex) {
               //     Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
        }
        else{
            JSONObject jo = new JSONObject();
            jo.accumulate("code", HttpServletResponse.SC_NOT_FOUND);
            jo.accumulate("msg", "No object id provided");
            try {
                response.addHeader("Content-Type", "application/json");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get annotation by given properties. 
     * @param key->value pair(s)
     * @reutrn list of annotations that match the given conditions.
     */
    public void getAnnotationByProperties() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){
            JSONObject received = JSONObject.fromObject(content);
            BasicDBObject query = new BasicDBObject();
            Set<String> set_received = received.keySet();
            for(String key : set_received){
                query.append(key, received.get(key));
            }
            List<DBObject> ls_result = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            JSONArray ja = new JSONArray();
            for(DBObject dbo : ls_result){
                ja.add((BasicDBObject) dbo);
            }
            if(ls_result.size() > 0){
                try {
                    response.addHeader("Content-Type","application/json");
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(ja);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }else{
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_NO_CONTENT);
            jo.element("msg", "Didn't receive any data. ");
            try {
                response.addHeader("Content-Type","application/json");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save a new annotation. 
     * @param all annotation properties.
     */
    public void saveNewAnnotation() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){
            try{
//            System.out.println("1111111111111111111111111111");
                JSONObject received = JSONObject.fromObject(content);
    //            System.out.println("received ========= " + received);
                received.accumulate("addedTime", System.currentTimeMillis());
    //            set the version to empty String
                received.accumulate("originalAnnoID", "");//set versionID for a new fork
                received.accumulate("version", 1);
                if(!received.containsKey("permission")){
                    received.accumulate("permission", Constant.PERMISSION_PRIVATE);
                }
                if(null == received.get("forkFromID") || "".equals(received.get("forkFromID"))){
                    received.accumulate("forkFromID", "");
                }
    //            System.out.println("222222222222222222222222222222");
                BasicDBObject serverQuery = new BasicDBObject();
                serverQuery.append("ip", request.getRemoteAddr());
    //            System.out.println("333333333333333333333333333333");
                DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
    //            System.out.println("444444444444444444444444444444");
                BasicDBObject asbdbo = (BasicDBObject) asdbo;
    //            System.out.println("555555555555555555555555555555");
                received.accumulate("serverName", asbdbo.get("name"));
                received.accumulate("serverIP", asbdbo.get("ip"));
    //            System.out.println("666666666666666666666666666666");
                //create BasicDBObject
            
                DBObject dbo = (DBObject) JSON.parse(received.toString());
            
    //            System.out.println("777777777777777777777777777777======== " + dbo.toString());
                String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
    //            System.out.println("888888888888888888888888888888========== " + newObjectID);
                //set @id to objectID and update the annotation
                BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
    //            System.out.println("999999999999999999999999999999");
                String uid = "http://165.134.105.29/annotationstore/annotation/" + dboWithObjectID.getObjectId("_id").toString();
    //            System.out.println("000000000000000000000000000000");
                dboWithObjectID.append("@id", uid);
    //            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
    //            System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
                JSONObject jo = new JSONObject();
    //            System.out.println("cccccccccccccccccccccccccccccc");
                jo.element("code", HttpServletResponse.SC_CREATED);
    //            System.out.println("dddddddddddddddddddddddddddddd");
                jo.element("@id", uid);
    //            System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.addHeader("Location", uid);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out = response.getWriter();
                    out.print(jo);
                } 
                catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (Exception ex){ //could not parse JSON.
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }else{
            //JSONObject jo = new JSONObject();
            //jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
            //jo.element("msg", "Didn't receive any data. ");
            //try {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
           //     out = response.getWriter();
           //     out.print(jo);
            //} catch (IOException ex) {
            //    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }
    }
    
    /**
     * Update a given annotation. 
     * @param annotation.objectID
     * @param all annotation properties include updated properties. 
     */
    public void updateAnnotation() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        try{
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content);
            query.append("@id", received.getString("@id").trim());
            BasicDBObject result = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != result){
                Set<String> set_keys = received.keySet();
                for(String key : set_keys){
                    if(result.containsKey(key) 
                            && (!key.equals("@id") 
                                    || !key.equals("version") 
                                    || !key.equals("forkFromID")
                                    || !key.equals("originalAnnoID")
                                    || !key.equals("objectID"))){
                        result.remove(key);
                        result.append(key, received.get(key));
                    }
                }
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, query, result);
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_OK);
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }

            }else{
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch(Exception ex){ //could not parse JSON
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Save current annotation to a new version. 
     * @param objectID
     * @param any to be updated annotation properties. 
     */
    public void saveNewVersionOfAnnotation() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        try{
            BasicDBObject query = new BasicDBObject();
            JSONObject received = new JSONObject();
            received = JSONObject.fromObject(content);
            query.append("_id", received.getString("@id").trim());
            BasicDBObject result = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != result){
                BasicDBObject versionQuery = new BasicDBObject();
                versionQuery.append("originalAnnoID", result.get("originalAnnoID"));
                BasicDBObject orderby = new BasicDBObject();
                orderby.append("version", 1);
                List<DBObject> ls_count = mongoDBService.findByExampleWithOrder(Constant.COLLECTION_ANNOTATION, versionQuery, orderby);
                if(ls_count.size() >= 10){
                    //the upper limit of version number is 10, when the 11th comes in, it will delete the first one and put 11th as 10th. 
                    BasicDBObject first = (BasicDBObject) ls_count.get(0);
                    BasicDBObject last = (BasicDBObject) ls_count.get(ls_count.size() - 1);
                    //delete the 1st record.
                    mongoDBService.delete(Constant.COLLECTION_ANNOTATION, first);
                    int versionNum = last.getInt("version");
                    received.remove("version");
                    received.accumulate("version", versionNum + 1);
                    Map<String, Object> values = received;
                    BasicDBObject dbo = new BasicDBObject(values);
                    String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                    //set @id to objectID and update the annotation
                    BasicDBObject dboWithObjectID = new BasicDBObject(dbo);
                    //used to be replace, not put.  Not sure why.
                    dboWithObjectID.put("@id", newObjectID);
                    mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_OK);
                    jo.element("newObjectID", newObjectID);
                    try {
                        response.setStatus(HttpServletResponse.SC_CREATED); //FIXME: Or should this be OK?
                        out = response.getWriter();
                        out.print(jo);
                    } catch (IOException ex) {
                        Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    BasicDBObject last = (BasicDBObject) ls_count.get(ls_count.size() - 1);
                    int versionNum = last.getInt("veresion");
                    received.remove("version");
                    received.accumulate("version", versionNum + 1);
                    Map<String, Object> values = received;
                    BasicDBObject dbo = new BasicDBObject(values);
                    String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                    //set @id to objectID and update the annotation
                    BasicDBObject dboWithObjectID = new BasicDBObject(dbo);
                    //used to be replace, not put.  Not sure why.
                    dboWithObjectID.put("@id", newObjectID);
                    mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_OK);
                    jo.element("newObjectID", newObjectID);
                    try {
                        response.setStatus(HttpServletResponse.SC_CREATED); //FIXME: or should this be OK?
                        out = response.getWriter();
                        out.print(jo);
                    } catch (IOException ex) {
                        Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else{
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                jo.element("msg", "The annotation you are trying to make a new version of does not exist.");
                try {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch(Exception ex){ //could not parse JSON
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Delete a given annotation. 
     * @param annotation.objectID
     */
    public void deleteAnnotationByObjectID() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){ 
            BasicDBObject query = new BasicDBObject();
            try{
                JSONObject received = JSONObject.fromObject(content);
                if(received.containsKey("objectID")){
                    query.append("_id", received.getString("objectID").trim());
                    mongoDBService.delete(Constant.COLLECTION_ANNOTATION, query);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                else{
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT); //FIXME or should this be BAD_REQUEST
                }
            }
            catch (Exception ex){ //could not parse JSON
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new Exception("annotation provided for delete was not JSON, could not get id to delete");
            }
        }
        else{
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    /**
     * Delete a given annotation. 
     * @param annotation.@id
     */
    public void deleteAnnotationByAtID() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        if(null != content){ 
            BasicDBObject query = new BasicDBObject();
            try{
                JSONObject received = JSONObject.fromObject(content);
                if(received.containsKey("@id")){
                    query.append("@id", received.getString("@id").trim());
                    mongoDBService.delete(Constant.COLLECTION_ANNOTATION, query);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                else{
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT); //FIXME or should this be BAD_REQUEST
                }
            }
            catch (Exception ex){ //could not parse JSON
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw new Exception("annotation provided for delete was not JSON, could not get id to delete");
            }
        }
        else{
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

//        JSONObject jo = new JSONObject();
 //       jo.element("code", HttpServletResponse.SC_OK);
//        try {
//            response.addHeader("Access-Control-Allow-Origin", "*");
//            out = response.getWriter();
//            out.print(jo);
//        } catch (IOException ex) {
//            Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    /**
     * Fork a given annotation
     * @param annotation.objectID
     * @param annotation.permission (optional, if null, set to private by default)
     */
    public void forkAnnotation() throws IOException, ServletException, Exception{
        content = processRequestBody(request);
        BasicDBObject query = new BasicDBObject();
        JSONObject received = JSONObject.fromObject(content);
        query.append("_id", received.getString("objectID").trim());
        BasicDBObject result = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
        if(null != result){
            BasicDBObject fork = new BasicDBObject(result);
            fork.append("forkFromID", result.get("_id"));
            JSONObject jo = JSONObject.fromObject(fork);
            try {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_NOT_FOUND);
            jo.element("msg", "The annotation you are trying to fork does not exist.");
            try {
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save forked annotation. 
     * @param annotation
     * @return what saveNewAnnotation() returns
     */
    public void saveForkAnnotation() throws IOException, ServletException, Exception{
        saveNewAnnotation();
    }
    
    @Override
    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    @Override
    public void setServletResponse(HttpServletResponse hsr) {
        this.response = hsr;
    }

    /**
     * @return the mongoDBService
     */
    public MongoDBService getMongoDBService() {
        return mongoDBService;
    }

    /**
     * @param mongoDBService the mongoDBService to set
     */
    public void setMongoDBService(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
    }

    /**
     * @return the acceptedServer
     */
    public AcceptedServer getAcceptedServer() {
        return acceptedServer;
    }

    /**
     * @param acceptedServer the acceptedServer to set
     */
    public void setAcceptedServer(AcceptedServer acceptedServer) {
        this.acceptedServer = acceptedServer;
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
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

}
