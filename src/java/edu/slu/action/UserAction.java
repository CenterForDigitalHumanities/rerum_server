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
import edu.slu.mongoEntity.Agent;
import edu.slu.mongoEntity.Person;
import edu.slu.mongoEntity.ProjectUserProfile;
import edu.slu.service.MongoDBService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.bson.types.ObjectId;

/**
 *
 * @author hanyan
 */
public class UserAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {
    //entity
    private Person person;
    private ProjectUserProfile projectUserProfile;
    //service
    private MongoDBService mongoDBService;
    //others
    private String content;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter out;
    private StringBuilder bodyString;
    private BufferedReader bodyReader;
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
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
     * Check duplicated person email.
     * @param person.email
     */
    public void checkDuEmail() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("email")){
            BasicDBObject dbo = new BasicDBObject();
            dbo.append("email", received.getString("email"));
            List<DBObject> ls_results = mongoDBService.findByExample(Constant.COLLECTION_USER, dbo);
            if(null != ls_results && ls_results.size() > 0){
                try {
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_CONFLICT);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                try {
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Save a agent which type is person. 
     * @param All agent properties and person properties
     * @return 200: save successful, 400: no correct properties or no sufficient properties. 
     */
    public void saveNewAgentPerson() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("person")){
            //save new person
            JSONObject jsonUser = received.getJSONObject("person");
            person = new Person();
            person.setEmail(jsonUser.getString("email"));
            person.setPwd(jsonUser.getString("pwd"));
            person.setDateCreated(new Date().getTime());
            BasicDBObject dbo = new BasicDBObject(person.toMap());
            String personObjectID = mongoDBService.save(Constant.COLLECTION_USER, dbo);
            BasicDBObject personQuery = new BasicDBObject();
            personQuery.append("_id", new ObjectId(personObjectID));
            person.setaID("rerum.io/person/"+personObjectID);
            BasicDBObject updateDbo = new BasicDBObject(person.toMap());
            mongoDBService.update(Constant.COLLECTION_USER, personQuery, updateDbo);
            if(received.containsKey("agent")){
                //save person related agent
                JSONObject jo_agent = received.getJSONObject("agent");
                Agent ag = new Agent();
                ag.setCreated(new Date().getTime());
                ag.setMbox(jo_agent.getString("mbox"));
                ag.setMbox_sha1sum(jo_agent.getString("mbox"));
                if(jo_agent.has("group")){
                    ag.setGroup(jo_agent.getJSONArray("group"));
                }else{
                    ag.setGroup(new JSONArray());
                }

                BasicDBObject agdbo = new BasicDBObject(ag.toMap());
                String objectID = mongoDBService.save(Constant.COLLECTION_AGENT, agdbo);
                ag.setaID("rerum.io/agent/"+objectID);
                BasicDBObject agQuery = new BasicDBObject();
                agQuery.append("_id", new ObjectId(ag.getObjectID()));
                BasicDBObject agupdatedbo = new BasicDBObject(ag.toMap());
                mongoDBService.update(Constant.COLLECTION_AGENT, agQuery, agupdatedbo);
                try {
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_OK);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                try {
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                    out = response.getWriter();
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            try {
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                out = response.getWriter();
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Update person info. 
     * @param person.objectID, person.email, person.pwd, person properties
     */
    public void updateAgentInfo() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        HttpSession session = request.getSession();
        BasicDBObject personQuery = new BasicDBObject();
        personQuery.append("email", session.getAttribute("personEmail"));
        DBObject dbUser = mongoDBService.findOneByExample(Constant.COLLECTION_USER, personQuery);
        if(null != dbUser){
            Set<String> keys = received.keySet();
            for(String s : keys){
                if(dbUser.containsField(s)){
                    dbUser.removeField(s);
                    dbUser.put(s, received.get(s));
                }
            }
            mongoDBService.update(Constant.COLLECTION_USER, personQuery, dbUser);
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_OK);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Check if an agent exists. 
     * @param person.aID
     */
    public void checkExistAgent() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("agent")){
            JSONObject agent = received.getJSONObject("agent");
            BasicDBObject bdbo = new BasicDBObject();
            bdbo.append("@id", agent.getString("@id"));
            Long count = mongoDBService.count(Constant.COLLECTION_AGENT, bdbo);
            if(count > 0){
                try {
                    out = response.getWriter();
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_FOUND);
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                try {
                    out = response.getWriter();
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Add project to person's profile. The project server must be saved in trusted server table before this action. 
     * @param projectUserProfile.alias, projectUserProfile.serverIP, projectUserProfile.config
     */
    public void addProjectToUser() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        HttpSession session = request.getSession();
        if(received.containsKey("projectUserProfile")){
            BasicDBObject personQuery = new BasicDBObject();
            personQuery.append("email", session.getAttribute("personEmail"));
            DBObject dbUser = mongoDBService.findOneByExample(Constant.COLLECTION_USER, personQuery);
            if(null != dbUser){
                BasicDBObject dboUser = (BasicDBObject) dbUser;
                String personObjectID = dboUser.getObjectId("_id").toString();
                //get remote server host ip
                HttpServletRequest request = ServletActionContext.getRequest();
                String requestIP = request.getRemoteAddr();
                BasicDBObject dbo = new BasicDBObject();
                dbo.append("ip", requestIP);
                Long numOfServer = mongoDBService.count(Constant.COLLECTION_ACCEPTEDSERVER, dbo);
                if(numOfServer > 0){
                    //add server to project person profile list
                    JSONObject jsonProjectUser = received.getJSONObject("projectUserProfile");
                    ProjectUserProfile pup = new ProjectUserProfile();
                    pup.setUserObjectID(personObjectID);
                    pup.setAlias(jsonProjectUser.getString("alias"));
                    List<String> ls_serverIP = new ArrayList();
                    ls_serverIP.add(requestIP);
                    pup.setLs_serverIP(ls_serverIP);
                    pup.setDateCreated(new Date().getTime());
                    pup.setDateUpdated(new Date().getTime());
                    pup.setConfig(jsonProjectUser.getString("config"));
                    BasicDBObject toSave = new BasicDBObject(pup.toMap());
                    //save person
                    mongoDBService.save(Constant.COLLECTION_USER, toSave);
                }else{
                    try {
                        out = response.getWriter();
                        JSONObject jo = new JSONObject();
                        jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                        out.print(jo);
                    } catch (IOException ex) {
                        Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else{
                try {
                    out = response.getWriter();
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Update person info in a given project. 
     * @param projectUserProfile(any thing to update)
     */
    public void updateProjectUserProfile() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        HttpSession session = request.getSession();
        if(received.containsKey("projectUserProfile") && received.containsKey("email")){
            BasicDBObject personQuery = new BasicDBObject();
            personQuery.append("email", session.getAttribute("personEmail"));
            DBObject dbUser = mongoDBService.findOneByExample(Constant.COLLECTION_USER, personQuery);
            if(null != dbUser){
                BasicDBObject dboUser = (BasicDBObject) dbUser;
                String personObjectID = dboUser.getObjectId("_id").toString();
                BasicDBObject profileQuery = new BasicDBObject();
                profileQuery.append("personObjectID", personObjectID);
                List<DBObject> ls_personProfiles = mongoDBService.findByExample(Constant.COLLECTION_USER, profileQuery);
                //get remote server host ip
                String requestIP = request.getRemoteAddr();
                for(DBObject dbo : ls_personProfiles){
                    BasicDBObject bdbo = (BasicDBObject) dbo;
                    int containServer = 0;
                    if(null != bdbo.get("ls_server_ip")){
                        JSONArray ja_serverIP = (JSONArray) bdbo.get("ls_server_ip");
                        for(int i=0;i<ja_serverIP.size();i++){
                            //chech if the ip is in the ip list of project person profile
                            if(requestIP.equals(ja_serverIP.get(i))){
                                //get new project person profile data
                                JSONObject newProjectUserProfile = received.getJSONObject("projectUserProfile");
                                //get keys from new project person profile
                                Set<String> keys = newProjectUserProfile.keySet();
                                //replace old value with new value
                                for(String k : keys){
                                    bdbo.remove(k);
                                    bdbo.append(k, newProjectUserProfile.get(k));
                                }
                                BasicDBObject updateQury = new BasicDBObject();
                                updateQury.append("_id", bdbo.getObjectId("_id").toString());
                                mongoDBService.update(Constant.COLLECTION_PROJECT_USER_PROFILE, updateQury, bdbo);
                                containServer = 1;
                                break;
                            }
                        }
                        //if there is no server ip match, return error msg
                        if(containServer == 0){
                            try {
                                out = response.getWriter();
                                JSONObject jo = new JSONObject();
                                System.out.println("403 because of server being unregistered.");
                                jo.element("code", HttpServletResponse.SC_UNAUTHORIZED);
                                out.print(jo);
                            } catch (IOException ex) {
                                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }else{
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * To check if the person is registered at IIIF store. 
     * @param person.email and password
     */
    public void personLogin() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("person")){
            JSONObject jsonUser = received.getJSONObject("person");
            person = new Person();
            person.setEmail(jsonUser.getString("email"));
            person.setPwd(jsonUser.getString("pwd"));
            BasicDBObject dbo = new BasicDBObject();
            dbo.append("email", person.getEmail());
            dbo.append("pwd", person.getPwd());
            Long count = mongoDBService.count(Constant.COLLECTION_USER, dbo);
            try {
                out = response.getWriter();
                if(count > 0){
                    HttpSession session = request.getSession();
                    session.setAttribute("personEmail", person.getEmail());
                    JSONObject jo = new JSONObject();
                    //return status code and session id
                    jo.element("code", HttpServletResponse.SC_OK);
                    jo.element("session", session.getId());
                    out.print(jo);
                }else{
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_NOT_FOUND);
                    out.print(jo);
                }
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Remove agent by objectID
     */
    public void delAgentByID() throws IOException, Exception{
        content = processRequestBody(request);
        JSONObject received = JSONObject.fromObject(content);
        if(received.containsKey("agent")){
            if(received.containsKey("id")){
                String objectID = received.getString("id");
                BasicDBObject query = new BasicDBObject();
                query.append("_id", new ObjectId(objectID));
                mongoDBService.delete(Constant.COLLECTION_USER, query);
                try {
                    out = response.getWriter();
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_ACCEPTED);
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                try {
                    out = response.getWriter();
                    JSONObject jo = new JSONObject();
                    jo.element("code", HttpServletResponse.SC_PARTIAL_CONTENT);
                    jo.element("msg", "Require id as parameter");
                    out.print(jo);
                } catch (IOException ex) {
                    Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            try {
                out = response.getWriter();
                JSONObject jo = new JSONObject();
                jo.element("code", HttpServletResponse.SC_BAD_REQUEST);
                jo.element("msg", "Require object 'agent'");
                out.print(jo);
            } catch (IOException ex) {
                Logger.getLogger(UserAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
     * @return the projectUserProfile
     */
    public ProjectUserProfile getProjectUserProfile() {
        return projectUserProfile;
    }

    /**
     * @param projectUserProfile the projectUserProfile to set
     */
    public void setProjectUserProfile(ProjectUserProfile projectUserProfile) {
        this.projectUserProfile = projectUserProfile;
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
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

}
