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
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONException;


/**
 * @author hanyan && bhaberbe
 * All the actions hit as an API like ex. /saveNewAnnotation.action
 * This implementation follows RESTFUL standards.  If you make changes, please adhere to this standard.

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
    private ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Write error to response.out.  The methods that call this function handle quitting, this just writes the error because of the quit. 
     * @param msg The message to show the user
     * @param code The HTTP response code to return
     */
    public void send_error(String msg, int code){
        JSONObject jo = new JSONObject();
        jo.element("code", code);
        jo.element("message", msg);
        try {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json");
            response.setStatus(code);
            out = response.getWriter();
            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            out.write(System.getProperty("line.separator"));
        } catch (IOException ex) {
            Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * The action first comes to this function.  It says what type of request it is and checks the the method is appropriately RESTful.  Returns false if not and
     * the method that calls this will handle approved=false;
     * @param http_request the actual http request object
     * @param request_type a string denoting what type of request this should be
     * @throws Exception 
    */
    public Boolean methodApproval(HttpServletRequest http_request, String request_type) throws Exception{
        String requestMethod = http_request.getMethod();
        boolean restful = false;
        System.out.println("Request type is "+request_type);
        System.out.println("Request method is "+requestMethod);
        if(requestMethod.equals("OPTIONS")){ //This is a browser pre flight...
            //This breaks everything because the pre flight request doesn't pass any data.  
            //This happens when using a bookmarklet from a different domain.  I do not know how to handle it.
            send_error("Browser pre-flight requests are not supported.  Call this API from within an application on a server.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);           
        }
        else{
            switch(request_type){
                case "update":
                    if(requestMethod.equals("PUT") || requestMethod.equals("PATCH")){
                        restful = true;
                    }
                    else{
                        send_error("Improper request method for this type of request, please use PUT or PATCH.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                break;
                case "create":
                    if(requestMethod.equals("POST")){
                        restful = true;
                    }
                    else{
                        send_error("Improper request method for this type of request, please use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                break;
                case "delete":
                    if(requestMethod.equals("DELETE")){
                        restful = true;
                    }
                    else{
                        send_error("Improper request method for this type of request, please use DELETE.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                break;
                case "get":
                    if(requestMethod.equals("GET")){
                        restful = true;
                    }
                    else{
                        send_error("Improper request method for this type of request, please use GET.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                break;
                default:
                    send_error("Improper request method for this type of request.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);

            }  
        }
        return restful;
    }
    
    /**
    * All actions come here to process the request body.  We check if it is JSON and pretty format it.  Returns null if there is a problem.  The methods that call
    * this will handle requestBody==null;
    */
    public String processRequestBody(HttpServletRequest http_request) throws IOException, ServletException, Exception{
        
        String cType = http_request.getContentType();
        String methodCheck = http_request.getMethod();
        String requestBody;
        JSONObject test;
        JSONArray test2;
        
        if(methodCheck.equals("OPTIONS")){//This is a browser pre flight...
            //This breaks everything because the pre flight request doesn't pass any data.  
            //This happens when using a bookmarklet from a different domain.  I do not know how to handle it.
            send_error("Browser pre-flight requests are not supported.  Call this API from within an application on a server.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            requestBody = null;
        }
        else if(cType.contains("application/json") || cType.contains("application/ld+json")){
            bodyReader = http_request.getReader();
            bodyString = new StringBuilder();
            String line="";
            while ((line = bodyReader.readLine()) != null)
            {
              bodyString.append(line + "\n");
            }
            requestBody = bodyString.toString();
            try{ //Try to parse as a JSONObject
              test = JSONObject.fromObject(requestBody);
            }
            catch(Exception ex){ //was not a JSONObject but might be a JSONArray
                try{ //Try to parse as a JSONArray
                    test2 = JSONArray.fromObject(requestBody);
                }
                catch(Exception ex2){ //Was not a JSONObject or a JSONArray.  Not valid JSON.  Throw error. 
                    send_error("The data passed was not valid JSON", HttpServletResponse.SC_BAD_REQUEST);
                    requestBody = null;
                }
            }          
        }
        else{ //I do not understand the content type being passed.     
            send_error("You did not use the correct content type.  Please use application/json or application/ld+json", HttpServletResponse.SC_BAD_REQUEST);
            requestBody = null;
        }
        //If you set headers, later down the line you cannot call response.sendError();
        response.setContentType("application/json"); //This is why we create JSON objects for the return body in most cases.  
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT"); //Not OPTIONS because that breaks things
        return requestBody;
    }
    
    /**
    *The batch save to intended to work with Broken Books, but could be applied elsewhere.  This batch will use the save() mongo function instead of insert() to determine whether 
    to do an update() or insert() for each item in the batch.  
    *    The content is from an HTTP request posting in an array filled with annotations to copy.  
    *    @see MongoDBAbstractDAO.bulkSaveMetadataForm(String collectionName, BasicDBList entity_array);
    *    @see MongoDBAbstractDAO.bulkSetIDProperty(String collectionName, BasicDBObject[] entity_array);
    */   
    public void batchSaveMetadataForm() throws UnsupportedEncodingException, IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "create");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null != content){
            JSONArray received_array = JSONArray.fromObject(content);
            BasicDBObject serverQuery = new BasicDBObject();
            serverQuery.append("ip", request.getRemoteAddr());
            DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
            BasicDBObject asbdbo = (BasicDBObject) asdbo;
            for(int b=0; b<received_array.size(); b++){
                JSONArray received_options;
                JSONObject received = received_array.getJSONObject(b);
                try{
                    received_options  = received.getJSONArray("__rerum");
                }
                catch(JSONException e){ //__rerum may or may not have been submitted with this item.  If not, don't throw just move on with an empty array.
                    received_options = new JSONArray();
                }
                JSONObject option = new JSONObject();
                JSONArray rerumOptions = new JSONArray();
                boolean permissionFound = false;
                boolean forkFromIDFound = false;
                //These three liners will happen a lot to follow.  It is to create an JSONObject to add to the __rerum JSONArray.
                option.element("addedTime", System.currentTimeMillis());
                rerumOptions.add(option);
                option.clear();
                option.element("originalAnnoID","");
                rerumOptions.add(option);
                option.clear();
                option.element("version", 1);
                rerumOptions.add(option);
                option.clear();
                //We need to check if certain options were already set.  If they were, then we shouldn't set them here.
                for(int k=0; k<received_options.size(); k++){
                    JSONObject entry = received_options.getJSONObject(k);
                    if(entry.containsKey("permission")){
                        permissionFound = true;
                    }
                    if(null != entry.get("forkFromID") || !"".equals(entry.get("forkFromID"))){
                        forkFromIDFound = true;
                    }
                }
                //TODO: Is this correct? 
                if(!permissionFound){
                    option.element("permission", Constant.PERMISSION_PRIVATE);
                    rerumOptions.add(option);
                    option.clear();
                }
                if(!forkFromIDFound){
                    option.element("forkFromID", "");
                    rerumOptions.add(option);
                    option.clear();
                }
                received.element("serverName", asbdbo.get("name"));
                received.element("serverIP", asbdbo.get("ip"));
                received_array.set(b,received);
            }
            
            BasicDBList dbo = (BasicDBList) JSON.parse(received_array.toString());
            JSONArray reviewedResources = new JSONArray();
            //if the size is 0, no need to bulk save.  Nothing is there.
            if(dbo.size() > 0){
                reviewedResources = mongoDBService.bulkSaveMetadataForm(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else{
             //   System.out.println("Skipping bulk save on account of empty array.");
            }
            //bulk save will automatically call bulk update so there is no real need to return these values.  We will for later use.
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_CREATED);
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
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            } catch (IOException ex) {
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    /** 
        * Each canvas has an annotation list with 0 - infinity annotations.  A copy requires a new annotation list with the copied annotations and a new @id.
        * Mongo allows us to bulk save.  
        * The content is from an HTTP request posting in an array filled with annotations to copy.  
        * @see MongoDBAbstractDAO.bulkSaveFromCopy(String collectionName, BasicDBList entity_array);
        * @see MongoDBAbstractDAO.bulkSetIDProperty(String collectionName, BasicDBObject[] entity_array);
    */ 
    public void batchSaveFromCopy() throws UnsupportedEncodingException, IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "create");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null != content){
            JSONArray received_array = JSONArray.fromObject(content);
            BasicDBObject serverQuery = new BasicDBObject();
            serverQuery.append("ip", request.getRemoteAddr());
            DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
            BasicDBObject asbdbo = (BasicDBObject) asdbo;
            for(int b=0; b<received_array.size(); b++){
                JSONArray received_options;
                JSONObject received = received_array.getJSONObject(b);
                try{
                    received_options  = received.getJSONArray("__rerum");
                }
                catch(JSONException e){ //__rerum may or may not have been submitted with this item.  If not, don't throw just move on with an empty array.
                    received_options = new JSONArray();
                }
                JSONObject option = new JSONObject();
                JSONArray rerumOptions = new JSONArray();
                boolean permissionFound = false;
                boolean forkFromIDFound = false;
                //These three liners will happen a lot to follow.  It is to create an JSONObject to add to the __rerum JSONArray.
                option.element("addedTime", System.currentTimeMillis());
                rerumOptions.add(option);
                option.clear();
                option.element("originalAnnoID","");
                rerumOptions.add(option);
                option.clear();
                option.element("version", 1);
                rerumOptions.add(option);
                option.clear();
                //We need to check if certain options were already set.  If they were, then we shouldn't set them here.
                for(int k=0; k<received_options.size(); k++){
                    JSONObject entry = received_options.getJSONObject(k);
                    if(entry.containsKey("permission")){
                        permissionFound = true;
                    }
                    if(null != entry.get("forkFromID") || !"".equals(entry.get("forkFromID"))){
                        forkFromIDFound = true;
                    }
                }
                //TODO: Is this correct? 
                if(!permissionFound){
                    option.element("permission", Constant.PERMISSION_PRIVATE);
                    rerumOptions.add(option);
                    option.clear();
                }
                if(!forkFromIDFound){
                    option.element("forkFromID", "");
                    rerumOptions.add(option);
                    option.clear();
                }
                received.element("serverName", asbdbo.get("name"));
                received.element("serverIP", asbdbo.get("ip"));
                received_array.set(b,received);
            }
            
            BasicDBList dbo = (BasicDBList) JSON.parse(received_array.toString());
            JSONArray newResources = new JSONArray();
            //if the size is 0, no need to bulk save.  Nothing is there.
            if(dbo.size() > 0){
                newResources = mongoDBService.bulkSaveFromCopy(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else{
             //   System.out.println("Skipping bulk save on account of empty array.");
            }
            //bulk save will automatically call bulk update 
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_CREATED);
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
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
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
        Boolean approved = methodApproval(request, "get");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null!=content){
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
                        out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ja));
                    } catch (IOException ex) {
                        Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    send_error("Object(s) not found.", HttpServletResponse.SC_NOT_FOUND);            
                }
            }
            else{
                send_error("Could not find field 'namespace' in provided object.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Get all version of annotations by ObjectID. 
     * @param annotation.objectID
     */
    public void getAllVersionsOfAnnotationByObjectID() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "get");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null!=content){
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
                        out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ja));
                    } 
                    catch (IOException ex) {
                        Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    send_error("Object(s) not found.", HttpServletResponse.SC_NOT_FOUND);
                }
            }
            else{
                send_error("Provided object did not contain key 'objectID'.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Get annotation by objectiD.  Strip all unnecessary key:value pairs before returning.
     * @param objectID (oid)
     * @return annotation object
     */
    public void getAnnotationByObjectID() throws IOException, ServletException, Exception{
        //content = processRequestBody(request);
        Boolean approved = methodApproval(request, "get");
        if(approved){
            
        }
        else{
            oid=null;
        }
        if(null != oid){
            //find one version by objectID
            System.out.println("gloabl oid is "+oid);
            BasicDBObject query = new BasicDBObject();
            query.append("_id", new ObjectId(oid));
            DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != myAnno){
                BasicDBObject bdbo = (BasicDBObject) myAnno;
                JSONObject jo = JSONObject.fromObject(myAnno.toMap());
                //The following are rerum properties that should be stripped.  They should be in __rerum.
                jo.remove("_id");
                jo.remove("addedTime");
                jo.remove("originalAnnoID");
                jo.remove("version");
                jo.remove("permission");
                jo.remove("forkFromID");
                jo.remove("serverName");
                jo.remove("serverIP");
                jo.remove("__rerum");
                try {
                    response.addHeader("Content-Type", "application/ld+json");
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                } 
                catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                send_error("No object(s) found with provided id '"+oid+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Get annotations by given properties. 
     * @param Object with key:value pairs with conditions to match against.
     * @reutrn list of annotations that match the given conditions.
     */
    public void getAnnotationByProperties() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "get");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
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
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ja));
                } 
                catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                send_error("Object(s) not found using provided properties '"+received+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Save a new annotation. 
     * @param all annotation properties.
     */
    public void saveNewAnnotation() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "create");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null != content){
            try{
                JSONObject received = JSONObject.fromObject(content);
                JSONArray received_options;
                try{
                    received_options  = received.getJSONArray("__rerum");
                }
                catch(JSONException e){ //__rerum may or may not have been submitted with this item.  If not, don't throw just move on with an empty array.
                    received_options = new JSONArray();
                }
                JSONObject option = new JSONObject();
                JSONArray rerumOptions = new JSONArray();
                boolean permissionFound = false;
                boolean forkFromIDFound = false;
                //These three liners will happen a lot to follow.  It is to create an JSONObject to add to the __rerum JSONArray.
                option.element("addedTime", System.currentTimeMillis());
                rerumOptions.add(option);
                option.clear();
                option.element("originalAnnoID","");
                rerumOptions.add(option);
                option.clear();
                option.element("version", 1);
                rerumOptions.add(option);
                option.clear();
                //We need to check if certain options were already set.  If they were, then we shouldn't set them here.
                for(int k=0; k<received_options.size(); k++){
                    JSONObject entry = received_options.getJSONObject(k);
                    if(entry.containsKey("permission")){
                        permissionFound = true;
                    }
                    if(null != entry.get("forkFromID") || !"".equals(entry.get("forkFromID"))){
                        forkFromIDFound = true;
                    }
                }
                //TODO: Is this correct? 
                if(!permissionFound){
                    option.element("permission", Constant.PERMISSION_PRIVATE);
                    rerumOptions.add(option);
                    option.clear();
                }
                if(!forkFromIDFound){
                    option.element("forkFromID", "");
                    rerumOptions.add(option);
                    option.clear();
                }
                BasicDBObject serverQuery = new BasicDBObject();
                serverQuery.append("ip", request.getRemoteAddr());
                DBObject asdbo = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, serverQuery);
                BasicDBObject asbdbo = (BasicDBObject) asdbo;
                option.element("serverIP", asbdbo.get("ip"));
                rerumOptions.add(option);
                option.clear();
                option.element("serverName", asbdbo.get("name"));
                rerumOptions.add(option);
                option.clear();
                //we use element instead of accumulate so the clean array made here is either created or entirely replaces __rerum to the object.
                received.element("__rerum", rerumOptions); 
                //create BasicDBObject           
                DBObject dbo = (DBObject) JSON.parse(received.toString());
                String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                //set @id to objectID and update the annotation
                BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                String uid = "http://store.rerum.io/rerumstore/id/" + dboWithObjectID.getObjectId("_id").toString();
                dboWithObjectID.append("@id", uid);
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                JSONObject jo = new JSONObject();
                
                jo.element("code", HttpServletResponse.SC_CREATED);
                jo.element("@id", uid);
                try {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.addHeader("Location", uid);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                } 
                catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (Exception ex){ //could not parse JSON.
                send_error("Trouble parsing JSON", HttpServletResponse.SC_BAD_REQUEST);
            }
        }

    }
    
    public void setVal(){
        
    }
    
    public void unsetVal(){
        
    }
    
    /**
     * Update a given annotation. PUT that does not set or unset only.
     * @param annotation.objectID
     * @param all annotation properties include updated properties. 
     * @FIXME things are in __rerum now
     * @ignore the following keys (they will never be updated)
     *      @id
     *      version
     *      forkFromID
     *      originalAnnoID
     *      objectID
     */
    public void updateAnnotation() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "update");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null!= content){
            try{
                BasicDBObject query = new BasicDBObject();
                JSONObject received = JSONObject.fromObject(content); //object that has an id and new key:val pairs.
                query.append("@id", received.getString("@id").trim());
                BasicDBObject result = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The result DB object
                JSONObject existing_object = JSONObject.fromObject(result); //object from the store to be updated
                if(null != result){
                    Set<String> update_anno_keys = received.keySet();
                    JSONArray update_rerum_options = received.getJSONArray("__rerum");
                    boolean existingOptions = false; //Does the result DB object already contain __rerum
                    if(result.containsKey("__rerum")){
                        existingOptions = true;
                    }
                    //If the object already in the database contains the key found from the object recieved from the user, update it barring a few special keys
                    for(String key : update_anno_keys){
                        if(result.containsKey(key) && (!key.equals("@id") || !key.equals("__rerum")) || !key.equals("objectID")){
                            result.remove(key);
                            result.append(key, received.get(key));
                        }
                    }
                    //If the object already in the database contains __rerum key, make sure the objects to be updated for the __rerum array already exist before updating.  ignore special keys.
                    if(existingOptions){
                        JSONArray existing_options = existing_object.getJSONArray("__rerum");
                        for(int l=0; l<update_rerum_options.size(); l++){
                            JSONObject update_option = update_rerum_options.getJSONObject(l);
                            String updateKey = update_option.keys().next().toString(); //This object will only contain one key:val pair
                            for(int m=0; m<existing_options.size(); m++){
                                JSONObject existing_option = existing_options.getJSONObject(m);
                                String existingKey = existing_option.keys().next().toString(); //This object will only contain one key:val pair
                                if(existingKey.equals(updateKey) && !(updateKey.equals("version") || updateKey.equals("forkFromID") || updateKey.equals("originalAnnoID"))){
                                    existing_options.element(m, update_option); //replace the element at this index with the submitted update to the object.
                                }
                            }
                        }
                        result.remove("__rerum");
                        result.append("__rerum", existing_options);
                    }
                    else{
                        //Don't update any __rerum stuff because this key did not exist in the object already
                    }
                    mongoDBService.update(Constant.COLLECTION_ANNOTATION, query, result); //update the result DBObject with any changes performed above
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_OK);
                    try {
                        response.addHeader("Access-Control-Allow-Origin", "*");
                        response.setStatus(HttpServletResponse.SC_OK);
                        out = response.getWriter();
                        out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                    } 
                    catch (IOException ex) {
                        Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                else{
                    send_error("Object(s) to update not found.", HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch(Exception ex){ //could not parse JSON
                send_error("Trouble parsing JSON", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Save current annotation to a new version. 
     * @param objectID
     * @param any to be updated annotation properties. 
     * @FIXME things are in __rerum now
     */
    public void saveNewVersionOfAnnotation() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "create");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null!= content){
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
                            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                        } 
                        catch (IOException ex) {
                            Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else{
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
                            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                        } 
                        catch (IOException ex) {
                            Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                else{
                    send_error("The annotation you are trying to make a new version of does not exist.", HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch(Exception ex){ //could not parse JSON
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Delete a given annotation. 
     * @param annotation.objectID
     */
    public void deleteAnnotationByObjectID() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "delete");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
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
                send_error("annotation provided for delete was not JSON, could not get id to delete", HttpServletResponse.SC_BAD_REQUEST);
            }
        }

    }
    
    /**
     * Delete a given annotation. 
     * @param annotation.@id
     */
    public void deleteAnnotationByAtID() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "delete");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
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
                     send_error("annotation provided for delete has no @id, could not delete", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            catch (Exception ex){ //could not parse JSON
                send_error("annotation provided for delete was not JSON, could not get id to delete", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Fork a given annotation
     * @param annotation.objectID
     * @param annotation.permission (optional, if null, set to private by default)
     * @FIXME things are in __rerum now
     */
    public void forkAnnotation() throws IOException, ServletException, Exception{
        Boolean approved = methodApproval(request, "create");
        if(approved){
            content = processRequestBody(request);
        }
        else{
            content = null;
        }
        if(null!= content){
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
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                } catch (IOException ex) {
                    Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                send_error("The annotation you are trying to fork does not exist.", HttpServletResponse.SC_NOT_FOUND);
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
