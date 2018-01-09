/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * REST notes
 * 
 * POST
    * HTTP.POST can be used when the client is sending data to the server and the server
    * will decide the URI for the newly created resource. The POST method is used 
    * to request that the origin server accept the entity enclosed in the request
    * as a new subordinate of the resource identified by the Request-URI 
    * in the Request-Line.

 * PUT
    * HTTP.PUT can be used when the client is sending data to the the server and
    * the client is determining the URI for the newly created resource. The PUT method 
    * requests that the enclosed entity be stored under the supplied Request-URI. If 
    * the Request-URI refers to an already existing resource, the enclosed entity
    * SHOULD be considered as a modified version of the one residing on the origin
    * server. If the Request-URI does not point to an existing resource, 
    * and that URI is capable of being defined as a new resource by the requesting 
    * user agent, the origin server can create the resource with that URI.
    * It is most-often utilized for update capabilities, PUT-ing to a known resource
    * URI with the request body containing the newly-updated representation of the 
    * original resource.

 * PATCH
    * HTTP.PATCH can be used when the client is sending one or more changes to be
    * applied by the the server. The PATCH method requests that a set of changes described 
    * in the request entity be applied to the resource identified by the Request-URI.
    * The set of changes is represented in a format called a patch document.
    * Submits a partial modification to a resource. If you only need to update one
    * field for the resource, you may want to use the PATCH method.
 * 
 */

/**
 * Web Annotation Protocol Notes
 * Annotations can be updated by using a PUT request to replace the entire state of the Annotation. 
 * Annotation Servers should support this method. Servers may also support using a PATCH request to update only the aspects of the Annotation that have changed, 
 * but that functionality is not specified in this document.
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author hanyan &&  bhaberbe
 All the actions hit as an API like ex. /saveNewObject.action
 This implementation follows RESTFUL standards.  If you make changes, please adhere to this standard.

 */
public class ObjectAction extends ActionSupport implements ServletRequestAware, ServletResponseAware{
    private String content;
    private String oid;
    private AcceptedServer acceptedServer;
    private MongoDBService mongoDBService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder bodyString;
    private BufferedReader bodyReader;
    private PrintWriter out;
    final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Check if the proposed object is a container type.
     * Related to Web Annotation compliance.  
     * @param jo  the JSON or JSON-LD object
     * @see getAnnotationByObjectID(),saveNewObject(),updateObject() 
     * @return containerType Boolean representing if RERUM knows whether it is a container type or not.  
     */
    public Boolean isContainerType(JSONObject jo){
        Boolean containerType = false;
        String typestring = jo.getString("@type");
        //These are the types RERUM knows and IIIF says these types are containers.  How can we check against custom @context and types?
        if(typestring.equals("sc:Sequence") || typestring.equals("sc:AnnotationList") 
            || typestring.equals("sc:Range") || typestring.equals("sc:Layer")
            || typestring.equals("sc:Collection")){
            containerType = true;
        }
        return containerType; 
    }
    
    /**
     * Check if the proposed object is valid JSON-LD.
     * @param jo  the JSON object to check
     * @see getAnnotationByObjectID(),saveNewObject(),updateObject() 
     * @return isLd Boolean
     */
    public Boolean isLD(JSONObject jo){
        Boolean isLD=jo.containsKey("@context");
        return isLD;
        // TODO: There's probably some great code to do real checking.
    }
    
