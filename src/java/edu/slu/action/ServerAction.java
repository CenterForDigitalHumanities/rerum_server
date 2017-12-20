/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.action;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.opensymphony.xwork2.ActionSupport;
import edu.slu.common.Constant;
import edu.slu.mongoEntity.AcceptedServer;
import edu.slu.service.MongoDBService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
public class ServerAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {
    private AcceptedServer acceptedServer;
    
    private MongoDBService mongoDBService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private PrintWriter out;
    
    private StringBuilder bodyString;
    private BufferedReader bodyReader;
    
    /*
    * Request processor so that data doesn't have to be passed through the API like {contet:{}}
    * It is backwards compatible to support this behavior. 
    *
    * Not 100% sure we need it for these actions, need to test around.  If it turns out we do, then add it to the individual methods below. 
    */
    public String processRequestBody(HttpServletRequest http_request) throws IOException, ServletException, Exception{
        String cType = http_request.getContentType();
        String requestBody;
        JSONObject test;
        JSONArray test2;
        
        System.out.println(System.getProperty("line.separator"));
        System.out.println(System.getProperty("line.separator"));
        System.out.println("Processing request...");
        System.out.println("Server info at the top of processing is "+acceptedServer);

        /* This means the type was application/x-www-form-urlencoded and they passed it like {content:{data}} so I already have content, just go forward using that.  This is backwards compatability */
        if(null != acceptedServer && !acceptedServer.equals("")){ 
            System.out.println("Content is already set, so return it");           
//            System.out.println(System.getProperty("line.separator"));
//            System.out.println(System.getProperty("line.separator"));          
            try{ //Try to parse as a JSONObject
                test = JSONObject.fromObject(acceptedServer);
            }
            catch(Exception ex){ //was not a JSONObject but might be a JSONArray
                //System.out.println("Was not an object...");
                try{ //Try to parse as a JSONArray
                    test2 = JSONArray.fromObject(acceptedServer);
                }
                catch(Exception ex2){ //Was not a JSONObject or a JSONArray.  Not valid JSON.  Throw error. 
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    throw new Exception("{error: 'The data passed was not valid JSON.'}");
                }
            }
            //System.out.println(content);
            return acceptedServer.toString();
        }
        if(cType.contains("application/x-www-form-urlencoded")){ //They passed this content type but did not follow the {content:{data}} format.
            //TODO: Throw improper request body error!!@@
            //System.out.println("application/x-www-form-urlencoded type not in proper {content:{data}} format ");
            requestBody = "{error: 'Improper request body.  Must use {content:{data}} format for content type application/x-www-form-urlencoded or instead use application/json or application/ld+json Content Type with valid JSON.'}";
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
                      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new Exception(requestBody);
        }

    }
    
    /**
     * Save a new server's domain and IP
     * @param acceptedServer
     */
    public void saveNewServer(){
        //check if the IP is duplicated
        BasicDBObject query = new BasicDBObject();
        query.append("ip", acceptedServer.getIp());
        List<DBObject> ls_results = mongoDBService.findByExample(Constant.COLLECTION_ACCEPTEDSERVER, query);
        if(null != ls_results && ls_results.size() > 0){
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_NOT_ACCEPTABLE);
            jo.element("info", "duplicated IP");
            try {
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            //if the IP is not duplicated, save the info. 
            BasicDBObject dbo = new BasicDBObject(acceptedServer.toMap());
            String newObjectID = mongoDBService.save(Constant.COLLECTION_ACCEPTEDSERVER, dbo);
            JSONObject jo = new JSONObject();
            jo.element("code", HttpServletResponse.SC_OK);
            jo.element("info", "Server saved!");
            jo.element("newObjectID", newObjectID);
            try {
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Update current server info by objectID.
     * @param acceptedServer
     */
    public void updateServerByObjectID(){
        BasicDBObject query = new BasicDBObject();
        query.append("_id", new ObjectId(acceptedServer.getObjectID()));
        mongoDBService.update(Constant.COLLECTION_ACCEPTEDSERVER, query, new BasicDBObject(acceptedServer.toMap()));
        JSONObject jo = new JSONObject();
        jo.element("info", HttpServletResponse.SC_OK);
        try {
            out = response.getWriter();
            out.print(jo);
        } catch (IOException ex) {
            Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get server info by Ip. 
     * @param acceptedServer.ip
     * @return acceptedServer
     */
    public void getServerByIp(){
        BasicDBObject query = new BasicDBObject();
        query.append("ip", acceptedServer.getIp());
        DBObject result = mongoDBService.findOneByExample(Constant.COLLECTION_ACCEPTEDSERVER, query);
        if(null != result){
            BasicDBObject dbo = (BasicDBObject) result;
            AcceptedServer as = new AcceptedServer(dbo);
            JSONObject jo = new JSONObject();
            jo.element("server", as.toMap());
            try {
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(ObjectAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

    @Override
    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    @Override
    public void setServletResponse(HttpServletResponse hsr) {
        this.response = hsr;
    }

    
}
