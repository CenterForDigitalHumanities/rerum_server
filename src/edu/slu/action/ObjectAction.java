/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. t
 */

/**
 * REST notes
 * https://spring.io/understanding/REST
 * https://user-images.githubusercontent.com/3287006/32914301-b2fbf798-cada-11e7-9541-a2bee8454c2c.png
 
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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;

//import org.apache.commons.codec.*;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.net.ProtocolException;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.ServletInputStream;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static java.lang.System.console;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import jdk.nashorn.internal.parser.JSONParser;
import static jxl.biff.BaseCellFeatures.logger;
import net.sf.json.JSONSerializer;

//import jdk.nashorn.internal.parser.JSONParser;
//import java.util.UUID;

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
    private String generatorID = "unknown";
    private final ObjectMapper mapper = new ObjectMapper();
    private CacheAccess<String, RSAPublicKey> cache = null;
    private static AmazonDynamoDB client;
    private static DynamoDB dynamoDB;
    private static String tableName;
    //private String json_obj;
    //AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();

    
   
        
    
            

   /**
    * Private function to get information from the rerum properties file
    
    * @param prop the name of the property to retrieve from the file.
    * @return the value for the provided property
    */    
   
   private static String getRerumProperty(String prop) {
      ResourceBundle rb = ResourceBundle.getBundle("rerum");
      String propVal = "";
      try {
         propVal = rb.getString(prop);
      } catch (MissingResourceException e) {                     
         System.err.println("Token ".concat(prop).concat(" not in Propertyfile!"));
      }
      return propVal;
   }
   
    /**
    * Public API proxy to generate a new refresh token through Auth0.
    * 
    * @param authCode An authorization code generated by the Auth0 /authorize call.
    * @return the Auth0 /oauth/token return's refresh_token value
    */    
   public void generateNewRefreshToken() throws MalformedURLException, IOException, Exception {
        if(null!= processRequestBody(request, false) && methodApproval(request, "token")){
            System.out.println("Proxy generate a refresh token");
            JSONObject received = JSONObject.fromObject(content);
            JSONObject jsonReturn = new JSONObject();
            String authTokenURL = "https://cubap.auth0.com/oauth/token";
            JSONObject tokenRequestParams = new JSONObject();
            
            tokenRequestParams.element("grant_type", "authorization_code");
            tokenRequestParams.element("client_id", getRerumProperty("clientID"));
            tokenRequestParams.element("code" , received.getString("authorization_code"));
            tokenRequestParams.element("client_secret", getRerumProperty("rerumSecret"));
            tokenRequestParams.element("redirect_uri", Constant.RERUM_BASE);
            try{
                URL auth0 = new URL(authTokenURL);
                HttpURLConnection connection = (HttpURLConnection) auth0.openConnection();
                connection.setRequestMethod("POST"); 
                connection.setConnectTimeout(5*1000); 
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.connect();
                DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                //Pass in the user provided JSON for the body 
                outStream.writeBytes(tokenRequestParams.toString());
                outStream.flush();
                outStream.close(); 
                //Execute rerum server v1 request
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    //Gather rerum server v1 response
                    sb.append(line);
                }
                reader.close();
                connection.disconnect();
                jsonReturn = JSONObject.fromObject(sb.toString());
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_OK);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
            catch(java.net.SocketTimeoutException e){ //This specifically catches the timeout
                System.out.println("The Auth0 token endpoint is taking too long...");
                jsonReturn = new JSONObject(); //We were never going to get a response, so return an empty object.
                jsonReturn.element("error", "The Auth0 endpoint took too long");
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
            catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                jsonReturn = new JSONObject(); 
                jsonReturn.element("error", "Couldn't access output stream");
                jsonReturn.element("msg", ex.toString());
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
        }
   }
   
   /**
    * Public API proxy to generate a new access token through Auth0.
    * 
    * @param refreshToken The refresh token given to the user on registration or generateNewRefreshToken
    * @return the Auth0 /oauth/token return's access_token property
    */    
   public void generateNewAccessToken() throws MalformedURLException, IOException, Exception {
       if(null!= processRequestBody(request, false) && methodApproval(request, "token")){
            System.out.println("Proxy generate an access token");
            JSONObject received = JSONObject.fromObject(content);
            JSONObject jsonReturn = new JSONObject();
            String authTokenURL = "https://cubap.auth0.com/oauth/token";
            JSONObject tokenRequestParams = new JSONObject(); 
            tokenRequestParams.element("grant_type","refresh_token");
            tokenRequestParams.element("client_id",getRerumProperty("clientID"));
            tokenRequestParams.element("client_secret",getRerumProperty("rerumSecret"));
            tokenRequestParams.element("refresh_token",received.getString("refresh_token"));
            tokenRequestParams.element("redirect_uri", Constant.RERUM_BASE);
            try{
                URL auth0 = new URL(authTokenURL);
                HttpURLConnection connection = (HttpURLConnection) auth0.openConnection();
                connection.setRequestMethod("POST"); 
                connection.setConnectTimeout(5*1000); 
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();
                DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                //Pass in the user provided JSON for the body 
                outStream.writeBytes(tokenRequestParams.toString());
                outStream.flush();
                outStream.close(); 
                //Execute rerum server v1 request
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    //Gather rerum server v1 response
                    sb.append(line);
                }
                reader.close();
                connection.disconnect();
                jsonReturn = JSONObject.fromObject(sb.toString());
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_OK);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
            catch(java.net.SocketTimeoutException e){ //This specifically catches the timeout
                System.out.println("The Auth0 token endpoint is taking too long...");
                jsonReturn = new JSONObject();
                jsonReturn.element("error", "The Auth0 endpoint took too long");
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
            catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                jsonReturn = new JSONObject();
                jsonReturn.element("error", "Couldn't access output stream");
                jsonReturn.element("msg", ex.toString());
                out = response.getWriter();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jsonReturn));
            }
        }
    }
   
    /**
     * Check if the proposed object is a container type.
     * Related to Web Annotation compliance.  
     * @FIXME  We need to rethink what update.action does and how to separate and handle PUT vs PATCH gracefully and compliantly.
     * @param jo  the JSON or JSON-LD object
     * @see getAnnotationByObjectID(),saveNewObject(),updateObject() 
     * @return containerType Boolean representing if RERUM knows whether it is a container type or not.  
     */
    private Boolean isContainerType(JSONObject jo){
        Boolean containerType = false;
        String typestring;
        try{
            typestring = jo.getString("@type");
        }
        catch (Exception e){
            typestring = "";
        }
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
        Boolean isLD= jo.containsKey("@context");
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
        System.out.println("Here is the error response json object to return with status "+status);
        System.out.println(jo);
        try {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(status);
            out = response.getWriter();
            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            out.write(System.getProperty("line.separator"));
        } 
        catch (IOException ex) {
            System.out.println("Write error response failed on IO exception");
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Add the __rerum properties object to a given JSONObject.If __rerum already exists, it will be overwritten because this method is only called on new objects. Properties for consideration are:
   APIversion        —1.0.0
   history.prime     —if it has an @id, import from that, else "root"
   history.next      —always [] 
   history.previous  —if it has an @id, @id
   releases.previous —if it has an @id, import from that, else ""
   releases.next     —always [] 
   generatedBy       —set to the @id of the public agent of the API Key.
   createdAt         —DateTime of right now.
   isOverwritten     —always ""
   isReleased        —always false
     * 
     * @param received A potentially optionless JSONObject from the Mongo Database (not the user).  This prevents tainted __rerum's
     * @param update A trigger for special handling from update actions
     * @return configuredObject The same object that was recieved but with the proper __rerum options.  This object is intended to be saved as a new object (@see versioning)
     */
    public JSONObject configureRerumOptions(JSONObject received, boolean update){
        JSONObject configuredObject = received;
        JSONObject received_options;
        System.out.println("Inside configureRerumOptions");
        try{
            //If this is an update, the object will have __rerum
            System.out.println("Inside try block");
            received_options = received.getJSONObject("__rerum");
            System.out.println("received_options"+received_options);
        }
        catch(Exception e){ 
            //otherwise, it is a new save or an update on an object without the __rerum property
            System.out.println("Inside catch block");
            received_options = new JSONObject();
            System.out.println("received_options"+received_options);
        }
        JSONObject history = new JSONObject();
        JSONObject releases = new JSONObject();
        JSONObject rerumOptions = new JSONObject();
        String history_prime = "";
        String history_previous = "";
        String releases_previous = "";
        String releases_replaces = releases_previous;
        System.out.println("releases_replaces"+releases_replaces);
        String[] emptyArray = new String[0];
        rerumOptions.element("@context", Constant.RERUM_CONTEXT); // RERUM context file
        rerumOptions.element("alpha", true); // alpha sandbox
        rerumOptions.element("APIversion", Constant.RERUM_API_VERSION);
       // System.out.println("rerumOptions"+rerumOptions);
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter dtFormat = DateTimeFormatter.ISO_DATE_TIME;
        String formattedCreationDateTime = dt.format(dtFormat);
        rerumOptions.element("createdAt", formattedCreationDateTime);
        rerumOptions.element("isOverwritten", "");
        rerumOptions.element("isReleased", "");
        System.out.println("rerumOptions"+rerumOptions);
        if(received_options.containsKey("history")){
            System.out.println("received_options"+received_options);
            history = received_options.getJSONObject("history");
            System.out.println("history"+history);
            if(update){
                System.out.println("update in if of configureRerumOptions"+history);
                //This means we are configuring from the update action and we have passed in a clone of the originating object (with its @id) that contained a __rerum.history
                if(history.getString("prime").equals("root")){
                    System.out.println("update in if of configureRerumOptions"+history);
                    //Hitting this case means we are updating from the prime object, so we can't pass "root" on as the prime value
                    history_prime = received.getString("id");
                    System.out.println("history_prime"+history_prime);
                }
                else{
                    //Hitting this means we are updating an object that already knows its prime, so we can pass on the prime value
                    history_prime = history.getString("prime");
                    System.out.println("history_prime in else"+history_prime);
                }
                //Either way, we know the previous value shold be the @id of the object received here. 
                //history_previous = received.getString("@id");
                history_previous = received.getString("id");
                System.out.println("history_previous in else"+history_previous);
            }
            else{
                //Hitting this means we are saving a new object and found that __rerum.history existed.  We don't trust it.
                history_prime = "root";
                history_previous = "";
                System.out.println("history_previous and history_prime in else of update"+history_previous+history_prime);
            }
        }
        else{
            System.out.println("Else Block of received_options.containsKey(\"history\") ");
            System.out.println("update in Else Block of received_options.containsKey(\"history\") "+update);
            if(update){
             //Hitting this means we are updating an object that did not have __rerum history.  This is an external object update.
                //FIXME @cubap @theHabes
                history_prime = "root";
                //history_previous = received.getString("@id");
               
                System.out.println("received"+received); 
                if(received.containsKey("@id")){
                    history_previous = received.getString("@id");
                }
                else{
                    history_previous = received.getString("id");
                }
                history_previous = received.getString("id");
                System.out.println("history_previous in Else Block of received_options.containsKey(\"history\") update true "+history_previous);
            }
            else{
             //Hitting this means we are are saving an object that did not have __rerum history.  This is normal   
                history_prime = "root";
                history_previous = "";
                System.out.println("history_previous and history_prime in else of update"+history_previous+history_prime);
                
            }
        }
        if(received_options.containsKey("releases")){
            releases = received_options.getJSONObject("releases");
            releases_previous = releases.getString("previous");
            System.out.println("releases and releases_previous in releases containsKey "+releases+releases_previous);
        }
        else{
            releases_previous = "";       
            System.out.println("releases_previous in releases containsKey "+releases_previous);
        }
        releases.element("next", emptyArray);
        history.element("next", emptyArray);
        history.element("previous", history_previous);
        history.element("prime", history_prime);
        releases.element("previous", releases_previous);
        releases.element("replaces", releases_replaces);
        rerumOptions.element("history", history);
        rerumOptions.element("releases", releases);      
        //The access token is in the header  "Authorization: Bearer {YOUR_ACCESS_TOKEN}"
        rerumOptions.element("generatedBy",generatorID); 
        configuredObject.element("__rerum", rerumOptions); //.element will replace the __rerum that is there OR create a new one
        System.out.println("configuredObject"+configuredObject);
        return configuredObject; //The mongo save/update has not been called yet.  The object returned here will go into mongo.save or mongo.update
    }
    
    /**
     * Internal helper method to update the history.previous property of a root object.  This will occur because a new root object can be created
     * by put_update.action on an external object.  It must mark itself as root and contain the original ID for the object in history.previous.
     * This method only receives reliable objects from mongo.
     * 
     * @param newRootObj the RERUM object whose history.previous needs to be updated
     * @param externalObjID the @id of the external object to go into history.previous
     * @return JSONObject of the provided object with the history.previous alteration
     */   
    private JSONObject alterHistoryPrevious (JSONObject newRootObj, String externalObjID){
        DBObject myAnnoWithHistoryUpdate;
        DBObject myAnno = (BasicDBObject) JSON.parse(newRootObj.toString());
        try{
            newRootObj.getJSONObject("__rerum").getJSONObject("history").element("previous", externalObjID); //write back to the anno from mongo
            myAnnoWithHistoryUpdate = (DBObject)JSON.parse(newRootObj.toString()); //make the JSONObject a DB object
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, myAnno, myAnnoWithHistoryUpdate); //update in mongo
        }
        catch(Exception e){ 
            writeErrorResponse("This object does not contain the proper history property.  It may not be from RERUM, the update failed.", HttpServletResponse.SC_CONFLICT);
        }
        return newRootObj;
    }
    
    /**
     * Internal helper method to update the history.next property of an object.  This will occur because updateObject will create a new object from a given object, and that
     * given object will have a new next value of the new object.  Watch out for missing __rerum or malformed __rerum.history
     * 
     * @param idForUpdate the @id of the object whose history.next needs to be updated
     * @param newNextID the @id of the newly created object to be placed in the history.next array.
     * @return Boolean altered true on success, false on fail
     */
    private boolean alterHistoryNext (String idForUpdate, String newNextID){
        //TODO @theHabes As long as we trust the objects we send to this, we can take out the lookup and pass in objects as parameters
        System.out.println("Trying to alter history...");
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
                System.out.println("Update in Mongo");
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, myAnno, myAnnoWithHistoryUpdate); //update in mongo
                System.out.println("Done");
                altered = true;
            }
            catch(Exception e){ 
                //@cubap @theHabes #44.  What if obj does not have __rerum or __rerum.history
                System.out.println("This object did not have a propery history...");
                writeErrorResponse("This object does not contain the proper history property.  It may not be from RERUM, the update failed.", HttpServletResponse.SC_CONFLICT);
            }
        }
        else{ //THIS IS A 404
            System.out.println("Couldnt find the object to alter history...");
            writeErrorResponse("Object for update not found...", HttpServletResponse.SC_NOT_FOUND);
        }
        return altered;
    }
    
    /**
     * Given an Authorization:Bearer {token} header, pull out the {token} and return it. 
     * 
     * @param authorizationHeader The 'Bearer {token}' String value to pull the {token} from.  If it does not contain 'Bearer', we have a bad header.
     * @return tokenToReturn the {token} that was extracted or "" for a bad header.
    */
    private String getTokenFromHeader(String authorizationHeader){
        String tokenToReturn = "";
        if(authorizationHeader.contains("Bearer")){
            tokenToReturn = authorizationHeader.replace("Bearer", "");
            tokenToReturn = tokenToReturn.trim();
        }
        else{
            //Bad Authorization header.  How should we handle?  Right now, the token will be returned as an empty String.
        }
        return tokenToReturn;
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
        System.out.println("Inside methodApproval ");
        String requestMethod = http_request.getMethod();
        String access_token = "";
        boolean auth_verified = false;
        boolean restful = false;
        // FIXME @webanno if you notice, OPTIONS is not supported here and MUST be 
        // for Web Annotation standards compliance.  
        if(null!=http_request.getHeader("Authorization") && !"".equals(http_request.getHeader("Authorization"))){
            access_token = getTokenFromHeader(http_request.getHeader("Authorization"));
        }
        switch(request_type){
            case "overwrite":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PUT")){
                        restful = true;
                    }
                    else{
                        writeErrorResponse("Improper request method for overwriting, please use PUT to overwrite this object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "update":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PUT")){
                        restful = true;
                    }
                    else{
                        writeErrorResponse("Improper request method for updating, please use PUT to replace this object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "patch":
                /**
                 * Note that PATCH is not a standard method.  Sometimes, programming languages don't have support for the method and 
                 * throw runtime errors, which forces people into using POST with a HTTP Method Override Header to say PATCH.
                 * As a result, the API must support catching this header on these POST requests and allowing it to be treated
                 * as PATCH throughout, which we can control here for patch, set and unset cases.  
                */
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PATCH")){
                        restful = true;
                    }
                    else{
                        String override = checkPatchOverrideSupport(http_request);
                        if(override.equals("yes")){
                            restful=true;
                        }
                        else if(override.equals("no")){
                            writeErrorResponse("Improper request method for updating, PATCH to remove keys from this RERUM object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        }
                        else if(override.equals("improper")){
                            //Error response returned by checkPatchOverrideSupport, don't double up and put one here
                        }
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "set":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PATCH")){
                        restful = true;
                    }
                    else{
                        String override = checkPatchOverrideSupport(http_request);
                        if(override.equals("yes")){
                            restful=true;
                        }
                        else if(override.equals("no")){
                            writeErrorResponse("Improper request method for updating, PATCH to remove keys from this RERUM object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        }
                        else if(override.equals("improper")){
                            //Error response returned by checkPatchOverrideSupport, don't double up and put one here
                        }
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "unset":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PATCH")){
                        restful = true;
                    }
                    else{
                        String override = checkPatchOverrideSupport(http_request);
                        if(override.equals("yes")){
                            restful=true;
                        }
                        else if(override.equals("no")){
                            writeErrorResponse("Improper request method for updating, PATCH to remove keys from this RERUM object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        }
                        else if(override.equals("improper")){
                            //Error response returned by checkPatchOverrideSupport, don't double up and put one here
                        }
                        
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "release":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("PATCH")){
                        restful = true;
                    }
                    else{
                        writeErrorResponse("Improper request method for updating, please use PATCH to alter this RERUM object.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "create":
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("POST")){
                        restful = true;
                    }
                    else{
                        writeErrorResponse("Improper request method for creating, please use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "delete":
                System.out.println("Method delete detected");
                auth_verified =  verifyAccess(access_token);
                if(auth_verified){
                    if(requestMethod.equals("DELETE")){
                        restful = true;
                    }
                    else{
                        writeErrorResponse("Improper request method for deleting, please use DELETE.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    }
                }
                else{
                    if("".equals(access_token)){
                        writeErrorResponse("Improper or missing Authorization header provided on request.  Required header must be 'Authorization: Bearer {token}'.", HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    else{
                        writeErrorResponse("Could not authorize you to perform this action.  Have you registered at "+Constant.RERUM_PREFIX, HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            break;
            case "get":
                auth_verified = true;
                if(requestMethod.equals("GET") || requestMethod.equals("HEAD")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for reading, please use GET or request for headers with HEAD.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            case "token":
                auth_verified = true;
                if(requestMethod.equals("POST")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for the Auth0 proxy, please use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            case "getProps":
                //This is a getByProperties request, so it acts like a GET, but has body like POST (and putting body in it forces POST method even when GET is set). 
                auth_verified = true;
                if(requestMethod.equals("POST")){
                    restful = true;
                }
                else{
                    writeErrorResponse("Improper request method for requesting objects with matching properties.  Use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            break;
            default:
                writeErrorResponse("Improper request method for this type of request (unknown).", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }   
        System.out.println(request_type+ " approved? "+restful);
        return restful;
    }
    
    /**
     * Since programming languages with HTTP packages don't all support PATCH, we have to detect the workaround.
     * If the user supplies the header
     * @param http_request the request that was made so we can check its headers.
     * @return 
     */
    private String checkPatchOverrideSupport(HttpServletRequest http_request){
        String overrideStatus = "no";
        String overrideHeader = http_request.getHeader("X-HTTP-Method-Override");
        String methodUsed = http_request.getMethod();
        if(null != overrideHeader){
            if(overrideHeader.equals("PATCH") && methodUsed.equals("POST")){
                overrideStatus = "yes";
            }
            else{
                overrideStatus = "improper";
                writeErrorResponse("The use of the override header for 'PATCH' requests is allowed.  The method must be 'POST' and the header value must be 'PATCH'", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }
        return overrideStatus;
    }
    
    /**
     * All actions come here to process the request body. We check if it is JSON.
     * DELETE is a special case because the content could be JSON or just the @id string and it only has to specify a content type if passing a JSONy object.  
     * and pretty format it. Returns pretty stringified JSON or fail to null.
     * Methods that call this should handle requestBody==null as unexpected.
     * @param http_request Incoming request to check.
     * @param supportStringID The request may be allowed to pass the @id as the body.
     * @return String of anticipated JSON format.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @throws java.lang.Exception
     */
    public String processRequestBody(HttpServletRequest http_request, boolean supportStringID) throws IOException, ServletException, Exception{
        String cType = http_request.getContentType();
        http_request.setCharacterEncoding("UTF-8");
        String requestBody;
        JSONObject complianceInfo = new JSONObject();
        /* UTF-8 special character support for requests */
        //http://biercoff.com/malformedinputexception-input-length-1-exception-solution-for-scala-and-java/
        ServletInputStream input = http_request.getInputStream();
        //CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        //decoder.onMalformedInput(CodingErrorAction.REPLACE);
        InputStreamReader reader = new InputStreamReader(input, "utf-8");
        //bodyReader = new BufferedReader( reader );
        //System.out.println("Process req body...");
        bodyReader = new BufferedReader(reader);
        //System.out.println("...got reader");
        bodyString = new StringBuilder();
        String line;
        JSONObject test;
        JSONArray test2;
        if(null!=cType && (cType.contains("application/json") || cType.contains("application/ld+json"))){
            //System.out.println("Processing because it was jsony");
            //Note special characters cause this to break right here. 
            try{
                while ((line = bodyReader.readLine()) != null)
                {
                  bodyString.append(line);
                }
            }
            catch(Exception e){
                System.out.println("Couldn't access body to read");
                System.out.println(e);
            }
            
            //System.out.println("built body string");
            requestBody = bodyString.toString();
            //System.out.println("here is the bodyString");
            //System.out.println(requestBody);
            try{ 
              //JSONObject test
              test = JSONObject.fromObject(requestBody);
            }
            catch(Exception ex){
                System.out.println("not a json object for processing");
                if(supportStringID){
                    //We do not allow arrays of ID's for DELETE, so if it failed JSONObject parsing then this is a hard fail for DELETE.
                    //They attempted to provide a JSON object for DELETE but it was not valid JSON
                    writeErrorResponse("The data passed was not valid JSON.  Could not get @id: "+requestBody, HttpServletResponse.SC_BAD_REQUEST);
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
            if(supportStringID){ //Content type is not JSONy, looking for @id string as body
                while ((line = bodyReader.readLine()) != null)
                {
                  bodyString.append(line);
                }
                requestBody = bodyString.toString(); 
                try{
                    test=JSONObject.fromObject(requestBody);
                    if(test.containsKey("@id")){
                        requestBody = test.getString("@id");
                        if("".equals(requestBody)){
                        //No ID provided
                            writeErrorResponse("Must provide an id or a JSON object containing @id of object to perform this action.", HttpServletResponse.SC_BAD_REQUEST);
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
                }
                catch (Exception e){
                    //This is good, they should not be using a JSONObject
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
        reader.close();
        input.close();
        content = requestBody;
        response.setContentType("application/json; charset=utf-8"); // We create JSON objects for the return body in most cases.  
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Allow-Methods", "GET,OPTIONS,HEAD,PUT,PATCH,DELETE,POST"); // Must have OPTIONS for @webanno 
        //System.out.println("requestBody in processRequestBody:"+requestBody);
        String medistring = requestBody;
        requestBody=medistring.replace("@","");
        System.out.println("requestBody in processRequestBody:"+requestBody);
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
        response.setContentType("UTF-8");
        System.out.println("inside addWebAnnotationHeaders ");
        if(isLD){
            response.addHeader("Content-Type", "application/ld+json;charset=utf-8;profile=\"http://www.w3.org/ns/anno.jsonld\""); 
        } 
        else {
            response.addHeader("Content-Type", "application/json;charset=utf-8;"); 
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
        System.out.println("End of addWebAnnotationHeaders ");
    }
    
     /**
     * Creates and appends headers to the HTTP response required by JSON-LD. 
     * This is specifically for responses that are not Web Annotation compliant (getByProperties, getAllDescendants(), getAllAncestors()).
     * They still need the JSON-LD support headers.
     * Headers are attached and read from {@link #response}. 
     * 
     * @param etag A unique fingerprint for the object for the Etag header.
     * @param isContainerType A boolean noting whether or not the object is a container type.
     * @param isLD  the object is either plain JSON or is JSON-LD ("ld+json")
     */
    private void addSupportHeaders(String etag, Boolean isLD){
        response.setContentType("UTF-8");
        if(isLD){
            response.addHeader("Content-Type", "application/ld+json;charset=utf-8;profile=\"http://www.w3.org/ns/anno.jsonld\""); 
        } 
        else {
            response.addHeader("Content-Type", "application/json;charset=utf-8;"); 
            // This breaks Web Annotation compliance, but allows us to return requested
            // objects without misrepresenting the content.
        }
        response.addHeader("Link", "<http://store.rerum.io/v1/context.json>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"");
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
        System.out.println("Inside addLocationHeader");
        if (response.containsHeader("Location")) {
            // add to existing header
            System.out.println("Inside contains header");
            addLocation = ((HttpServletRequest) response).getHeader("Location").concat(",")
                    .concat(obj.getString("id"));
            System.out.println("addLocation" + addLocation);
        }
        else {
            // no header attached yet
            System.out.println("Inside no header");
            System.out.println("obj inside no header"+obj);
            addLocation = obj.getString("id");
            System.out.println("addLocation" + addLocation);

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
     * Public facing servlet action to find all upstream versions of an object.  This is the action the user hits with the API.
     * If this object is `prime`, it will be the only object in the array.
     * @param oid variable assigned by urlrewrite rule for /id in urlrewrite.xml
     * @respond JSONArray to the response out for parsing by the client application.
     * @throws Exception 
     */
    public void getAllAncestors() throws Exception{
        if(null != oid && methodApproval(request, "get")){
            BasicDBObject query = new BasicDBObject();
            query.append("_id", oid);
            BasicDBObject mongo_obj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != mongo_obj){
                JSONObject safe_received = JSONObject.fromObject(mongo_obj); //We can trust this is the object as it exists in mongo
                List<DBObject> ls_versions = getAllVersions(safe_received);
                JSONArray ancestors = JSONArray.fromObject(getAllAncestors(ls_versions, safe_received, new JSONArray()));
                for(int x=0; x<ancestors.size(); x++){
                    expandPrivateRerumProperty(ancestors.getJSONObject(x));
                }
                try {
                    //@cubap @theHabes TODO how can we make this Web Annotation compliant?
                    addSupportHeaders("", true);
                    addLocationHeader(ancestors);
                    response.addHeader("Access-Control-Allow-Origin", "*"); 
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(ancestors));
                } 
                catch (IOException ex) {
                    Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                writeErrorResponse("No object found with provided id '"+oid+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Internal method to filter ancestors upstream from `key object` until `root`. It should always receive a reliable object, not one from the user.
     * This list WILL NOT contains the keyObj.
     * 
     *  "Get requests can't have body"
     *  In fact in the standard they can (at least nothing says they can't). But lot of servers and firewall implementation suppose they can't 
     *  and drop them so using body in get request is a very bad idea.
     * 
     * @param ls_versions all the versions of the key object on all branches
     * @param keyObj The object from which to start looking for ancestors.  It is not included in the return. 
     * @param discoveredAncestors The array storing the ancestor objects discovered by the recursion.
     * @return array of objects
     */
    private JSONArray getAllAncestors(List<DBObject> ls_versions, JSONObject keyObj, JSONArray discoveredAncestors) {
        String previousID = keyObj.getJSONObject("__rerum").getJSONObject("history").getString("previous"); //The first previous to look for
        String rootCheck = keyObj.getJSONObject("__rerum").getJSONObject("history").getString("prime"); //Make sure the keyObj is not root.
        //@cubap @theHabes #44.  What if obj does not have __rerum or __rerum.history
        for (DBObject thisVersion : ls_versions) {
            JSONObject thisObject = JSONObject.fromObject(thisVersion);      
            if("root".equals(rootCheck)){
                //Check if we found root when we got the last object out of the list.  If so, we are done.  If keyObj was root, it will be detected here.  Break out. 
                break;
            }
            else if(thisObject.getString("@id").equals(previousID)){
                //If this object's @id is equal to the previous from the last object we found, its the one we want.  Look to its previous to keep building the ancestors Array.   
                previousID = thisObject.getJSONObject("__rerum").getJSONObject("history").getString("previous");
                rootCheck = thisObject.getJSONObject("__rerum").getJSONObject("history").getString("prime");
                if("".equals(previousID) && !"root".equals(rootCheck)){
                    //previous is blank and this object is not the root.  This is gunna trip it up.  
                    //@cubap Yikes this is a problem.  This branch on the tree is broken...what should we tell the user?  How should we handle?
                    
                    break;
                }
                else{
                    discoveredAncestors.add(thisObject);
                    //Recurse with what you have discovered so far and this object as the new keyObj
                    getAllAncestors(ls_versions, thisObject, discoveredAncestors);
                    break;
                }
            }                  
        }
        return discoveredAncestors;
    }
    
    /**
     * Public facing servlet to gather for all versions downstream from a provided `key object`.
     * @param oid variable assigned by urlrewrite rule for /id in urlrewrite.xml
     * @throws java.lang.Exception
     * @respond JSONArray to the response out for parsing by the client application.
     */
    public void getAllDescendants() throws Exception {
       if(null != oid && methodApproval(request, "get")){
            BasicDBObject query = new BasicDBObject();
            query.append("_id", oid);
            BasicDBObject mongo_obj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
            if(null != mongo_obj){
                JSONObject safe_received = JSONObject.fromObject(mongo_obj); //We can trust this is the object as it exists in mongo
                List<DBObject> ls_versions = getAllVersions(safe_received);
                JSONArray descendants = JSONArray.fromObject(getAllDescendants(ls_versions, safe_received, new JSONArray()));
                for(int x=0; x<descendants.size(); x++){
                    expandPrivateRerumProperty(descendants.getJSONObject(x));
                }
                try {
                    //@cubap @theHabes TODO how can we make this Web Annotation compliant?
                    addSupportHeaders("", true);
                    addLocationHeader(descendants);
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(descendants));
                } 
                catch (IOException ex) {
                    Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                writeErrorResponse("No object found with provided id '"+oid+"'.", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    /**
     * Internal method to find all downstream versions of an object.  It should always receive a reliable object, not one from the user.
     * If this object is the last, the return will be an empty JSONArray.  The keyObj WILL NOT be a part of the array.  
     * @param  ls_versions All the given versions, including root, of a provided object.
     * @param  keyObj The provided object
     * @param  discoveredDescendants The array storing the descendants objects discovered by the recursion.
     * @return All the objects that were deemed descendants in a JSONArray
     */

    private JSONArray getAllDescendants(List<DBObject> ls_versions, JSONObject keyObj, JSONArray discoveredDescendants){
        JSONArray nextIDarr = new JSONArray();
        //@cubap @theHabes #44.  What if obj does not have __rerum or __rerum.history
        if(keyObj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next").isEmpty()){
            //essentially, do nothing.  This branch is done.
        }
        else{
            //The provided object has nexts, get them to add them to known descendants then check their descendants.
            nextIDarr = keyObj.getJSONObject("__rerum").getJSONObject("history").getJSONArray("next");
        }      
        for(int m=0; m<nextIDarr.size(); m++){ //For each id in the array
            String nextID = nextIDarr.getString(m);
            for (DBObject thisVersion : ls_versions) {
                JSONObject thisObject = JSONObject.fromObject(thisVersion);
                if(thisObject.getString("@id").equals(nextID)){ //If it is equal, add it to the known descendants
                    //Recurse with what you have discovered so far and this object as the new keyObj
                    discoveredDescendants.add(thisObject);
                    getAllDescendants(ls_versions, thisObject, discoveredDescendants);
                    break;
                }
            }
        }
        return discoveredDescendants;
    }
    
    /**
     * Internal private method to loads all derivative versions from the `root` object. It should always receive a reliable object, not one from the user.
     * Used to resolve the history tree for storing into memory.
     * @param  obj A JSONObject to find all versions of.  If it is root, make sure to prepend it to the result.  If it isn't root, query for root from the ID
     * found in prime using that result as a reliable root object. 
     * @return All versions from the store of the object in the request
     * @throws Exception 
     */
    private List<DBObject> getAllVersions(JSONObject obj) throws Exception {
        List<DBObject> ls_versions = null;
        BasicDBObject rootObj;
        BasicDBObject query = new BasicDBObject();
        BasicDBObject queryForRoot = new BasicDBObject();  
        String primeID;
        //@cubap @theHabes #44.  What if obj does not have __rerum or __rerum.history
        if(obj.getJSONObject("__rerum").getJSONObject("history").getString("prime").equals("root")){
            primeID = obj.getString("@id");
            //Get all objects whose prime is this things @id
            query.append("__rerum.history.prime", primeID);
            ls_versions = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            for(int i=0 ; i<ls_versions.size(); i++){
                BasicDBObject version = (BasicDBObject)ls_versions.get(i);
                version.remove("_id");
                ls_versions.set(i, version);
            }
            rootObj = (BasicDBObject) JSON.parse(obj.toString()); 
            rootObj.remove("_id");
            //Prepend the rootObj we know about
            ls_versions.add(0, rootObj);
        }
        else{
            primeID = obj.getJSONObject("__rerum").getJSONObject("history").getString("prime");
            //Get all objects whose prime is equal to this ID
            query.append("__rerum.history.prime", primeID);
            ls_versions = mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
            for(int i=0 ; i<ls_versions.size(); i++){
                BasicDBObject version = (BasicDBObject)ls_versions.get(i);
                version.remove("_id");
                ls_versions.set(i, version);
            }
            queryForRoot.append("@id", primeID);
            rootObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, queryForRoot);
            //This is called by getAllAncestors and getAllDescendants which is why they do not expandPrivateRerumProperty for their return.
            rootObj.remove("_id");
            //Prepend the rootObj whose ID we knew and we queried for
            ls_versions.add(0, rootObj);
        }
        return ls_versions;
    }
        
        
    /**
     * Get annotation by objectiD.  Strip all unnecessary key:value pairs before returning.
     * @param oid variable assigned by urlrewrite rule for /id in urlrewrite.xml
     * @rspond with the new annotation ID in the Location header and the new object created in the body.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void getByID() throws IOException, ServletException, Exception{
         try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAR6RLDQ4RCG5E7PV7", "yrBoWi2+Sz+ifMUczf8tHUX7SCe1Zv4PF66WQ52I");
            client = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_1).build();
            dynamoDB = new DynamoDB(client);
            tableName = "rerum_dev";
        }
        catch(Exception e){
            System.out.println("AWS initialization error below");
            System.out.println(e);
        }   
        System.out.println("getByID Test again");
        request.setCharacterEncoding("UTF-8");
       Table table = dynamoDB.getTable(tableName);
        if(null != oid && methodApproval(request, "get")){
            //find one version by objectID
            BasicDBObject query = new BasicDBObject();
           // query.append("_id", oid);
           //System.out.println("oid in getByID"+oid);
           /*oid= Constant.RERUM_ID_PREFIX+oid;*/
           oid="http://ec2-50-17-144-87.compute-1.amazonaws.com:8080/v1/id/"+oid;
           System.out.println("oid in getByID"+oid);
            Item item = table.getItem("id", oid);
            //System.out.println("item in getByID"+item);
            //String json_obj = item.toJSON();
            //Object jso = json_obj;
            //System.out.println("json_obj is:"+json_obj);
            //DBObject myAnno = mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
           
            if(null != item){
                //BasicDBObject bdbo = (BasicDBObject) myAnno;
                //JSONObject jo = JSONObject.fromObject(myAnno.toMap());
                System.out.println("item in getByID" + item);
                String json_obj = item.toJSON();
                Object jso = json_obj;
               
                JSONObject jo = JSONObject.fromObject(jso);
                System.out.println("jo in getByID"+jo);
                String idForHeader = jo.getString("id");
                System.out.println("idForHeader:"+idForHeader);
                //The following are rerum properties that should be stripped.  They should be in __rerum.
                //jo.remove("id");
                jo.remove("addedTime");
                jo.remove("originalAnnoID");
                jo.remove("version");
                jo.remove("permission");
                jo.remove("forkFromID"); // retained for legacy v0 objects
                jo.remove("serverName");
                jo.remove("serverIP");
                expandPrivateRerumProperty(jo);
                // @context may not be here and shall not be added, but the response
                // will not be ld+json without it.
                try {
                    addWebAnnotationHeaders(oid, isContainerType(jo), isLD(jo));
                    System.out.println("jo in getByID before addlocationheader"+jo);
                    addLocationHeader(jo);
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
        System.out.println("batch save");
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
                //System.out.println("batch save from copy off to bulk save from copy sending");
                //System.out.println(dbo.toString());
                newResources = mongoDBService.bulkSaveFromCopy(Constant.COLLECTION_ANNOTATION, dbo);
            }
            else {
                // empty array
            }
            for(int x=0; x<newResources.size(); x++){
                expandPrivateRerumProperty(newResources.getJSONObject(x));
            }
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_CREATED);
            jo.element("new_resources", newResources);
            try {
                addSupportHeaders("", true);
                addLocationHeader(newResources);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.addHeader("Access-Control-Allow-Origin", "*");
                out = response.getWriter();
                out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
            } catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Save a new annotation provided by the user. 
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @respond with new @id in Location header and the new annotation in the body.
     */
    public void saveNewObject() throws IOException, ServletException, Exception{
        
       System.out.println("create object Test again");
        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAR6RLDQ4RCG5E7PV7", "yrBoWi2+Sz+ifMUczf8tHUX7SCe1Zv4PF66WQ52I");
            client = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_1).build();
            dynamoDB = new DynamoDB(client);
            tableName = "rerum_dev";
        }
        catch(Exception e){
            System.out.println("AWS initialization error below");
            System.out.println(e);
        } 
        
        System.out.println("request:"+request);

        if(null != processRequestBody(request, false) && methodApproval(request, "create")){
            System.out.println("process and approval over.  actually save now");
            JSONObject received = JSONObject.fromObject(content);
            System.out.println("received.toString() at the top level before configureRerumOptions"+received.toString());
            if(received.containsKey("@id") && !received.getString("@id").isEmpty()){
                writeErrorResponse("Object already contains an @id "+received.containsKey("@id")+".  Either remove this property for saving or if it is a RERUM object update instead.", HttpServletResponse.SC_BAD_REQUEST);
            }
            else{
                //JSONObject iiif_validation_response ;//checkIIIFCompliance(received, true); //This boolean should be provided by the user somehow.  It is a intended-to-be-iiif flag
                received = configureRerumOptions(received, false);
                System.out.println("received after configureRerumOptions:"+received.toString());
                received.remove("_id");
                DBObject dbo = (DBObject) JSON.parse(received.toString());
                if(null!=request.getHeader("Slug")){
                    // Slug is the user suggested ID for the annotation. This could be a cool RERUM thing.
                    // cubap: if we want, we can just copy the Slug to @id, warning
                    // if there was some mismatch, since versions are fine with that.
                }
               /* Table table = dynamoDB.getTable(tableName);
                Item item = new Item().withPrimaryKey("id", "friday-insert")
                                      .withNumber("Misc_Field", 21004)
                                      .withNumber("Random_Char", 2500);
	        table.putItem(item);*/
                    UUID uniqueKey = UUID.randomUUID();
                    String uniquekeyid = uniqueKey.toString();
                    String primaryKeyId = uniquekeyid.replace("-","");
                    System.out.println("primaryKeyId in saveNewObject"+primaryKeyId);
                   Table table = dynamoDB.getTable(tableName);
                   System.out.println("received.toString()"+received.toString());
                   String newPrimaryKeyId = "http://ec2-50-17-144-87.compute-1.amazonaws.com:8080/v1/id/"+primaryKeyId;
               Item item = new Item().withPrimaryKey("id", newPrimaryKeyId)
                                     .withJSON("alpha"/*Constant.COLLECTION_ANNOTATION*/, received.toString());
	        table.putItem(item);
                item = table.getItem("id", newPrimaryKeyId);
	        String json_obj;
	        json_obj = item.toJSON();
                
               logger.debug(String.format("newPrimaryKeyId in saveNewObject = %s", newPrimaryKeyId));
               logger.debug(String.format("json_obj in saveNewObject = %s", json_obj));
                //logger.debug("newPrimaryKeyId in saveNewObject {}.", newPrimaryKeyId);
                System.out.println("newPrimaryKeyId in saveNewObject :"+newPrimaryKeyId);
                System.out.println("json_obj in saveNewObject :"+json_obj);
               //String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                //set @id from _id and update the annotation
              // BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                //String newid = Constant.RERUM_ID_PREFIX+newObjectID;
                String newid = Constant.RERUM_ID_PREFIX+primaryKeyId;
                //dboWithObjectID.put("@id", newid);
                //mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
               JSONObject jo = new JSONObject();
                //JSONObject newObjWithID = JSONObject.fromObject(dboWithObjectID);
               //item = table.getItem("id", newPrimaryKeyId);
               //String json_obj = item.toJSON();
              // Object obj = json_obj;
              JSONObject finalJSONObject = JSONObject.fromObject(json_obj);

                //JsonObject convertedObject = new Gson().fromJson(finalJSONObject, JsonObject.class);
                expandPrivateRerumProperty(finalJSONObject);
                jo.element("code", HttpServletResponse.SC_CREATED);
                //newid.remove("_id");
                jo.element("new_obj_state", finalJSONObject);
                //jo.element("iiif_validation", iiif_validation_response);
                //try {
                System.out.println("Object created: "+finalJSONObject);
                try {
                    addWebAnnotationHeaders(newid, isContainerType(received), isLD(received));
                    addLocationHeader(finalJSONObject);
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out = response.getWriter();
                    out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                } 
                catch (IOException ex) {
                    Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Public facing servlet to PATCH set values of an existing RERUM object.
     * @respond with state of new object in the body
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void patchSetUpdate()throws IOException, ServletException, Exception{
        Boolean historyNextUpdatePassed = false;
        System.out.println("patch set update");
        if(null!= processRequestBody(request, true) && methodApproval(request, "set")){
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content); 
            if(received.containsKey("@id")){
                String updateHistoryNextID = received.getString("@id");
                query.append("@id", updateHistoryNextID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
                BasicDBObject updatedObject = (BasicDBObject) originalObject.copy(); //A copy of the original, this will be saved as a new object.  Make all edits to this variable.
                boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(originalObject));
                boolean isReleased = checkIfReleased(JSONObject.fromObject(originalObject));
                if(alreadyDeleted){
                    writeErrorResponse("The object you are trying to update is deleted.", HttpServletResponse.SC_FORBIDDEN);
                }
                else if(isReleased){
                    writeErrorResponse("The object you are trying to update is released. Fork to make changes.", HttpServletResponse.SC_FORBIDDEN);
                }
                else{
                    if(null != originalObject){
                        Set<String> update_anno_keys = received.keySet();
                        int updateCount = 0;
                        //If the object already in the database contains the key found from the object recieved from the user...
                        for(String key : update_anno_keys){
                            if(originalObject.containsKey(key)){ //Keys matched.  Ignore it, set only works for new keys.
                                //@cubap @theHabes do we want to build that this happened into the response at all?
                            }
                            else{ //this is a new key, this is a set. Allow null values.
                                updatedObject.append(key, received.get(key));
                                updateCount += 1;
                            }
                        }
                        if(updateCount > 0){
                            JSONObject newObject = JSONObject.fromObject(updatedObject);//The edited original object meant to be saved as a new object (versioning)
                            newObject = configureRerumOptions(newObject, true); //__rerum for the new object being created because of the update action
                            newObject.remove("@id"); //This is being saved as a new object, so remove this @id for the new one to be set.
                            //Since we ignore changes to __rerum for existing objects, we do no configureRerumOptions(updatedObject);
                            DBObject dbo = (DBObject) JSON.parse(newObject.toString());
                            String newNextID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                            String newNextAtID = Constant.RERUM_ID_PREFIX+newNextID;
                            BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                            dboWithObjectID.append("@id", newNextAtID);
                            newObject.element("@id", newNextAtID);
                            expandPrivateRerumProperty(newObject);
                            newObject.remove("_id");
                            mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                            historyNextUpdatePassed = alterHistoryNext(updateHistoryNextID, newNextAtID); //update history.next or original object to include the newObject @id
                            if(historyNextUpdatePassed){
                                System.out.println("object patch set updated: "+newNextAtID);
                                JSONObject jo = new JSONObject();
                                JSONObject iiif_validation_response = checkIIIFCompliance(newNextAtID, "2.1");
                                jo.element("code", HttpServletResponse.SC_OK);
                                jo.element("original_object_id", updateHistoryNextID);
                                jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                                jo.element("iiif_validation", iiif_validation_response);
                                try {
                                    addWebAnnotationHeaders(newNextID, isContainerType(newObject), isLD(newObject));
                                    addLocationHeader(newObject);
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
                            // Nothing could be patched
                            addLocationHeader(received);
                            writeErrorResponse("Nothing could be PATCHed", HttpServletResponse.SC_NO_CONTENT);
                        }
                    }
                    else{
                        //This could means it was an external object, but those fail for PATCH updates.
                        writeErrorResponse("Object "+received.getString("@id")+" not found in RERUM, could not update.  PUT update to make this object a part of RERUM.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Public facing servlet to PATCH unset values of an existing RERUM object.
     * @respond with state of new object in the body
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void patchUnsetUpdate()throws IOException, ServletException, Exception{
        Boolean historyNextUpdatePassed = false;
        System.out.println("Patch unset update");
        if(null!= processRequestBody(request, true) && methodApproval(request, "unset")){
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content); 
            if(received.containsKey("@id")){
                String updateHistoryNextID = received.getString("@id");
                query.append("@id", updateHistoryNextID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
                BasicDBObject updatedObject = (BasicDBObject) originalObject.copy(); //A copy of the original, this will be saved as a new object.  Make all edits to this variable.
                boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(originalObject));
                boolean isReleased = checkIfReleased(JSONObject.fromObject(originalObject));
                if(alreadyDeleted){
                    writeErrorResponse("The object you are trying to update is deleted.", HttpServletResponse.SC_FORBIDDEN);
                }
                else if(isReleased){
                    writeErrorResponse("The object you are trying to update is released.  Fork to make changes.", HttpServletResponse.SC_FORBIDDEN);
                }
                else{
                    if(null != originalObject){
                        Set<String> update_anno_keys = received.keySet();
                        int updateCount = 0;
                        //If the object already in the database contains the key found from the object recieved from the user...
                        for(String key : update_anno_keys){
                            if(originalObject.containsKey(key)){
                                if(key.equals("@id") || key.equals("__rerum") || key.equals("objectID") || key.equals("_id") ){
                                    // Ignore these in a PATCH.  DO NOT update, DO NOT count as an attempt to update
                                }
                                else{
                                    if(null != received.get(key)){ //Found matching keys and value is not null.  Ignore these.
                                       //@cubap @theHabes do we want to build that this happened into the response at all?
                                    }  
                                    else{ //Found matching keys and value is null, this is an unset
                                        updatedObject.remove(key);
                                        updateCount +=1;
                                    }
                                }
                            }
                            else{ //Original object does not contain this key, perhaps the user meant set.
                                //@cubap @theHabes do we want to build that this happened into the response at all?
                            }
                        }
                        if(updateCount > 0){
                            JSONObject newObject = JSONObject.fromObject(updatedObject);//The edited original object meant to be saved as a new object (versioning)
                            newObject = configureRerumOptions(newObject, true); //__rerum for the new object being created because of the update action
                            newObject.remove("@id"); //This is being saved as a new object, so remove this @id for the new one to be set.
                            //Since we ignore changes to __rerum for existing objects, we do no configureRerumOptions(updatedObject);
                            DBObject dbo = (DBObject) JSON.parse(newObject.toString());
                            String newNextID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                            String newNextAtID = Constant.RERUM_ID_PREFIX+newNextID;
                            BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                            dboWithObjectID.append("@id", newNextAtID);
                            newObject.element("@id", newNextAtID);
                            expandPrivateRerumProperty(newObject);
                            newObject.remove("_id");
                            mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                            historyNextUpdatePassed = alterHistoryNext(updateHistoryNextID, newNextAtID); //update history.next or original object to include the newObject @id
                            if(historyNextUpdatePassed){
                                System.out.println("Patch unset updated: "+newNextAtID);
                                JSONObject jo = new JSONObject();
                                JSONObject iiif_validation_response = checkIIIFCompliance(newNextAtID, "2.1");
                                jo.element("code", HttpServletResponse.SC_OK);
                                jo.element("original_object_id", updateHistoryNextID);
                                jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                                jo.element("iiif_validation", iiif_validation_response);
                                try {
                                    addWebAnnotationHeaders(newNextID, isContainerType(newObject), isLD(newObject));
                                    addLocationHeader(newObject);
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
                            // Nothing could be patched
                            addLocationHeader(received);
                            writeErrorResponse("Nothing could be PATCHed", HttpServletResponse.SC_NO_CONTENT);
                        }
                    }
                    else{
                        //This could means it was an external object, but those fail for PATCH updates.
                        writeErrorResponse("Object "+received.getString("@id")+" not found in RERUM, could not update.  PUT update to make this object a part of RERUM.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Update a given annotation. Cannot set or unset keys.  
     * @respond with state of new object in the body
     */
    public void patchUpdateObject() throws ServletException, Exception{
        Boolean historyNextUpdatePassed = false;
        System.out.println("trying to patch");
        if(null!= processRequestBody(request, true) && methodApproval(request, "patch")){
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content); 
            if(received.containsKey("@id")){
                String updateHistoryNextID = received.getString("@id");
                query.append("@id", updateHistoryNextID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
                boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(originalObject));
                boolean isReleased = checkIfReleased(JSONObject.fromObject(originalObject));
                if(alreadyDeleted){
                    writeErrorResponse("The object you are trying to update is deleted.", HttpServletResponse.SC_FORBIDDEN);
                }
                else if(isReleased){
                    writeErrorResponse("The object you are trying to update is released.  Fork to make changes.", HttpServletResponse.SC_FORBIDDEN);
                }
                else{
                    if(null != originalObject){
                        BasicDBObject updatedObject = (BasicDBObject) originalObject.copy(); //A copy of the original, this will be saved as a new object.  Make all edits to this variable.
                        Set<String> update_anno_keys = received.keySet();
                        boolean triedToSet = false;
                        int updateCount = 0;
                        //If the object already in the database contains the key found from the object recieved from the user, update it barring a few special keys
                        //Users cannot update the __rerum property, so we ignore any update action to that particular field.  
                        for(String key : update_anno_keys){
                            if(originalObject.containsKey(key) ){
                                //Skip keys we want to ignore and keys that match but have matching values
                                if(!(key.equals("@id") || key.equals("__rerum") || key.equals("objectID") || key.equals("_id")) && received.get(key) != originalObject.get(key)){
                                    updatedObject.remove(key);
                                    updatedObject.append(key, received.get(key));
                                    updateCount +=1 ;
                                }
                            }
                            else{
                                triedToSet = true;
                               // break;
                            }
                        }
                        if(triedToSet){
                            System.out.println("Patch meh 1");
                            //@cubap @theHabes We continued with what we could patch.  Do we tell the user at all?
                            //writeErrorResponse("A key you are trying to update does not exist on the object.  You can set with the patch_set or put_update action.", HttpServletResponse.SC_BAD_REQUEST);
                        }
                        else if(updateCount == 0){
                            System.out.println("Patch meh 2");
                            addLocationHeader(received);
                            writeErrorResponse("Nothing could be PATCHed", HttpServletResponse.SC_NO_CONTENT);
                        }
                        else{
                            JSONObject newObject = JSONObject.fromObject(updatedObject);//The edited original object meant to be saved as a new object (versioning)
                            newObject = configureRerumOptions(newObject, true); //__rerum for the new object being created because of the update action
                            newObject.remove("@id"); //This is being saved as a new object, so remove this @id for the new one to be set.
                            //Since we ignore changes to __rerum for existing objects, we do no configureRerumOptions(updatedObject);
                            DBObject dbo = (DBObject) JSON.parse(newObject.toString());
                            String newNextID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                            String newNextAtID = Constant.RERUM_ID_PREFIX+newNextID;
                            BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                            dboWithObjectID.append("@id", newNextAtID);
                            newObject.element("@id", newNextAtID);
                            expandPrivateRerumProperty(newObject);
                            newObject.remove("_id");
                            mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                            historyNextUpdatePassed = alterHistoryNext(updateHistoryNextID, newNextAtID); //update history.next or original object to include the newObject @id
                            if(historyNextUpdatePassed){
                                System.out.println("Patch updated object: "+newNextAtID);
                                JSONObject jo = new JSONObject();
                                JSONObject iiif_validation_response = checkIIIFCompliance(newNextAtID, "2.1");
                                jo.element("code", HttpServletResponse.SC_OK);
                                jo.element("original_object_id", updateHistoryNextID);
                                jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                                jo.element("iiif_validation", iiif_validation_response);
                                try {
                                    addWebAnnotationHeaders(newNextID, isContainerType(newObject), isLD(newObject));                                  
                                    addLocationHeader(newObject);
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
                    }
                    else{
                        writeErrorResponse("Object "+received.getString("@id")+" not found in RERUM, could not patch update.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not patch update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
     /**
     * Public facing servlet to PUT replace an existing object.  Can set and unset keys.
     * @respond with new state of the object in the body.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void putUpdateObject()throws IOException, ServletException, Exception{
        //@webanno The client should use the If-Match header with a value of the ETag it received from the server before the editing process began, 
        //to avoid collisions of multiple users modifying the same Annotation at the same time
        //cubap: I'm not sold we have to do this. Our versioning would allow multiple changes. 
        //The application might want to throttle internally, but it can.
        Boolean historyNextUpdatePassed = false;
        String primarykey ="";
        JSONObject newjson = new JSONObject();
        System.out.println("put update object");
        logger.debug(String.format("request in putUpdateObject = %s", request));
        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAR6RLDQ4RCG5E7PV7", "yrBoWi2+Sz+ifMUczf8tHUX7SCe1Zv4PF66WQ52I");
            client = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_1).build();
            dynamoDB = new DynamoDB(client);
            tableName = "rerum_dev";
        }
        catch(Exception e){
            System.out.println("AWS initialization error below");
            System.out.println(e);
        } 

        if(null!= processRequestBody(request, true) && methodApproval(request, "update")){
            BasicDBObject query = new BasicDBObject();
            //String medistring = content;
            //content=medistring.replace("@","");
            JSONObject received = JSONObject.fromObject(content); 
            logger.debug(String.format("content in putUpdateObject = %s", content));
            logger.debug(String.format("received in putUpdateObject = %s", received));
            //JSONArray updatedArray = (JSONArray) JSONSerializer.toJSON(content);
            //System.out.println(updatedArray.size());
          //JSONArray updatedArray = JSONArray.fromString(content);
            JSONArray updatedArray = JSONArray.fromObject(received);
            //JSONArray array = JSONArray.fromObject(content);
            System.out.println("JSONArray size"+updatedArray.size());
            //received = configureRerumOptions(received, false);
           
                
                Iterator<String> keys = received.keys();
                System.out.println(received.get("@id"));
                primarykey = received.get("@id").toString();
                System.out.println("primarykey"+primarykey);
                Map<String, Object> data = new HashMap<String, Object>();
                while(keys.hasNext()) {
                 String key = keys.next();
                 //String key = keys.get(0x0);
                 System.out.println("key:"+key);
                 //System.out.println("jsonObject.get(key) :"+json.get(key));
                 if(!key.equals("@id")){
                     System.out.println("jsonObject.get(key) :"+received.get(key));
                     data.put( key, received.get(key) );
                     
                 }
                   /* if (json.containsKey("id")) {
                     // do something with jsonObject here      
                     break;
                    }
                    else {
                        System.out.println("other objects in the putUpdate request:"+json.get("id"));
                    }*/
                }
                //JSONObject newjson = new JSONObject();
                newjson.putAll( data );
                //newjson = configureRerumOptions(received, true);
               newjson = configureRerumOptions(newjson, false);
                System.out.println("newjson in the putUpdate request:"+newjson);
               // System.out.println("id in the putUpdate request:"+json.get("id"));
            
            //System.out.println();
           //System.out.println("id in the putUpdate request:"+json.get("id"));
            Table table = dynamoDB.getTable(tableName);
            Item old_item = table.getItem("id", primarykey);
            String prev_json_obj;
	    prev_json_obj = old_item.toJSON();
            
            /*UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", primarykey)
                    .withUpdateExpression("set alpha = :alpha")
                    .withValueMap(new ValueMap().withJSON(":alpha", newjson.toString())).withReturnValues(ReturnValue.ALL_NEW);;
            
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            System.out.println(outcome.getItem().toJSONPretty());*/

            //logger.debug(String.format("received.toString in putUpdateObject = %s", received.toString()));
            if(received.containsKey("@id")){
                System.out.println("received in if of putUpdate"+received);
                UUID uniqueKey = UUID.randomUUID();
                    String uniquekeyid = uniqueKey.toString();
                    String primaryKeyId = uniquekeyid.replace("-","");
                    System.out.println("primaryKeyId in putUpdateObject"+primaryKeyId);
                   
                   //System.out.println("received.toString()"+received.toString());
                   String newPrimaryKeyId = "http://ec2-50-17-144-87.compute-1.amazonaws.com:8080/v1/id/"+primaryKeyId;
                   Item new_item = new Item().withPrimaryKey("id", newPrimaryKeyId)
                                     .withJSON("alpha", newjson.toString());
	        table.putItem(new_item);
                new_item = table.getItem("id", newPrimaryKeyId);
	        String json_obj;
	        json_obj = new_item.toJSON();
                Object new_json_jo = json_obj;
                System.out.println("new_json_jo"+new_json_jo);
                JSONObject old_json_obj = JSONObject.fromObject(prev_json_obj); 
                String updateHistoryNextID = newPrimaryKeyId;//received.getString("@id");
                System.out.println("updateHistoryNextID in putUpdateObject"+updateHistoryNextID);
                JSONObject originalProperties = old_json_obj.getJSONObject("alpha");
                System.out.println("originalProperties in putUpdateObject before next"+originalProperties);
                //originalProperties.getJSONObject("__rerum").element("isOverwritten", formattedOverwrittenDateTime)
                originalProperties.getJSONObject("__rerum").getJSONObject("history").element("next", newPrimaryKeyId);
                System.out.println("originalProperties in putUpdateObject after next"+originalProperties);
                UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", primarykey)
                                .withUpdateExpression("set alpha = :alpha")
                                .withValueMap(new ValueMap().withJSON(":alpha", originalProperties.toString())).withReturnValues(ReturnValue.ALL_NEW);
                        UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
                query.append("id", updateHistoryNextID);
                Item origObj = table.getItem("id", updateHistoryNextID);
                String orig_json_obj = origObj.toJSON();
                Object jso = json_obj;
                String received_str = received.toString();
                //SONObject newjsonobj = new JSONObject(received_str);
                //JSONParser parser = new JSONParser();
                JSONObject orig_obj = JSONObject.fromObject(jso);
                System.out.println("orig_obj in putUpdateObject"+orig_obj);
                JSONObject updatedObj = JSONObject.fromObject(received.toString());
                System.out.println("updatedObj in putUpdateObject"+updatedObj);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
                
                BasicDBObject updatedObject = (BasicDBObject) JSON.parse(received.toString()); //A copy of the original, this will be saved as a new object.  Make all edits to this variable.
                //JSONObject originalJSONObj = JSONObject.fromObject(originalObject);
                JSONObject originalJSONObj = JSONObject.fromObject(received);
                boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(originalObject));
                boolean isReleased = checkIfReleased(JSONObject.fromObject(originalObject));
                if(alreadyDeleted){
                    writeErrorResponse("The object you are trying to update is deleted.", HttpServletResponse.SC_FORBIDDEN);
                }
                else if(isReleased){
                    writeErrorResponse("The object you are trying to update is released.  Fork to make changes.", HttpServletResponse.SC_FORBIDDEN);
                }
                else{
                    if(null != originalObject){
                        JSONObject newObject = JSONObject.fromObject(updatedObject);//The edited original object meant to be saved as a new object (versioning)
                        originalProperties = originalJSONObj.getJSONObject("__rerum");
                        newObject.element("__rerum", originalProperties);
                        //Since this is a put update, it is possible __rerum is not in the object provided by the user.  We get a reliable copy oof the original out of mongo
                        newObject = configureRerumOptions(newObject, true); //__rerum for the new object being created because of the update action
                        newObject.remove("@id"); //This is being saved as a new object, so remove this @id for the new one to be set.
                        newObject.remove("_id");
                        DBObject dbo = (DBObject) JSON.parse(newObject.toString());
                        String newNextID = updateHistoryNextID;//mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
                        String newNextAtID = Constant.RERUM_ID_PREFIX+newNextID;
                        BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
                        dboWithObjectID.append("@id", newNextAtID);
                        newObject.element("@id", newNextAtID);
                        expandPrivateRerumProperty(newObject);
                        newObject.remove("_id");
                        //mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
                        historyNextUpdatePassed = alterHistoryNext(updateHistoryNextID, newNextAtID); //update history.next or original object to include the newObject @id
                        if(historyNextUpdatePassed){
                            System.out.println("Object put updated: "+newNextAtID);
                            JSONObject jo = new JSONObject();
                            JSONObject iiif_validation_response = checkIIIFCompliance(newNextAtID, "2.1");
                            jo.element("code", HttpServletResponse.SC_OK);
                            jo.element("original_object_id", updateHistoryNextID);
                            jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                            jo.element("iiif_validation", iiif_validation_response);
                            try {
                                addWebAnnotationHeaders(newNextID, isContainerType(newObject), isLD(newObject));
                                addLocationHeader(newObject);
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
                        updateExternalObject(received);
                    }
                }
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Public facing servlet to PUT overwrite an existing object.  Can set and unset keys.  There will be no reference to the node as it originally existed
     * because this intentionally avoids any versioning around the action.  It is a pure overwrite and should only be allowed for the generator of the object.
     * Do NOT overwrite __rerum, keep the original no matter what __rerum is provided by the user.
     * 
     * 
     * @respond with new state of the object in the body.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void overwriteObject()throws IOException, ServletException, Exception{
        System.out.println("overwrite object");
        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAR6RLDQ4RCG5E7PV7", "yrBoWi2+Sz+ifMUczf8tHUX7SCe1Zv4PF66WQ52I");
            client = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_1).build();
            dynamoDB = new DynamoDB(client);
            tableName = "rerum_dev";
        }
        catch(Exception e){
            System.out.println("AWS initialization error below");
            System.out.println(e);
        } 
        if(null!= processRequestBody(request, true) && methodApproval(request, "overwrite")){
            //BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content); 
            System.out.println("content in overwrite Object"+content);
            System.out.println("received in overwrite Object"+received);
            JSONObject updatedJson = new JSONObject();
            String primarykey = "";
            Table table = dynamoDB.getTable(tableName);
            if(received.containsKey("@id")){
                String receivedID = received.getString("@id");
                 //JSONObject json = (JSONObject) js;
                Iterator<String> keys = received.keys();
                System.out.println(received.get("@id"));
                primarykey = received.get("@id").toString();
                System.out.println("primarykey"+primarykey);
                Map<String, Object> data = new HashMap<String, Object>();
                while(keys.hasNext()) {
                 String key = keys.next();
                 //String key = keys.get(0x0);
                 System.out.println("key:"+key);
                 //System.out.println("jsonObject.get(key) :"+json.get(key));
                 if(!key.equals("@id")){
                     System.out.println("jsonObject.get(key) :"+received.get(key));
                     data.put( key, received.get(key) );
                     
                 }
                   /* if (json.containsKey("id")) {
                     // do something with jsonObject here      
                     break;
                    }
                    else {
                        System.out.println("other objects in the putUpdate request:"+json.get("id"));
                    }*/
                }
                //JSONObject newjson = new JSONObject();
                updatedJson.putAll( data );
                //newjson = configureRerumOptions(received, true);
               updatedJson = configureRerumOptions(updatedJson, false);
                System.out.println("updateJson in the overwrite request:"+updatedJson);
                String origObjGenerator = updatedJson.getJSONObject("__rerum").getString("history");
                System.out.println("origObjGenerator in the overwrite request:"+origObjGenerator);
                //query.append("@id", receivedID);
                //BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The originalObject DB object
              //  JSONObject originalJSONObj = JSONObject.fromObject(originalObject);
               // boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(originalObject));
               // boolean isReleased = checkIfReleased(JSONObject.fromObject(originalObject));
               // String origObjGenerator = originalJSONObj.getJSONObject("__rerum").getString("generatedBy");
               // boolean isGenerator = (origObjGenerator.equals(generatorID));
               Item item = table.getItem("id", primarykey);
               String prev_json_obj = item.toJSON();
               System.out.println("item"+item);
               System.out.println("prev_json_obj"+prev_json_obj);
               Object jso = prev_json_obj;
               System.out.println("jso"+jso);
               
               JSONObject originalJSONObj = JSONObject.fromObject(jso);  
               System.out.println("originalJSONObj in overwrite :"+originalJSONObj);
               boolean alreadyDeleted = checkIfDeleted(JSONObject.fromObject(jso));
               System.out.println("alreadyDeleted in overwrite :"+alreadyDeleted);
               boolean isReleased = checkIfReleased(JSONObject.fromObject(jso));
               System.out.println("isReleased in overwrite :"+isReleased);
               //String origObjGenerator = originalJSONObj.getJSONObject("__rerum").getString("generatedBy");
               //System.out.println("origObjGenerator in overwrite :" + origObjGenerator);
               //System.out.println("generatorID in overwrite :" + generatorID);
               //boolean isGenerator = (origObjGenerator.equals(generatorID));
               //System.out.println("isGenerator in overwrite :" + isGenerator);

                if(alreadyDeleted){
                    writeErrorResponse("The object you are trying to overwrite is deleted.", HttpServletResponse.SC_FORBIDDEN);
                }
                else if(isReleased){
                    writeErrorResponse("The object you are trying to overwrite is released.  Fork to make changes.", HttpServletResponse.SC_FORBIDDEN);
                }
                /*else if(!isGenerator){
                    writeErrorResponse("The object you are trying to overwrite was not created by you.  Fork to make changes.", HttpServletResponse.SC_UNAUTHORIZED);
                }*/
                else{
                    System.out.println("inside the else block");
                    if(null != item){
                        System.out.println("inside the if block");
                        JSONObject newObject = updatedJson;//The edited original object meant to be saved as a new object (versioning)
                        System.out.println("newObject in if block of overwriteObject"+newObject);
                        //newObject.remove("_id");
                        JSONObject originalProperties = newObject.getJSONObject("alpha");
                        //System.out.println("originalProperties in if block of overwriteObject"+originalProperties);
                        
                        LocalDateTime dt = LocalDateTime.now();
                        DateTimeFormatter dtFormat = DateTimeFormatter.ISO_DATE_TIME;
                        String formattedOverwrittenDateTime = dt.format(dtFormat);
                        originalProperties.getJSONObject("__rerum").element("isOverwritten", formattedOverwrittenDateTime);
                        //originalProperties.getJSONObject("__rerum").element("isOverwritten", formattedOverwrittenDateTime);
                        //newObject.element("__rerum", originalProperties);
                        //DBObject udbo = (DBObject) JSON.parse(newObject.toString());
                        //mongoDBService.update(Constant.COLLECTION_ANNOTATION, originalObject, udbo);
                        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", primarykey)
                                .withUpdateExpression("set alpha = :alpha")
                                .withValueMap(new ValueMap().withJSON(":alpha", originalProperties.toString())).withReturnValues(ReturnValue.ALL_NEW);
                        UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
                        System.out.println(outcome.getItem().toJSONPretty());
                        Item updatedItem = table.getItem("id", primarykey);
                        System.out.println("item in getByID" + item);
                        String json_obj = updatedItem.toJSON();
                        Object tempjso = json_obj;
               
                        newObject = JSONObject.fromObject(tempjso);
                        JSONObject jo = new JSONObject();
                        //JSONObject iiif_validation_response = checkIIIFCompliance(receivedID, "2.1");
                        System.out.println("object overwritten: "+receivedID);
                        //expandPrivateRerumProperty(newObject);
                        //newObject.remove("_id");
                        jo.element("code", HttpServletResponse.SC_OK);
                        jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                        //jo.element("iiif_validation", iiif_validation_response);
                        try {
                            System.out.println("Inside the try block");
                            addWebAnnotationHeaders(receivedID, isContainerType(newObject), isLD(newObject));
                            addLocationHeader(newObject);
                            response.addHeader("Access-Control-Allow-Origin", "*");
                            response.setStatus(HttpServletResponse.SC_OK);
                            out = response.getWriter();
                            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
                        }
                        catch (IOException ex) {
                            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not update.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
     /**
     * Public facing servlet to release an existing RERUM object.  This will not perform history tree updates, but rather releases tree updates.
     * (AKA a new node in the history tree is NOT CREATED here.)
     * 
     * @respond with new state of the object in the body.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public void releaseObject() throws IOException, ServletException, Exception{
        boolean treeHealed = false;
        boolean isGenerator = false;
        System.out.println("Release object");
        if(null!= processRequestBody(request, true) && methodApproval(request, "release")){
            BasicDBObject query = new BasicDBObject();
            JSONObject received = JSONObject.fromObject(content);
            if(received.containsKey("@id")){
                String updateToReleasedID = received.getString("@id");
                query.append("@id", updateToReleasedID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //The original DB object
                BasicDBObject releasedObject = (BasicDBObject) originalObject.copy(); //A copy of the original.  Make all edits to this variable.
                JSONObject safe_original = JSONObject.fromObject(originalObject); //The original object represented as a JSON object.  Safe for edits. 
                String previousReleasedID = safe_original.getJSONObject("__rerum").getJSONObject("releases").getString("previous");
                JSONArray nextReleases = safe_original.getJSONObject("__rerum").getJSONObject("releases").getJSONArray("next");
                boolean alreadyReleased = checkIfReleased(safe_original);
                if(alreadyReleased){
                    writeErrorResponse("This object is already released.  You must fork this annotation as one of your own to release it.", HttpServletResponse.SC_FORBIDDEN);
                }
                else{
                    if(null != originalObject){
                        String origObjGenerator = safe_original.getJSONObject("__rerum").getString("generatedBy");
                        isGenerator = (origObjGenerator.equals(generatorID));
                        if(isGenerator){
                            LocalDateTime dt = LocalDateTime.now();
                            DateTimeFormatter dtFormat = DateTimeFormatter.ISO_DATE_TIME;
                            String formattedReleasedDateTime = dt.format(dtFormat);
                            safe_original.getJSONObject("__rerum").element("isReleased", formattedReleasedDateTime);
                            safe_original.getJSONObject("__rerum").getJSONObject("releases").element("replaces", previousReleasedID);
                            releasedObject = (BasicDBObject) JSON.parse(safe_original.toString());
                            if(!"".equals(previousReleasedID)){// A releases tree exists and an ancestral object is being released.  
                                treeHealed  = healReleasesTree(safe_original); 
                            }
                            else{ //There was no releases previous value. 
                                if(nextReleases.size() > 0){ //The release tree has been established and a descendant object is now being released.
                                    treeHealed  = healReleasesTree(safe_original);
                                }
                                else{ //The release tree has not been established
                                    treeHealed = establishReleasesTree(safe_original);
                                }
                            }
                            if(treeHealed){ //If the tree was established/healed
                                //perform the update to isReleased of the object being released.  Its releases.next[] and releases.previous are already correct.
                                mongoDBService.update(Constant.COLLECTION_ANNOTATION, originalObject, releasedObject);
                                expandPrivateRerumProperty(releasedObject);
                                releasedObject.remove("_id");
                                JSONObject newObject = JSONObject.fromObject(releasedObject);
                                System.out.println("Object released: "+updateToReleasedID);
                                JSONObject jo = new JSONObject();
                                jo.element("code", HttpServletResponse.SC_OK);
                                jo.element("new_obj_state", newObject); //FIXME: @webanno standards say this should be the response.
                                jo.element("previously_released_id", previousReleasedID); 
                                jo.element("next_releases_ids", nextReleases);                           
                                try {
                                    addWebAnnotationHeaders(updateToReleasedID, isContainerType(safe_original), isLD(safe_original));
                                    addLocationHeader(newObject);
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
                            writeErrorResponse("You are not the generator of this object.  Only the agent who created this object can release it.  Agent= "+generatorID, HttpServletResponse.SC_UNAUTHORIZED);
                        }
                        
                    }
                    else{
                        //This could mean it was an external object, but the release action fails on those.
                        writeErrorResponse("Object "+received.getString("@id")+" not found in RERUM, could not release.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }    
            }
            else{
                writeErrorResponse("Object did not contain an @id, could not release.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * Internal helper method to update the releases tree from a given object that is being released.  See code in method for further documentation.
     * https://www.geeksforgeeks.org/find-whether-an-array-is-subset-of-another-array-set-1/
     * 
     * This method only receives reliable objects from mongo.
     * 
     * @param obj the RERUM object being released
     * @return Boolean success or some kind of Exception
     */
    private boolean healReleasesTree (JSONObject releasingNode) throws Exception{
        Boolean success = true;
        List<DBObject> ls_versions = getAllVersions(releasingNode);
        JSONArray descendants = getAllDescendants(ls_versions, releasingNode, new JSONArray());
        JSONArray anscestors = getAllAncestors(ls_versions, releasingNode, new JSONArray());
        for(int d=0; d<descendants.size(); d++){ //For each descendant
            JSONObject desc = descendants.getJSONObject(d);
            boolean prevMatchCheck = desc.getJSONObject("__rerum").getJSONObject("releases").getString("previous").equals(releasingNode.getJSONObject("__rerum").getJSONObject("releases").getString("previous"));
            DBObject origDesc = (DBObject) JSON.parse(desc.toString());
            if(prevMatchCheck){ 
                //If the descendant's previous matches the node I am releasing's releases.previous, swap the descendant releses.previous with node I am releasing's @id. 
                desc.getJSONObject("__rerum").getJSONObject("releases").element("previous", releasingNode.getString("@id"));
                if(!desc.getJSONObject("__rerum").getString("isReleased").equals("")){ 
                    //If this descendant is released, it replaces the node being released
                    if(desc.getJSONObject("__rerum").getJSONObject("releases").getString("previous").equals(releasingNode.getString("@id"))){
                        desc.getJSONObject("__rerum").getJSONObject("releases").element("replaces", releasingNode.getString("@id")); 
                    }
                }
                DBObject descToUpdate = (DBObject) JSON.parse(desc.toString());
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, origDesc, descToUpdate);
            }
        }
        JSONArray origNextArray = releasingNode.getJSONObject("__rerum").getJSONObject("releases").getJSONArray("next");
        for(int a=0; a<anscestors.size(); a++){ //For each ancestor
            JSONArray ancestorNextArray = anscestors.getJSONObject(a).getJSONObject("__rerum").getJSONObject("releases").getJSONArray("next");
            JSONObject ans = anscestors.getJSONObject(a);
            DBObject origAns = (DBObject) JSON.parse(ans.toString());
            if(origNextArray.size() == 0){ 
                //The releases.next on the node I am releasing is empty.  This means only other ancestors with empty releases.next[] are between me and the next ancenstral released node
                if(ancestorNextArray.size() == 0){
                    ancestorNextArray.add(releasingNode.getString("@id")); //Add the id of the node I am releasing into the ancestor's releases.next array.
                }
            }
            else{
                //The releases.next on the node I am releasing has 1 - infinity entries.  I need to check if any of the entries of that array exist in the releases.next of my ancestors and remove them before
                //adding the @id of the released node into the acenstral releases.next array.  
                for (int i=0; i<origNextArray.size(); i++){ //For each id in the next array of the object I am releasing (will not be []).
                    String compareOrigNextID = origNextArray.getString(i);
                    for(int j=0; j<ancestorNextArray.size(); j++){ //For each id in the ancestor's releases.next array
                        String compareAncestorID = ancestorNextArray.getString(j);
                        if (compareOrigNextID.equals(compareAncestorID)){ 
                            //If the id is in the next array of the object I am releasing and in the releases.next array of the ancestor
                            ancestorNextArray.remove(j); //remove that id.
                        }
                        //Whether or not the ancestral node replaces the node I am releasing or not happens in releaseObject() when I make the node I am releasing isReleased because I can use the releases.previous there.  
                        if(j == ancestorNextArray.size()-1){ //Once I have checked against all id's in the ancestor node releases.next[] and removed the ones I needed to
                            ancestorNextArray.add(releasingNode.getString("@id")); //Add the id of the node I am releasing into the ancestor's releases.next array.
                        }
                    }
                } 
            }
            ans.getJSONObject("__rerum").getJSONObject("releases").element("next", ancestorNextArray);
            DBObject ansToUpdate = (DBObject) JSON.parse(ans.toString());
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, origAns, ansToUpdate);
        }
        return success;
    } 
    
    /**
     * Internal helper method to establish the releases tree from a given object that is being released.  
     * This can probably be collapsed into healReleasesTree.  It contains no checks, it is brute force update ancestors and descendants.
     * It is significantly cleaner and slightly faster than healReleaseTree() which is why I think we should keep them separate. 
     *  
     * This method only receives reliable objects from mongo.
     * 
     * @param obj the RERUM object being released
     * @return Boolean sucess or some kind of Exception
     */
    private boolean establishReleasesTree (JSONObject obj) throws Exception{
        Boolean success = true;
        List<DBObject> ls_versions = getAllVersions(obj);
        JSONArray descendants = getAllDescendants(ls_versions, obj, new JSONArray());
        JSONArray anscestors = getAllAncestors(ls_versions, obj, new JSONArray());
        for(int d=0; d<descendants.size(); d++){
            JSONObject desc = descendants.getJSONObject(d);
            DBObject origDesc = (DBObject) JSON.parse(desc.toString());
            desc.getJSONObject("__rerum").getJSONObject("releases").element("previous", obj.getString("@id"));
            DBObject descToUpdate = (DBObject) JSON.parse(desc.toString());
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, origDesc, descToUpdate);
        }
        for(int a=0; a<anscestors.size(); a++){
            JSONObject ans = anscestors.getJSONObject(a);
            DBObject origAns = (DBObject) JSON.parse(ans.toString());
            ans.getJSONObject("__rerum").getJSONObject("releases").getJSONArray("next").add(obj.getString("@id"));
            DBObject ansToUpdate = (DBObject) JSON.parse(ans.toString());
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, origAns, ansToUpdate);
        }
        return success;
    }
    
    /**
     * A helper function that determines whether or not an object has been flagged as deleted.
     * This should only be fed reliable objects from mongo
     * @param obj
     * @return A boolean representing the truth.
     */
    private boolean checkIfDeleted(JSONObject obj){
        boolean deleted = obj.containsKey("__deleted");
        return deleted;
    }
    
    /**
     * A public facing servlet to determine if a given object ID is for a deleted object.
     * @FIXME make the parameter the http_request, get the @id, get from mongo and feed to private version.
     * @param obj_id
     * @return A boolean representing the truth.
     */
    public boolean checkIfDeleted(String obj_id){
        BasicDBObject query = new BasicDBObject();
        BasicDBObject dbObj;
        JSONObject checkThis = new JSONObject();
        query.append("@id", obj_id);
        dbObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); 
        if(null != dbObj){
            checkThis = JSONObject.fromObject(dbObj);
        }
        
        return checkIfDeleted(checkThis);

    }
    
    /**
    * check that the API keys match and that this application has permission to delete the object. These are internal and the objects passed in are
     * first taken from mongo, they are not the obj provided by the application.
    */
    private boolean checkApplicationPermission(JSONObject obj){
        boolean permission = true;
        //@cubap @theHabes TODO check that the API keys match and that this application has permission to delete the object
        return permission;
    }
    
    
    /**
     * A helper function that gathers an object by its id and determines whether or not it is flagged as released. These are internal and the objects passed in are
     * first taken from mongo, they are not the obj provided by the application.
     * @param obj
     * @return 
     */
    private boolean checkIfReleased(JSONObject obj){
        boolean released = false;
        //@cubap @theHabes #44.  What if obj does not have __rerum
        if(!obj.containsKey("__rerum") || !obj.getJSONObject("__rerum").containsKey("isReleased")){
            released = false;
        }
        else if(!obj.getJSONObject("__rerum").getString("isReleased").equals("")){
            released = true;
        }
        return released;
    }
    
    /**
     * Public facing servlet that checks whether the provided object id is of a released object.
     * @param obj_id
     * @return 
     */
    public boolean checkIfReleased(String obj_id){
        BasicDBObject query = new BasicDBObject();
        BasicDBObject dbObj;
        JSONObject checkThis = new JSONObject();
        query.append("@id", obj_id);
        dbObj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); 
        if(null != dbObj){
            checkThis = JSONObject.fromObject(dbObj);
        }
        return checkIfReleased(checkThis);
    }
       
    /**
     * Public facing servlet to delete a given annotation. 
     */
    public void deleteObject() throws IOException, ServletException, Exception{
        System.out.println("Delete object");
        if(null!=processRequestBody(request, true) && methodApproval(request, "delete")){ 
            BasicDBObject query = new BasicDBObject();
            BasicDBObject originalObject;
            //processRequestBody will always return a stringified JSON object here, even if the ID provided was a string in the body.
            JSONObject received = JSONObject.fromObject(content);
            JSONObject safe_received;
            JSONObject updatedWithFlag = new JSONObject();
            BasicDBObject updatedObjectWithDeletedFlag;
            if(received.containsKey("@id")){
                query.append("@id", received.getString("@id"));
                BasicDBObject mongo_obj = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query);
                safe_received = JSONObject.fromObject(mongo_obj); //We can trust this is the object as it exists in mongo
                boolean alreadyDeleted = checkIfDeleted(safe_received);
                boolean isReleased = false;
                boolean passedAllChecks = false;
                if(alreadyDeleted){
                    writeErrorResponse("Object for delete is already deleted.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
                else{
                    isReleased = checkIfReleased(safe_received);
                    if(isReleased){
                        writeErrorResponse("This object is in a released state and cannot be deleted.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);  
                    }
                    else{
                        String origObjGenerator = safe_received.getJSONObject("__rerum").getString("generatedBy");
                        boolean isGenerator = (origObjGenerator.equals(generatorID));
                        if(isGenerator){
                           passedAllChecks = true;
                        }
                        else{
                           writeErrorResponse("Only the agent that created this object can delete it.  Agent= "+generatorID, HttpServletResponse.SC_UNAUTHORIZED);   
                        }
                    }
                }
                if(passedAllChecks){ //If all checks have passed.  If not, we want to make sure their writeErrorReponse() don't stack.  
                    originalObject = (BasicDBObject) JSON.parse(safe_received.toString()); //The original object out of mongo for persistance
                    //Found the @id in the object, but does it exist in RERUM?
                    if(null != originalObject){
                        String preserveID = safe_received.getString("@id");
                        JSONObject deletedFlag = new JSONObject(); //The __deleted flag is a JSONObject
                        deletedFlag.element("object", originalObject);
                        deletedFlag.element("deletor", generatorID); 
                        deletedFlag.element("time", System.currentTimeMillis());
                        updatedWithFlag.element("@id", preserveID);
                        updatedWithFlag.element("__deleted", deletedFlag); //We want everything wrapped in deleted except the @id.
                        Object forMongo = JSON.parse(updatedWithFlag.toString()); //JSONObject cannot be converted to BasicDBObject
                        updatedObjectWithDeletedFlag = (BasicDBObject) forMongo;
                        boolean treeHealed = healHistoryTree(JSONObject.fromObject(originalObject));
                        if(treeHealed){
                            mongoDBService.update(Constant.COLLECTION_ANNOTATION, originalObject, updatedObjectWithDeletedFlag);
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            System.out.println("Object deleted:"+preserveID);
                        }
                        else{
                            //@cubap @theHabes FIXME By default, objects that don't have the history property will fail to this line.
                            writeErrorResponse("We could not update the history tree correctly.  The delete failed.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                    else{
                        writeErrorResponse("The '@id' string provided for DELETE could not be found in RERUM: "+safe_received.getString("@id")+". \n DELETE failed.", HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            }
            else{
                writeErrorResponse("Object for delete did not contain an '@id'.  Could not delete.", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    /**
     * An internal method to handle when an object is deleted and the history tree around it will need amending.  
     * This function should only be handed a reliable object from mongo.
     * 
     * @param obj A JSONObject of the object being deleted.
     * @return A boolean representing whether or not this function succeeded. 
     */
     private boolean healHistoryTree(JSONObject obj){
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
            //@cubap @theHabes #44.  What if obj does not have __rerum or __rerum.history            
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
                    System.out.println("object did not have previous and was not root.  Weird...");
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
                 System.out.println("could not find an object assosiated with id found in history tree");
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
                 System.out.println("We had a previous id in the object but could not find it in the store");
                 success = false;
             }
         }
         return success;
     }
     
     /**
     * An internal method to make all descendants of this JSONObject take on a new history.prime = this object's @id
     * This should only be fed a reliable object from mongo
     * @param obj A new prime object whose descendants must take on its id
     */
     private boolean newTreePrime(JSONObject obj){
         boolean success = true;
         String primeID = obj.getString("@id");
         JSONArray descendants = new JSONArray();
         for(int n=0; n< descendants.size(); n++){
             JSONObject descendantForUpdate = descendants.getJSONObject(n);
             JSONObject originalDescendant = descendants.getJSONObject(n);
             BasicDBObject objToUpdate = (BasicDBObject)JSON.parse(originalDescendant.toString());;
             descendantForUpdate.getJSONObject("__rerum").getJSONObject("history").element("prime", primeID);
             BasicDBObject objWithUpdate = (BasicDBObject)JSON.parse(descendantForUpdate.toString());
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
        String uid = Constant.RERUM_ID_PREFIX+newObjectID;
        dboWithObjectID.append("@id", uid);
        mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
        iiif_return = checkIIIFCompliance(uid, "2.1"); //If it is an object we are creating, this line means @context must point to Presentation API 2 or 2.1
        if(iiif_return.containsKey("okay")){
            if(iiif_return.getInt("okay") == 0){
                if(intendedIIIF){
                    //If it was intended to be a IIIF object, then remove this object from the store because it was not valid and return an error to the user

                }
                else{
                    //Otherwise say it is ok so the action looking do validate does not writeErrorResponse()
                    iiif_return.element("okay", 1);
                }
            }
            iiif_return.remove("received");
        }
        else{
            //Then the validator had a problem, perhaps it timed out and returned a blank object...
            iiif_return.element("okay", 0);
            iiif_return.element("error", "504: IIIF took too long to repond to RERUM.  The object was not submitted for validation.");
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
     * @throws java.net.MalformedURLException
     */
    public JSONObject checkIIIFCompliance(String objURL, String version) throws MalformedURLException, IOException{
        JSONObject iiif_return = new JSONObject();
        String iiif_validation_url = "https://iiif.io/api/presentation/validator/service/validate?format=json&version="+version+"&url="+objURL;
        System.out.println("IIIF validate URL wil be bypassed as it is timing out a lot.");
//        System.out.println(iiif_validation_url);
//        try{
//            URL validator = new URL(iiif_validation_url);
//            BufferedReader reader = null;
//            StringBuilder stringBuilder;
//            HttpURLConnection connection = (HttpURLConnection) validator.openConnection();
//            connection.setRequestMethod("GET"); 
//            connection.setConnectTimeout(5*1000); //This tends to choke sometimes...should i handle a timeout better?
//            System.out.println("Connect to iiif validator...");
//            connection.connect();
//            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            stringBuilder = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null)
//            {
//              stringBuilder.append(line);
//            }
//            connection.disconnect();
//        
//            if(stringBuilder.length() > 0){
//                iiif_return = JSONObject.fromObject(stringBuilder.toString());
//                iiif_return.remove("received");
//            }
//            else{
//                iiif_return = new JSONObject();
//            }
//        }
//        catch(java.net.SocketTimeoutException e){ //This specifically catches the timeout
//            System.out.println("The iiif endpoint is taking too long, so its going to be a blank object.");
//            iiif_return = new JSONObject(); //We were never going to get a response, so return an empty object.
//        }
        
        return iiif_return;
    }
    
     /**
     * Internal helper method to handle put_update.action on an external object.  The goal is to make a copy of object as denoted by the PUT request
     * as a RERUM object (creating a new object) then have that new root object reference the @id of the external object in its history.previous. 
     * 
     * @param externalObj the external object as it existed in the PUT request to be saved.
     */
    private void updateExternalObject(JSONObject externalObj){
        System.out.println("update on external object");
        //System.out.println(externalObj);
        externalObj.remove("_id"); //Make sure not to pass this along to any save/update scenario.  
        try {
            JSONObject jo = new JSONObject();
            JSONObject iiif_validation_response = checkIIIFCompliance(externalObj, true);
            JSONObject newObjState = configureRerumOptions(externalObj, true);
            DBObject dbo = (DBObject) JSON.parse(newObjState.toString());
            String exernalObjID = newObjState.getString("@id");
            String newRootID;
            String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
            //set @id from _id and update the annotation
            BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)dbo);
            newRootID = Constant.RERUM_ID_PREFIX+newObjectID;
            dboWithObjectID.append("@id", newRootID);
            newObjState.element("@id", newRootID);
            mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, dboWithObjectID);
            newObjState = configureRerumOptions(newObjState, false);
            newObjState = alterHistoryPrevious(newObjState, exernalObjID); //update history.previous of the new object to contain the external object's @id.
            expandPrivateRerumProperty(newObjState);
            newObjState.remove("_id");
            jo.element("code", HttpServletResponse.SC_CREATED);
            jo.element("original_object_id", exernalObjID);
            jo.element("new_obj_state", newObjState); 
            jo.element("iiif_validation", iiif_validation_response);
            addWebAnnotationHeaders(newRootID, isContainerType(newObjState), isLD(newObjState));
            addLocationHeader(newObjState);
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(HttpServletResponse.SC_CREATED);
            System.out.println("Object now internal to rerum: "+newRootID);
            out = response.getWriter();
            out.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(jo));
        }
        catch (IOException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }

     }
    
    private JSONObject getJWKS() throws MalformedURLException, ProtocolException, IOException{
        JSONObject jwksFile = new JSONObject();
        String jwksLocation = "https://cubap.auth0.com/.well-known/jwks.json";
        URL jwksURL = new URL(jwksLocation);
        BufferedReader reader = null;
        StringBuilder stringBuilder;
        HttpURLConnection connection = (HttpURLConnection) jwksURL.openConnection();
        connection.setRequestMethod("GET"); 
        connection.setReadTimeout(5*1000);
        connection.connect();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
        {
          stringBuilder.append(line);
        }
        connection.disconnect();
        reader.close();
        jwksFile = JSONObject.fromObject(stringBuilder.toString());
        return jwksFile;
    }
    
    public static String getAccessTokenWithAuth(String auth_code) throws UnsupportedEncodingException {
        System.out.println("Getting an access token");
        String token="";
        int rcode = 0;
        String tokenURL="https://cubap.auth0.com/oauth/token";
        JSONObject body = new JSONObject();
        //body.element("grant_type", "authorization_code");
        body.element("grant_type", "client_credentials");
        body.element("client_id", getRerumProperty("clientID"));
        body.element("client_secret", getRerumProperty("rerumSecret"));
        body.element("audience", "http://rerum.io/api");
        //body.element("code", auth_code);
        body.element("redirect_uri", Constant.RERUM_BASE);
        //System.out.println("I will be using this body: ");
        //System.out.println(body);
        try {           
            URL tURL = new URL(tokenURL);
            HttpURLConnection connection;
            connection = (HttpURLConnection) tURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();
            rcode = connection.getResponseCode();
            if(connection.getResponseCode() == 401 ){
                //One of the values used was wrong.  The secret may have expired, the clientID may be wrong, the auth_code may be wrong or the user may not be a part of the RERUM client.
                token = "Auth0 responded 401.  Contact RERUM.";
            }
            else if(connection.getResponseCode() >= 500){
                token = "Auth0 had an internal error.  Try again later. "+rcode;
            }
            else if(connection.getResponseCode() == 200){
                DataOutputStream jsonString = new DataOutputStream(connection.getOutputStream());
                jsonString.writeBytes(body.toString());
                jsonString.flush();
                jsonString.close();
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBlob = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null) {
                    responseBlob.append(line);
                }
                input.close();
                connection.disconnect();
                JSONObject responseObj = new JSONObject();
                if(responseBlob.length() > 0){
                    try{
                        responseObj = JSONObject.fromObject(responseBlob.toString());
                    }
                    catch(Exception e){
                        //response was not JSON, so let it an empty object.
                    }
                }
                if(responseObj.containsKey("access_token")){
                    token = responseObj.getString("access_token");
                    if("".equals(token)){
                        token = "Auth0 did not respond with a token.";
                    }
                }
                else{
                    token = "Auth0 did not respond with a token.  ";
                } 
            }
            else{
                token = "There was an issue contacting Auth0. "+rcode+".  Contact RERUM.";
            }

        } catch (ProtocolException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return token;
    }

    /*
        Verify the access code in the Bearer header of an action request.
    */
    private boolean verifyAccess(String access_token) throws IOException, ServletException, Exception{
        System.out.println("verify a JWT access token");
        System.out.println(access_token);
        boolean verified = false;
        JSONObject userInfo;
        try {
            DecodedJWT receivedToken = JWT.decode(access_token);
            System.out.println("initialize cache...");
            cache = JCS.getInstance("jwksCache");
            String KID = receivedToken.getKeyId();
            // Could cache this so we don't have to keep grabbing it since it has to happen on every call. 
            // http://commons.apache.org/proper/commons-jcs/getting_started/intro.html
            // https://commons.apache.org/proper/commons-jcs/
            Jwk jwk; 
            JwkProvider provider;
            System.out.println("check cache for key...");
            RSAPublicKey pubKey = cache.get("pubKey");
            //Cache the key so we don't have to keep requesting auth0 for it.  Expires in cache every 10 hours.  
            if(null==pubKey){
                System.out.println("key not in cache, ask auth0...");
                provider = new UrlJwkProvider("https://cubap.auth0.com/.well-known/jwks.json");
                jwk = provider.get(KID);
                pubKey = (RSAPublicKey) jwk.getPublicKey();     
                cache.put("pubKey", pubKey);
                System.out.println("key in cache...");
            }
            Algorithm algorithm = Algorithm.RSA256(pubKey, null);
            JWTVerifier verifier = JWT.require(algorithm).build(); //Reusable verifier instance
               //.withIssuer("auth0")
             System.out.println("receivedToken is : "+receivedToken); 
             String rerum_agent = Constant.RERUM_AGENT_CLAIM;
             System.out.println("rerum_agent :"+rerum_agent);
             System.out.println("receivedToken.getClaim(Constant.RERUM_AGENT_CLAIM) value is"+receivedToken.getClaim(Constant.RERUM_AGENT_CLAIM));
            generatorID = receivedToken.getClaim(Constant.RERUM_AGENT_CLAIM).asString();
            System.out.println("generatorID:"+generatorID);
            //System.out.println("Was I able to pull the agent claim from the token directly without userinfo without verifying?  Value below");
            //System.out.println("Value: "+generatorID);
            if(botCheck(generatorID)){
                System.out.println("It passed the bot check, no need to verify the access token.  I have the generator ID.  ");
                verified = true;
            }
            else{
                DecodedJWT d_jwt = verifier.verify(access_token);
                System.out.println("We were able to verify the access token. ");
                verified = true;
            }
        } 
        catch ( CacheException e )
        {
            System.out.println("Problem initializing cache: "+e.getMessage());
        }
        catch (JwkException | JWTVerificationException | IllegalArgumentException exception){
            //Invalid signature/claims/token.  Try to authenticate the old way
            System.out.println("Verification failed.  We were given a bad token.  IP fallback.  Exception below, but caught.");
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, exception);
            String requestIP = request.getRemoteAddr();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //check if the domain name and ip is in database
            BasicDBObject query = new BasicDBObject();
            query.append("ip", requestIP);
            DBObject result = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, query);
            if(null != result){
                System.out.println("[Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ App registered in legacy system");
                if(null!=result.get("agent") && !"".equals(result.get("agent"))){
                    //The user registered their IP with the new system
                    System.out.println("The registered server had an agent ID with it, so it must be from the new system.");
                    userInfo = JSONObject.fromObject(result);
                }
                else{
                    //This is a legacy user.
                    //Create agent and write back to server collection
                    //_id is messed up so adding the agent in doesn't always work.
                    System.out.println("The registered server did not have an agent ID.  It must be from the old system.");
                    System.out.println("We will generate a new agent to store with the registered server.");
                    userInfo = generateAgentForLegacyUser(JSONObject.fromObject(result));
                }
                //System.out.println("Grab agent id from");
                //System.out.println(userInfo);
                generatorID = userInfo.getString("agent");
                verified = true;
            }
            else{
                System.out.println("[Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ The app needs to register with RERUM");
                verified = false;
            }
        }
        System.out.println("I need a generator out of all this.  Did I get it: "+generatorID);
        return verified;
    }
    
    /**
     * Look at agent in token to accept even expired tokens from our known bot
     * at Auth0 who initializes agents for newly registered applications.
     * @param agent URI intended for generator
     * @return true if matched
     */
    private boolean botCheck(String agent) {
        System.out.println("agent:"+agent);
        System.out.println("getRerumProperty(\"bot_agent\"):"+getRerumProperty("bot_agent"));
        return agent.equals(getRerumProperty("bot_agent"));
    }
       
    private JSONObject generateAgentForLegacyUser(JSONObject legacyUserObj){
        System.out.println("Detected a legacy registration.  Creating an agent and storing it with this legacy object.");
        //System.out.println(legacyUserObj);
        JSONObject newAgent = new JSONObject();
        DBObject originalToUpdate = (DBObject)JSON.parse(legacyUserObj.toString());
        JSONObject orig = JSONObject.fromObject(originalToUpdate);
        String mbox = "Not Provided";
        String label = "Not Provided";
        String homepage = "Not Provided";
        if(legacyUserObj.containsKey("name") && !"".equals(legacyUserObj.getString("name"))){
            label = legacyUserObj.getString("name");
        }
        if(legacyUserObj.containsKey("contact") && !"".equals(legacyUserObj.getString("contact"))){
            mbox = legacyUserObj.getString("contact");
        }
        if(legacyUserObj.containsKey("website") && !"".equals(legacyUserObj.getString("website"))){
            homepage = legacyUserObj.getString("website");
        }
        newAgent.element("@type", "foaf:Agent");
        newAgent.element("@context", Constant.RERUM_PREFIX+"context.json");
        newAgent.element("mbox", mbox); 
        newAgent.element("label", label); 
        newAgent.element("homepage", homepage); 
        DBObject dbo = (DBObject) JSON.parse(newAgent.toString());
        String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, dbo);
        orig.element("agent", Constant.RERUM_ID_PREFIX+newObjectID);
        newAgent.element("@id", Constant.RERUM_ID_PREFIX+newObjectID);
        newAgent.element("agent", Constant.RERUM_ID_PREFIX+newObjectID);
        generatorID = Constant.RERUM_ID_PREFIX+newObjectID;
        DBObject updatedOrig = (DBObject) JSON.parse(orig.toString());
        DBObject idOnAgent = (DBObject) JSON.parse(newAgent.toString());
        mongoDBService.update(Constant.COLLECTION_ACCEPTEDSERVER, originalToUpdate, updatedOrig); //This update does not appear to work every time...
        mongoDBService.update(Constant.COLLECTION_ANNOTATION, dbo, idOnAgent);
        System.out.println("Agent created and stored with accepted server...");
        System.out.println(updatedOrig);
        return newAgent;
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
    
    /**
     * Expand the __rerum property for JSON-LD context purposes.
     * @param thisObject A JSONObject that needs direct manipulation.
     * @return The manipulated JSONObject
     */
    private JSONObject expandPrivateRerumProperty(JSONObject thisObject) {
        //This is a breaking change for applications relying on the __rerum field (TPEN_NL).  Need to hold off on this for now.
//        if(thisObject.has("__rerum")){
//            JSONObject rerumProperty = thisObject.getJSONObject("__rerum");
//            thisObject.element(Constant.RERUM_API_DOC, rerumProperty);
//            thisObject.remove("__rerum");
//        }
        return thisObject;
    }
    
    /**
     * Expand the __rerum property for JSON-LD context purposes.
     * @param thisObject A BasicDBObject that needs direct manipulation.
     * @return The manipulated BasicDBObject
     */
    private BasicDBObject expandPrivateRerumProperty(BasicDBObject thisObject) {
//        if(thisObject.containsField("__rerum")){
//            Object rerumProps = thisObject.get("__rerum");
//            thisObject.put(Constant.RERUM_API_DOC, rerumProps);
//            thisObject.remove("__rerum");
//        }
        return thisObject;
    }

}