    /**
     * Write error to response.out.  The methods that call this function handle quitting, this just writes the error because of the quit. 
     * @param msg The message to show the user
     * @param status The HTTP response status to return
     */
    public void writeErrorResponse(String msg, int status){
        JSONObject jo = new JSONObject();
        jo.element("code", status);
        jo.element("message", msg);
        try {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json");
            response.setStatus(status);
            out = response.getWriter();
            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            out.write(System.getProperty("line.separator"));
        } 
        catch (IOException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Add the __rerum properties object to a given JSONObject. If __rerum already exists you need to update certain values.  See below.
     * Properties for consideration are:
     *   APIversion        —1.0.0
     *   history.prime     —if it has an @id, import from that, else "root"
     *   history.next      —always [] (or null, perhaps)
     *   history.previous  —if it has an @id, @id
     *   releases.previous —if it has an @id, import from that, else null
     *   releases.next     —always [] (or null, perhaps)
     *   generatedBy       —set to the @id of the public agent of the API Key.
     *   createdAt         —"addedTime" timestamp in milliseconds
     *   isOverwritten, isReleased   —always null
     * 
     * @param received A potentially optionless JSONObject from the Mongo Database (not the user).  This prevents tainted __rerum's
     * @return configuredObject The same object that was recieved but with the proper __rerum options.  This object is intended to be saved as a new object (@see versioning)
     */
    public JSONObject configureRerumOptions(JSONObject received, boolean update){
        JSONObject configuredObject = received;
        JSONObject received_options;
        try{
            //If this is an update, the object will have __rerum
            received_options = received.getJSONObject("__rerum"); 
        }
        catch(Exception e){ 
            //otherwise, it is a new save or an update on an object without the __rerum property
            received_options = new JSONObject();
        }
        JSONObject history = new JSONObject();
        JSONObject releases = new JSONObject();
        JSONObject rerumOptions = new JSONObject();
        String history_prime = "";
        String history_previous = "";
        String releases_previous = "";
        String[] emptyArray = new String[0];
        rerumOptions.element("APIversion", "1.0.0");
        rerumOptions.element("createdAt", System.currentTimeMillis());
        rerumOptions.element("isOverwritten", "");
        rerumOptions.element("isReleased", "");
        if(received_options.containsKey("history")){
            history = received_options.getJSONObject("history");
            if(update){
                //This means we are configuring from the update action and we have passed in a clone of the originating object (with its @id) that contained a __rerum.history
                if(history.getString("prime").equals("root")){
                    //Hitting this case means we are updating from the prime object, so we can't pass "root" on as the prime value
                    history_prime = received.getString("@id");
                }
                else{
                    //Hitting this means we are updating an object that already knows its prime, so we can pass on the prime value
                    history_prime = history.getString("prime");
                }
                //Either way, we know the previous value shold be the @id of the object received here. 
                history_previous = received.getString("@id");
            }
            else{
                //Hitting this means we are saving a new object and found that __rerum.history existed.  We don't trust it.
                history_prime = "root";
                history_previous = "";
            }
        }
        else{
            if(update){
             //Hitting this means we are updating an object that did not have __rerum history.  This is weird.  What should I do?
                //FIXME 
            }
            else{
             //Hitting this means we are are saving an object that did not have __rerum history.  This is normal   
                history_prime = "root";
                history_previous = "";
            }
        }
        if(received_options.containsKey("releases")){
            releases = received_options.getJSONObject("releases");
            releases_previous = releases.getString("previous");
        }
        else{
            releases_previous = "";         
        }
        releases.element("next", emptyArray);
        history.element("next", emptyArray);
        history.element("previous", history_previous);
        history.element("prime", history_prime);
        releases.element("previous", releases_previous);
        rerumOptions.element("history", history);
        rerumOptions.element("releases", releases);      
        //The access token is in the header  "Authorization: Bearer {YOUR_ACCESS_TOKEN}"
        //HttpResponse<String> response = Unirest.post("https://cubap.auth0.com/oauth/token") .header("content-type", "application/json") .body("{\"grant_type\":\"client_credentials\",\"client_id\": \"WSCfCWDNSZVRQrX09GUKnAX0QdItmCBI\",\"client_secret\": \"8Mk54OqMDqBzZgm7fJuR4rPA-4T8GGPsqLir2aP432NnmG6EAJBCDl_r_fxPJ4x5\",\"audience\": \"https://cubap.auth0.com/api/v2/\"}") .asString(); 
        rerumOptions.element("generatedBy",""); //TODO get the @id of the public agent of the API key
        configuredObject.element("__rerum", rerumOptions); //.element will replace the __rerum that is there OR create a new one
        return configuredObject; //The mongo save/update has not been called yet.  The object returned here will go into mongo.save or mongo.update
    }
    
    /**
     * Update the history.next property of an object.  This will occur because updateObject will create a new object from a given object, and that
 given object will have a new next value of the new object.  Watch out for missing __rerum or malformed __rerum.history
     * 
     * @param idForUpdate the @id of the object whose history.next needs to be updated
     * @param newNextID the @id of the newly created object to be placed in the history.next array.
     * @return Boolean altered true on success, false on fail
     */
    public boolean alterHistoryNext (String idForUpdate, String newNextID){
        Boolean altered = false;
        BasicDBObject query = new BasicDBObject();
        query.append("@id", idForUpdate);
        DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
        DBObject myAnnoWithHistoryUpdate;
        JSONObject annoToUpdate = JSONObject.fromObject(myAnno);
        if(null != myAnno){
            try{
                annoToUpdate.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next").add(newNextID); //write back to the anno from mongo
                myAnnoWithHistoryUpdate = (DBObject)JSON.parse(annoToUpdate.toString()); //make the JSONObject a DB object
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, myAnno, myAnnoWithHistoryUpdate); //update in mongo
                altered = true;
            }
            catch(Exception e){ //__rerum array does not exist or history object malformed.  What should I do?
                writeErrorResponse("This object does not contain the proper history property.  It may not be from RERUM, the update failed.", HttpServletResponse.SC_CONFLICT);
            }
        }
        else{ //THIS IS A 404
            writeErrorResponse("Object for update not found...", HttpServletResponse.SC_NOT_FOUND);
        }
        return altered;
    }
    
    /**
     * Checks for appropriate RESTful method being used.
     * The action first comes to this function.  It says what type of request it 
     * is and checks the the method is appropriately RESTful.  Returns false if not and
     * the method that calls this will handle a false response;
     * @param http_request the actual http request object
     * @param request_type a string denoting what type of request this should be
     * @return Boolean indicating RESTfulness
     * @throws Exception 
    */
    public Boolean methodApproval(HttpServletRequest http_request, String request_type) throws Exception{
        String requestMethod = http_request.getMethod();
        boolean restful = false;
        // FIXME @webanno if you notice, OPTIONS is not supported here and MUST be 
        // for Web Annotation standards compliance.  
        switch(request_type){
            case "update":
                if(requestMethod.equals("PUT") || requestMethod.equals("PATCH")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for updating, please use PUT or PATCH.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            case "create":
                if(requestMethod.equals("POST")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for creating, please use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            case "delete":
                if(requestMethod.equals("DELETE")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for deleting, please use DELETE.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            case "get":
                if(requestMethod.equals("GET") || requestMethod.equals("HEAD")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for reading, please use GET or receive headers with HEAD.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            default:
                writeErrorResponse("Improper request method for this type of request (unknown).", HttpServletResponse.SC_METHOD_NOT_ALLOWED);

            }  
        return restful;
    }
    
    /**
     * All actions come here to process the request body. We check if it is JSON.
     * DELETE is a special case because the content could be JSON or just the @id string and it only has to specify a content type if passing a JSONy object.  
     * and pretty format it. Returns pretty stringified JSON or fail to null.
     * Methods that call this should handle requestBody==null as unexpected.
     * @param http_request Incoming request to check.
     * @return String of anticipated JSON format.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @throws java.lang.Exception
     */
    public String processRequestBody(HttpServletRequest http_request, boolean deletion) throws IOException, ServletException, Exception{
        String cType = http_request.getContentType();
        String requestBody;
        JSONObject complianceInfo = new JSONObject();
        bodyReader = http_request.getReader();
        bodyString = new StringBuilder();
        String line;
        if(cType.contains("application/json") || cType.contains("application/ld+json")){
            JSONObject test;
            JSONArray test2;
            while ((line = bodyReader.readLine()) != null)
            {
              bodyString.append(line).append("\n");
            }
            requestBody = bodyString.toString();
            try{ 
              //JSONObject test
              test = JSONObject.fromObject(requestBody);
            }
            catch(Exception ex){ 
                if(deletion){
                    //We do not allow arrays of ID's for DELETE, so if it failed JSONObject parsing then this is a hard fail for DELETE.
                    //They attempted to provide a JSON object for DELETE but it was not valid JSON
                    writeErrorResponse("The data passed was not valid JSON.  Could not get @id for DELETE: \n"+requestBody, HttpServletResponse.SC_BAD_REQUEST);
                    requestBody = null;
                }
                else{
                    //Maybe it was an action on a JSONArray, check that before failing JSON parse test.
                    try{
                        //JSONArray test
                        test2 = JSONArray.fromObject(requestBody);
                    }
                    catch(Exception ex2){
                        // not a JSONObject or a JSONArray. 
                        writeErrorResponse("The data passed was not valid JSON:\n"+requestBody, HttpServletResponse.SC_BAD_REQUEST);
                        requestBody = null;
                    }
                }
            }          
            // no-catch: Is either JSONObject or JSON Array
        }
        else{ 
            if(deletion){ //Content type is not JSONy, looking for @id string as body
                while ((line = bodyReader.readLine()) != null)
                {
                  bodyString.append(line).append("\n");
                }
                requestBody = bodyString.toString(); 
                if("".equals(requestBody)){
                    //No ID provided
                    writeErrorResponse("Must provide an id or a JSON object containing @id of object to delete.", HttpServletResponse.SC_BAD_REQUEST);
                    requestBody = null;
                }
                else{ 
                    // This string could be ANYTHING.  ANY string is valid at this point.  Create a wrapper JSONObject for elegant handling in deleteObject().  
                    // We will check against the string for existing objects in deleteObject(), processing the body is completed as far as this method is concerned.
                    JSONObject modifiedDeleteRequest = new JSONObject();
                    modifiedDeleteRequest.element("@id", requestBody);
                    requestBody = modifiedDeleteRequest.toString();
                }
            }
            else{ //This is an error, actions must use the correct content type
                writeErrorResponse("Invalid Content-Type. Please use 'application/json' or 'application/ld+json'", HttpServletResponse.SC_BAD_REQUEST);
                requestBody = null;
            }
        }
        //@cubap @theHabes TODO IIIF compliance handling on action objects
        /*
        if(null != requestBody){
            complianceInfo = checkIIIFCompliance(requestBody, "2.1");
            if(complianceInfo.getInt("okay") < 1){
                writeErrorResponse(complianceInfo.toString(), HttpServletResponse.SC_CONFLICT);
                requestBody = null;
            }
        }
        */
        content = requestBody;
        response.setContentType("application/json"); // We create JSON objects for the return body in most cases.  
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Allow-Methods", "GET,OPTIONS,HEAD,PUT,PATCH,DELETE,POST"); // Must have OPTIONS for @webanno 
        return requestBody;
    }
    
    /**
     * Creates and appends headers to the HTTP response required by Web Annotation standards.
     * Headers are attached and read from {@link #response}. 
     * 
     * @param etag A unique fingerprint for the object for the Etag header.
     * @param isContainerType A boolean noting whether or not the object is a container type.
     * @param isLD  the object is either plain JSON or is JSON-LD ("ld+json")
     */
    private void addWebAnnotationHeaders(String etag, Boolean isContainerType, Boolean isLD){
        if(isLD){
            response.addHeader("Content-Type", "application/ld+json;profile=\"http://www.w3.org/ns/anno.jsonld\""); 
        } 
        else {
            response.addHeader("Content-Type", "application/json;"); 
            // This breaks Web Annotation compliance, but allows us to return requested
            // objects without misrepresenting the content.
        }
        if(isContainerType){
            response.addHeader("Link", "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\""); 
            response.addHeader("Link", "<http://www.w3.org/TR/annotation-protocol/>; rel=\"http://www.w3.org/ns/ldp#constrainedBy\"");  
        }
        else{
            response.addHeader("Link", "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\""); 
        }
        response.addHeader("Allow", "GET,OPTIONS,HEAD,PUT,PATCH,DELETE,POST"); 
        if(!"".equals(etag)){
            response.addHeader("Etag", etag);
        }
    }
    
    /**
     * Creates or appends Location header from @id.
     * Headers are attached and read from {@link #response}. 
     * @param obj  the JSON being returned to client
     * @see #addLocationHeader(net.sf.json.JSONArray) addLocationHeader(JSONArray)
     */
    private void addLocationHeader(JSONObject obj){
        String addLocation;
        // Warning: if there are multiple "Location" headers only one will be returned
        // our practice of making a list protects from this.
        if (response.containsHeader("Location")) {
            // add to existing header
            addLocation = response.getHeader("Location").concat(",").concat(obj.getString("@id"));
        }
        else {
            // no header attached yet
            addLocation = obj.getString("@id");
        }
        response.setHeader("Location", addLocation);
    }

    /**
     * Creates or appends list of @ids to Location header.
     * Headers are attached and read from {@link #response}. 
     * @param arr  the JSON Array being returned to the client
     * @see #addLocationHeader(net.sf.json.JSONObject) addLocationHeader(JSONObject)
     */    
    private void addLocationHeader(JSONArray arr){
        for(int j=0; j<arr.size(); j++){
            addLocationHeader(arr.getJSONObject(j)); 
        }
    }

    /** 
        * TODO @see batchSaveMetadataForm.  Do both methods need to exist?  Combine if possible. This is the method we use for generic bulk saving.
        * Each canvas has an annotation list with 0 - infinity annotations.  A copy requires a new annotation list with the copied annotations and a new @id.
        * Mongo allows us to bulk save.  
        * The content is from an HTTP request posting in an array filled with annotations to copy.  
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.servlet.ServletException
        * @see MongoDBAbstractDAO.bulkSaveFromCopy(String collectionName, BasicDBList entity_array);
        * @see MongoDBAbstractDAO.bulkSetIDProperty(String collectionName, BasicDBObject[] entity_array);
    */ 
    public void batchSaveFromCopy() throws UnsupportedEncodingException, IOException, ServletException, Exception{
        if(null != processRequestBody(request, false) && methodApproval(request, "create")){
            JSONArray received_array = JSONArray.fromObject(content);
            for(int b=0; b<received_array.size(); b++){ //Configure __rerum on each object
                JSONObject configureMe = received_array.getJSONObject(b);
                configureMe = configureRerumOptions(configureMe, false); //configure this object
                received_array.set(b, configureMe); //Replace the current iterated object in the array with the configured object
            }
            BasicDBList dbo = (BasicDBList) JSON.parse(received_array.toString()); //tricky cause can't use JSONArray here
            JSONArray newResources = new JSONArray();
            //if the size is 0, no need to bulk save.  Nothing is there.
            if(dbo.size() > 0){
                newResources = mongoDBService.bulkSaveFromCopy(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else {
                // empty array
            }
            //bulk save will automatically call bulk update 
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_CREATED);
            jo.element("new_resources", newResources);
            addLocationHeader(newResources);
            try {
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            } catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Servlet method to find all upstream versions of an object.  This is the action the user hits with the API.
     * If this object is `prime`, it will be the only object in the array.
     * @param  http_request Servlet request for relatives
     * @throws Exception 
     */
    public void getAllAncestors(HttpServletRequest http_request) throws Exception{
        if(null != processRequestBody(request, true) && methodApproval(request, "get")){
            // TODO: @theHabes, this is waiting for something clever to happen.
            // This code is not correct at all, but pseudo-correct.
            JSONObject received = JSONObject.fromObject(content);
            
            List<DBObject> ls_versions = getAllVersions(received);
            // cubap: At this point, we have all the versions of the object (except maybe the
            // original?) and need to filter to the ones we want.
            // Getting the whole document is a mess, but if we get subdocuments of __rerum, 
            // we don't need to worry as much.
            
            //What are we trying to return to the user here.  Resolved objects or @ids?
            //What is faster?  Taking this ID and resolving all previous too root, or make one call for everything for all versions and parse out what we need from memory. 
            //What is safer?  Is it ok to gather a large collection to memory and read from it.  Is there a limit on this?  Whats our limit?
            //ls_versions as a list cannot be trusted, so we would have to search the whole list each time (instead of a query by @id each time) which I have to assume is faster. 
            // http://snmaynard.com/2012/10/17/things-i-wish-i-knew-about-mongodb-a-year-ago/
            // https://www.datadoghq.com/blog/collecting-mongodb-metrics-and-statistics/
            // https://www.sitepoint.com/7-simple-speed-solutions-mongodb/
            
            JSONArray ancestors = getAllAncestors(ls_versions, received);
            try {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_OK);
                out = response.getWriter();
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ancestors));
            } 
            catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Filters ancestors upstream from `key object` until `prime`.  This is the internal private function that gathers the data for the action available to the user.
     * This list WILL NOT contains the keyObj.
     * @param  ls_versions all the versions of the key object on all branches
     * @param keyObj The object from which to start looking for ancestors.  It is not included in the return. 
     * @return array of objects
     */
    private JSONArray getAllAncestors(List<DBObject> ls_versions, JSONObject keyObj, JSONArray discoveredAncestors) {
        String previousID = keyObj.getJSONObject("__rerum").getJSONObject("history").getString("previous"); //The first previous to look for
        String rootCheck = keyObj.getJSONObject("__rerum").getJSONObject("history").getString("prime"); //Make sure the keyObj is not root.
        DBObject previousObj;
        List<DBObject> ls_objects = null;
        for(int n=0; n<ls_versions.size(); n++){
            DBObject thisVersion = ls_versions.get(n);
            JSONObject thisObject = JSONObject.fromObject(thisVersion);
            //@cubap what should we do if we detect malformed objects here?
            /*
            try{
                
            }
            catch (Exception e){
                writeErrorResponse("Could not gather the ancestors.  One of the nodes did not contains a proper history object.",  HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                break;
            }
            */
            if("root".equals(rootCheck)){
                //Check if we found root when we got the last object out of the list.  If so, we are done.  If keyObj was root, it will be detected here.  Break out. 
                System.out.println("END SCENARIO!");
                break;
            }
            else if(thisObject.getString("@id").equals(previousID)){
                //If this object's @id is equal to the previous from the last object we found, its the one we want.  Look to its previous to keep building the ancestors Array.  
                
                previousID = thisObject.getJSONObject("__rerum").getJSONObject("history").getString("previous");
                rootCheck = thisObject.getJSONObject("__rerum").getJSONObject("history").getString("prime");
                
                if("".equals(previousID) && !"root".equals(thisObject)){
                    //previous is blank and this object is not the root.  This is gunna trip it up.  
                    //@cubap Yikes this is a problem.  This branch on the tree is broken...what should we tell the user?
                    return new JSONArray();
                }
                else{
                    //either previous had a value or we found root and it will be caught in the next iteration.  Proceed with confidence. 
                    previousObj = (DBObject) JSON.parse(thisVersion.toString());
                    discoveredAncestors.add(previousObj);
                    getAllAncestors(ls_versions, thisObject, discoveredAncestors);
                }
            }                  
        }
        return discoveredAncestors;
    }
    
    /**
     * Filters for all versions downstream from `key object`.
     * @param  ls_versions all the versions of the key object on all branches
     * @return array of objects
     */
    public void getAllDescendants(HttpServletRequest http_request) throws Exception {
        List<DBObject> ls_objects = null;
        JSONObject received = JSONObject.fromObject(content);
        List<DBObject> ls_versions = getAllVersions(received);
        JSONArray descendants = getAllDescendants(ls_versions, received, new JSONArray());
        // TODO: Iterate the List and find the original object. Then move from
        // _rerum.history.next to _rerum.history.next, building a new List
        // to return to the servlet. Consider organizing tree in arrays.
        try {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(HttpServletResponse.SC_OK);
            out = response.getWriter();
            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(descendants));
        } 
        catch (IOException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Servlet method to find all downstream versions of an object.  
     * If this object is the last, the return will be null.
     * @TODO this needs to return an array of JSONObjects.  It needs to support a request by string id or object with {@id:id} in it.
     * @param  http_request Servlet request for relatives
     * @throws Exception 
     */

    private JSONArray getAllDescendants(List<DBObject> ls_versions, JSONObject keyObj, JSONArray discoveredDescendants){
        JSONArray nextIDarr = new JSONArray();
        //var helperArr = [];
        System.out.println("What is end scenario for getting desc on "+keyObj.getString("@id")+"?");
        System.out.println(keyObj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next"));
        if(keyObj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next").size() > 0){
            //essentially, do nothing.  This branch is done.
            System.out.println("END SCENARIO!");
        }
        else{
            //The provided object has nexts, get them to add them to known descendants then check their descendants.
            nextIDarr = keyObj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next");
        }
        System.out.println("Nexts: "+nextIDarr);
        for(int m=0; m<nextIDarr.size(); m++){ //For each id in the array
            String nextID = nextIDarr.getString(m);
            //System.out.println("2");
            for(int n=0; n<ls_versions.size(); n++){ //Check if obj[@id] is equal to the id from the array
                DBObject thisVersion = ls_versions.get(n);
                JSONObject thisObject = JSONObject.fromObject(thisVersion);
                System.out.println("Main does "+thisObject.getString("@id")+" == "+nextID);
                if(thisObject.getString("@id") == nextID){ //If it is equal, add it to the known descendants
                    //System.out.println("Push "+nextID+" into discovered arr.");
                    System.out.println("Push this object into discovered array "+thisObject.getString("@id"));
                    //helperArr.concat(thisVersion["next"]);
                    discoveredDescendants.add(thisObject);
                    System.out.println("Now recurse on "+thisObject.getString("@id"));
                    getAllDescendants(ls_versions, thisObject, discoveredDescendants);
                }
            }
        }
        return discoveredDescendants;
    }
    
    /**
     * Loads all derivative versions from the `prime` object. Used to resolve the history tree to memory for reads from memory.
     * This means we don't make O(n) calls to the database for objects, we do it to a List.  
     * in other methods. May be replaced later with more optimized logic.
     * @param  http_request Servlet request for relatives
     * @return All versions from the store of the object in the request
     * @throws Exception 
     */
    private List<DBObject> getAllVersions(JSONObject obj) throws Exception {
        List<DBObject> ls_versions = null;
        BasicDBObject rootObj;
        BasicDBObject query = new BasicDBObject();
        BasicDBObject queryForRoot = new BasicDBObject();  
        String primeID;
         //@theHabes @cubap   So here is a pinch point.  If we index __rerum, this would be much faster.
        if(obj.getJSONObject("__rerum").getJSONObject("history").getString("prime").equals("root")){
            primeID = obj.getString("@id");
            //Get all objects whose prime is this things @id
            query.append("__rerum.history.prime", primeID);
            ls_versions = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            rootObj = (BasicDBObject) JSON.parse(obj.toString()); 
            ls_versions.add(0, rootObj);
        }
        else{
            primeID = obj.getString("@id");
            //Get all objects whose prime is equal to this ID
            query.append("__rerum.history.prime", primeID);
            ls_versions = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            queryForRoot.append("@id", primeID);
            rootObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, queryForRoot);
            ls_versions.add(0, rootObj);
        }
        // get reliable copy of key object
        return ls_versions;
    }
        
    /**
     * Get annotation by objectiD.  Strip all unnecessary key:value pairs before returning.
     * @param objectID (oid)
     * @return annotation object
     */
    public void getByID() throws IOException, ServletException, Exception{
        if(null != oid && methodApproval(request, "get")){
            //find one version by objectID
            BasicDBObject query = new BasicDBObject();
            query.append("_id", oid);
            DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != myAnno){
                BasicDBObject bdbo = (BasicDBObject) myAnno;
                JSONObject jo = JSONObject.fromObject(myAnno.toMap());
                //String idForHeader = jo.getString("_id");
                //The following are rerum properties that should be stripped.  They should be in __rerum.
                jo.remove("_id");
                jo.remove("addedTime");
                jo.remove("originalAnnoID");
                jo.remove("version");
                jo.remove("permission");
                jo.remove("forkFromID"); // retained for legacy v0 objects
                jo.remove("serverName");
                jo.remove("serverIP");
                // @context may not be here and shall not be added, but the response
                // will not be ld+json without it.
                try {
                    addWebAnnotationHeaders(oid, isContainerType(jo), isLD(jo));
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                } 
                catch (IOException ex){
                    Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                writeErrorResponse("No object found with provided id '"+oid+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Get annotations by given properties. 
     * @param Object with key:value pairs with conditions to match against.
     * @reutrn list of annotations that match the given conditions.
     */
    // This is not Web Annotation standard as the specifications states you respond with a single object, not a list.  Not sure what to do with these.
    // @cubap answer: I asked on oac-discuss and was told Web Annotation hasn't handled lists yet, so just be nice.
    public void getByProperties() throws IOException, ServletException, Exception{
        if(null != processRequestBody(request, false) && methodApproval(request, "get")){
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
                    response.addHeader("Content-Type","application/json"); // not ld+json because it is an array
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ja));
                } 
                catch (IOException ex) {
                    Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                writeErrorResponse("Object(s) not found using provided properties '"+received+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Save a new annotation. 
     * @param all annotation properties.
     */
    public void saveNewObject() throws IOException, ServletException, Exception{
        if(null != processRequestBody(request, false) && methodApproval(request, "create")){
            JSONObject received = JSONObject.fromObject(content);
            JSONObject iiif_validation_response = checkIIIFCompliance(received, true); //This boolean should be provided by the user somehow.  It is a intended-to-be-iiif flag
            configureRerumOptions(received, false);
            DBObject dbo = (DBObject) JSON.parse(received.toString());
            if(null!=request.getHeader("Slug")){
                // Slug is the user suggested ID for the annotation. This could be a cool RERUM thing.
                // cubap: if we want, we can just copy the Slug to @id, warning
                // if there was some mismatch, since versions are fine with that.
            }
            String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
            //set @id from _id and update the annotation
            BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
            String uid = "http://devstore.rerum.io/rerumserver/id/"+newObjectID;
            dboWithObjectID.append("@id", uid);
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
            JSONObject jo = new JSONObject();
            JSONObject newObjWithID = JSONObject.fromObject(dboWithObjectID);
            jo.element("code", HttpServletResponse.SC_CREATED);
            jo.element("@id", uid);
            jo.element("iiif_validation", iiif_validation_response);
            try {
                response.addHeader("Access-Control-Allow-Origin", "*");
                addWebAnnotationHeaders(newObjectID, isContainerType(received), isLD(received));
                addLocationHeader(newObjWithID);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out = response.getWriter();
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            } 
            catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public void setVal(){
        
    }
    
    public void unsetVal(){
        
    }
    
    /**
     * Update a given annotation. PUT that does not set or unset only.
     * This is one place new branches of an annotation may be created
     * when the `annotation.objectID` resolves to an object that has
     * an entry in .__rerum.history.next already.
     * @param annotation.objectID
     * @param all annotation properties include updated properties. 
     * @ignore the following keys (they will never be updated)
     *      @id
     *      objectID
     */
    public void updateObject() throws IOException, ServletException, Exception{
        //The client should use the If-Match header with a value of the ETag it received from the server before the editing process began, 
        //to avoid collisions of multiple users modifying the same Annotation at the same time
        //cubap: I'm not sold we have to do this. Our versioning would allow multiple changes. 
        //The application might want to throttle internally, but it can.
        Boolean historyNextUpdatePassed = false;
        if(null!= processRequestBody(request, false) && methodApproval(request, "update")){
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content); 
            String updateHistoryNextID = received.getString("@id");
            query.append("@id", updateHistoryNextID);
            BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
            BasicDBObject updatedObject = (BasicDBObject) originalObject.clone(); //A copy of the original, this will be saved as a new object.  Make all edits to this variable.
            if(null != originalObject){
                Set<String> update_anno_keys = received.keySet();
                //If the object already in the database contains the key found from the object recieved from the user, update it barring a few special keys
                //Users cannot update the __rerum property, so we ignore any update action to that particular field.  
                for(String key : update_anno_keys){
                    if(originalObject.containsKey(key) && (!key.equals("@id") || !key.equals("__rerum")) || !key.equals("objectID")){
                        updatedObject.remove(key);
                        updatedObject.append(key, received.get(key));
                    }
                }
                JSONObject newObject = JSONObject.fromObject(updatedObject);//The edited original object meant to be saved as a new object (versioning)

                newObject = configureRerumOptions(newObject, true); //__rerum for the new object being created because of the update action
                newObject.remove("@id"); //This is being saved as a new object, so remove this @id for the new one to be set.
                //Since we ignore changes to __rerum for existing objects, we do no configureRerumOptions(updatedObject);
                DBObject dbo = (DBObject) JSON.parse(newObject.toString());
                String newNextID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                String newNextAtID = "http://devstore.rerum.io/rerumserver/id/"+newNextID;
                BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                dboWithObjectID.append("@id", newNextAtID);
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                historyNextUpdatePassed = alterHistoryNext(updateHistoryNextID, newNextAtID); //update history.next or original object to include the newObject @id
                if(historyNextUpdatePassed){
                    JSONObject jo = new JSONObject();
                    JSONObject iiif_validation_response = checkIIIFCompliance(newNextAtID, "2.1");
                    jo.element("code", HttpServletResponse.SC_OK);
                    jo.element("original_object_id", updateHistoryNextID);
                    jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                    jo.element("iiif_validation", iiif_validation_response);
                    try {
                        addWebAnnotationHeaders(newNextID, isContainerType(newObject), isLD(newObject));
                        response.addHeader("Access-Control-Allow-Origin", "*");
                        response.setStatus(HttpServletResponse.SC_OK);
                        out = response.getWriter();
                        out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                    } 
                    catch (IOException ex) {
                        Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    //The error is already written to response.out, do nothing.
                }
            }
            else{
                writeErrorResponse("Object "+received.getString("@id")+" not found in RERUM, could not update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * A helper function that determines whether or not an object has been flagged as deleted.
     * @param obj
     * @return A boolean representing the truth.
     */
    public boolean checkIfDeleted(JSONObject obj){
        boolean deleted = obj.containsKey("__deleted");
        return deleted;
    }
    
    /**
     * A helper function that gathers an object by its id and determines whether or not it is flagged as deleted.
     * @param obj_id
     * @return A boolean representing the truth.
     */
    public boolean checkIfDeleted(String obj_id){
        BasicDBObject query = new BasicDBObject();
        BasicDBObject dbObj;
        query.append("@id", obj_id);
        dbObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); 
        JSONObject checkThis = JSONObject.fromObject(dbObj);
        return checkIfDeleted(checkThis);

    }
    
    /**
    * check that the API keys match and that this application has permission to delete the object
    */
    public boolean checkApplicationPermission(JSONObject obj){
        boolean permission = true;
        //@cubap @theHabes TODO check that the API keys match and that this application has permission to delete the object
        return permission;
    }
    
    /**
     * A helper function that gathers an object by its id and determines whether or not it is flagged as released.
     * @param obj
     * @return 
     */
    public boolean checkIfReleased(JSONObject obj){
        boolean released = false;
        if(!obj.getJSONObject("__rerum").getString("isReleased").equals("")){
            released = true;
        }
        return released;
    }
    
    /**
     * A helper function that gathers an object by its id and determines whether or not it is flagged as released.
     * @param obj_id
     * @return 
     */
    public boolean checkIfReleased(String obj_id){
        BasicDBObject query = new BasicDBObject();
        BasicDBObject dbObj;
        query.append("@id", obj_id);
        dbObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); 
        JSONObject checkThis = JSONObject.fromObject(dbObj);
        return checkIfReleased(checkThis);
    }
       
    /**
     * Delete a given annotation. 
     */
    public void deleteObject() throws IOException, ServletException, Exception{
        if(null!=processRequestBody(request, true) && methodApproval(request, "delete")){ 
            BasicDBObject query = new BasicDBObject();
            BasicDBObject originalObject;
            BasicDBObject updatedObjectWithDeletedFlag;
            //processRequestBody will always return a stringified JSON object here, even if the ID provided was a string in the body.
            JSONObject received = JSONObject.fromObject(content);
            boolean alreadyDeleted = checkIfDeleted(received);
            boolean permission = false;
            boolean isReleased = false;
            boolean passedAllChecks = false;
            if(alreadyDeleted){
                writeErrorResponse("Object for delete is already deleted.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
            else{
                isReleased = checkIfReleased(received);
                if(isReleased){
                    writeErrorResponse("This object is in a released state and cannot be deleted.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);  
                }
                else{
                    permission = checkApplicationPermission(received);
                    if(permission){
                       passedAllChecks = true;
                    }
                    else{
                       writeErrorResponse("Only the application that created this object can delete it.", HttpServletResponse.SC_UNAUTHORIZED);   
                    }
                }
            }
            if(passedAllChecks){ //If all checks have passed.  If not, we want to make sure their writeErrorReponse() don't stack.  
                if(received.containsKey("@id")){
                    query.append("@id", received.getString("@id").trim());
                    originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The original object out of mongo for persistance
                    updatedObjectWithDeletedFlag = (BasicDBObject) originalObject.clone(); //A clone of this mongo object for manipulation.
                    //Found the @id in the object, but does it exist in RERUM?
                    if(null != originalObject){
                        JSONObject deletedFlag = new JSONObject(); //The __deleted flag is a JSONObject
                        deletedFlag.element("object", originalObject);
                        deletedFlag.element("deletor", "TODO"); //@cubap I assume this will be an API key?
                        deletedFlag.element("time", System.currentTimeMillis());
                        updatedObjectWithDeletedFlag = (BasicDBObject) updatedObjectWithDeletedFlag.put("__deleted", deletedFlag);
                        boolean treeHealed = greenThumb(JSONObject.fromObject(originalObject));
                        if(treeHealed){
                            mongoDBService.update(Constant.COLLECTION_ANNOTATION, originalObject, updatedObjectWithDeletedFlag);
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }
                        else{
                            //@cubap @theHabes FIXME By default, objects that don't have the history property will fail to this line.
                            writeErrorResponse("We could not update the history tree correctly.  The delete failed.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                    else{
                        writeErrorResponse("The '@id' string provided for DELETE could not be found in RERUM: "+received.getString("@id")+". \n DELETE failed.", HttpServletResponse.SC_NOT_FOUND);
                    }
                }
                else{
                    writeErrorResponse("Object for delete did not contain an '@id'.  Could not delete.", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }
    
    /**
     * When an object is deleted, the history tree around it will need amending.  This function acts as the green thumb for tree trimming.
     * 
     * @param obj A JSONObject of the object being deleted.
     * @return A boolean representing whether or not this function succeeded. 
     */
     public boolean greenThumb(JSONObject obj){
         boolean success = true;
         String previous_id = "";
         String prime_id = "";
         JSONArray next_ids = new JSONArray();
         try{
             //Try to dig down the object and get these properties
            previous_id = obj.getJSONObject("__rerum").getJSONObject("history").getString("previous");
            prime_id = obj.getJSONObject("__rerum").getJSONObject("history").getString("prime");
            next_ids = obj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next");
         }
         catch(Exception e){
             //@cubap @theHabes FIXME this should probably be handled separately and gracefully and have its own writeErrorReponse() that doesn't stack with deleteObject().  @see treeHealed
            previous_id = ""; //This ensures detectedPrevious is false
            prime_id = ""; //This ensures isRoot is false
            next_ids = new JSONArray(); //This ensures the loop below does not run.
            success = false; //This will bubble out to deleteObj() and have the side effect that this object is not deleted.  @see treeHealed
         }
         boolean isRoot = prime_id.equals("root"); 
         boolean detectedPrevious = !previous_id.equals("");
         //Update the history.previous of all the next ids in the array of the deleted object
         for(int n=0; n<next_ids.size(); n++){
             BasicDBObject query = new BasicDBObject();
             BasicDBObject objToUpdate;
             BasicDBObject objWithUpdate;
             String nextID = next_ids.getString(n);
             query.append("@id", nextID);
             objToUpdate = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); 
             if(null != objToUpdate){
                JSONObject fixHistory = JSONObject.fromObject(objToUpdate);
                if(isRoot){ //The object being deleted was root.  That means these next objects must become root.  Strictly, all history trees must have num(root) > 0.  
                    fixHistory.getJSONObject("__rerum").getJSONObject("history").element("prime", "root");
                    newTreePrime(fixHistory);
                }
                else if(detectedPrevious){ //The object being deleted had a previous.  That is now absorbed by this next object to mend the gap.  
                    fixHistory.getJSONObject("__rerum").getJSONObject("history").element("previous", previous_id);
                }
                else{
                    // @cubap @theHabes TODO Yikes this is some kind of error...it is either root or has a previous, this case means neither are true.
                    // cubap: Since this is a __rerum error and it means that the object is already not well-placed in a tree, maybe it shouldn't fail to delete?
                    // theHabes: Are their bad implications on the relevant nodes in the tree that reference this one if we allow it to delete?  Will their account of the history be correct?
                    //success = false;
                }
                Object forMongo = JSON.parse(fixHistory.toString()); //JSONObject cannot be converted to BasicDBObject
                objWithUpdate = (BasicDBObject)forMongo;
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, objToUpdate, objWithUpdate);
             }
             else{
                 success = false;
                 //Yikes this is an error, could not find an object assosiated with id found in history tree.
             }
         }
         if(detectedPrevious){ 
             //The object being deleted had a previous.  That previous object next[] must be updated with the deleted object's next[].
             BasicDBObject query2 = new BasicDBObject();
             BasicDBObject objToUpdate2;
             BasicDBObject objWithUpdate2;
             query2.append("@id", previous_id);
             objToUpdate2 = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query2); 
             if(null != objToUpdate2){
                JSONObject fixHistory2 = JSONObject.fromObject(objToUpdate2); 
                JSONArray origNextArray = fixHistory2.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next");
                JSONArray newNextArray = new JSONArray();  
                //JSONArray does not have splice, but we can code our own.  This will splice out obj["@id"].
                for (int i=0; i<origNextArray.size(); i++){ 
                    if (!origNextArray.getString(i).equals(obj.getString("@id"))){
                        //So long as the value is not the deleted id, add it to the newNextArray (this is the splice).  
                        newNextArray.add(origNextArray.get(i));
                    }
                } 
                newNextArray.addAll(next_ids); //Adds next array of deleted object to the end of this array in order.
                fixHistory2.getJSONObject("__rerum").getJSONObject("history").element("next", newNextArray); //Rewrite the next[] array to fix the history
                Object forMongo2 = JSON.parse(fixHistory2.toString()); //JSONObject cannot be converted to BasicDBObject
                objWithUpdate2 = (BasicDBObject)forMongo2;
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, objToUpdate2, objWithUpdate2);
             }
             else{
                 //Yikes this is an error.  We had a previous id in the object but could not find it in the store.
                 success = false;
             }
         }
         return success;
     }
     
     /**
     * All descendants of this JSONObject must take on a new history.prime of this object's ID
     * 
     * @param obj A new prime object whose descendants must take on its id
     */
     public boolean newTreePrime(JSONObject obj){
         boolean success = true;
         String primeID = obj.getString("@id");
         //TODO
         JSONArray descendants = new JSONArray();
         //JSONArray descendants = getAllDescendants(obj);
         for(int n=0; n< descendants.size(); n++){
             JSONObject descendantForUpdate = descendants.getJSONObject(n);
             JSONObject originalDescendant = descendants.getJSONObject(n);
             Object orig = JSON.parse(originalDescendant.toString()); //JSONObject cannot be converted to BasicDBObject
             BasicDBObject objToUpdate = (BasicDBObject)orig;
             descendantForUpdate.getJSONObject("__rerum").getJSONObject("history").element("prime", primeID);
             Object forMongo = JSON.parse(descendantForUpdate.toString()); //JSONObject cannot be converted to BasicDBObject
             BasicDBObject objWithUpdate = (BasicDBObject)forMongo;
             mongoDBService.update(Constant.COLLECTION_ANNOTATION, objToUpdate, objWithUpdate);
         }
         return success;
     }
     
     /**
     * Validate data is IIIF compliant against IIIF's validator.  This object is intended for creation and not yet saved into RERUM so it does not yet have an @id.
     * The only way to hit the IIIF Validation API is to use the object's @id. The idea would be to save the objects to get an id, hit the 
     * IIIF API and if the object was intended to be IIIF, delete it from the store and return an error to the user.
     * 
     * In the case the object was not intended to be IIIF, do not return an error. Since the methods calling this will handle what happens based of iiif_return.okay,
     * just set okay to 1 and the methods calling this will treat it as if it isn't a problem. 
     * 
     * @param objectToCheck A JSON object to parse through and validate.  This object has not yet been saved into mongo, so it does not have an @id yet
     * @param intendedIIIF A flag letting me know whether or not this object is intended to be IIIF.  If it isn't, don't treat validation failure as an error.
     * @return iiif_return The return JSONObject from hitting the IIIF Validation API.
     */
    public JSONObject checkIIIFCompliance(JSONObject objectToCheck, boolean intendedIIIF) throws MalformedURLException, IOException{
        JSONObject iiif_return = new JSONObject();
        DBObject dbo = (DBObject) JSON.parse(objectToCheck.toString());
        BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
        String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
        String uid = "http://devstore.rerum.io/rerumserver/id/"+newObjectID;
        dboWithObjectID.append("@id", uid);
        mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
        iiif_return = checkIIIFCompliance(uid, "2.1"); //If it is an object we are creating, this line means @context must point to Presentation API 2 or 2.1
        if(iiif_return.getInt("okay") == 0){
            if(intendedIIIF){
                //If it was intended to be a IIIF object, then remove this object from the store because it was not valid and return an error to the user
                
            }
            else{
                //Otherwise say it is ok so the action looking do validate does not writeErrorResponse()
                iiif_return.element("okay", 1);
            }
        }
        BasicDBObject query = new BasicDBObject();
        query.append("_id", newObjectID);
        mongoDBService.delete(Constant.COLLECTION_ANNOTATION, query);
        return iiif_return;
    }
    
     /**
     * A JSONObject that already has an @id can be validated against this IIIF validation URL.  It will return a JSONObject.
     * A save or update action could hit this to see if the resulting object is IIIF valid.  If not, a 'rollback' or 'delete' could be performed
     * and the warnings and errors sent back to the user.  If using this function, it is assumed objURL is intended to be a IIIF URL.
     * 
     * @param objURL The @id or id URL of a IIIF JSON object represented as a String.
     * @param version The Intended Presentation API version to validate against represented as a String.  (1, 2 or 2.1)
     * @return iiif_return The return JSONObject from hitting the IIIF Validation API.
     */
    public JSONObject checkIIIFCompliance(String objURL, String version) throws MalformedURLException, IOException{
        JSONObject iiif_return = new JSONObject();
        String iiif_validation_url = "http://iiif.io/api/presentation/validator/service/validate?format=json&version="+version+"&url="+objURL;
        URL validator = new URL(iiif_validation_url);
        BufferedReader reader = null;
        StringBuilder stringBuilder;
        HttpURLConnection connection = (HttpURLConnection) validator.openConnection();
        connection.setRequestMethod("GET"); //hmm... I think this is right
        connection.setReadTimeout(15*1000);
        connection.connect();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
        {
          stringBuilder.append(line + "\n");
        }
        connection.disconnect();
        iiif_return = JSONObject.fromObject(stringBuilder.toString());
        return iiif_return;
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

