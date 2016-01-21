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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(AnnotationAction.class.getName()).log(Level.SEVERE, null, ex);
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
