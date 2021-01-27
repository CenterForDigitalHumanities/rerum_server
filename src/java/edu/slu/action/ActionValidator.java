/*
 * Test MongoDBAbstractDAO.java unit functions that are required to work for the interactions with mongo.  You can find what actions are required by viewing
 * the various actions and private helpers in ObjectAction.java.  
 */
package edu.slu.action;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import edu.slu.common.Constant;
import edu.slu.service.MongoDBService;
import net.sf.json.JSONObject;

/**
 *
 * @author bhaberbe
 */
public class ActionValidator extends HttpServlet {
    private MongoDBService mongoDBService;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
            request.setCharacterEncoding("UTF-8");
            int codeOverwrite = 500;
            LocalDateTime dt = LocalDateTime.now();
            DateTimeFormatter dtFormat = DateTimeFormatter.ISO_DATE_TIME;
            String formattedOverwrittenDateTime = dt.format(dtFormat);
            String createResult, putUpdateResult, patchUpdateResult, patchSetResult, patchUnsetResult, overwriteResult, deleteResult, queryResult;
            createResult=putUpdateResult=patchUpdateResult=patchSetResult=patchUnsetResult=overwriteResult=deleteResult=queryResult="<font color='red'>ERROR</font>";
            JSONObject jo = new JSONObject();
            jo.element("validate_create", formattedOverwrittenDateTime);
            DBObject test = (DBObject) JSON.parse(jo.toString());
            
            //For CREATE, which is a save and update in mongo unit actions (make it exist for the _id, update it with an @id)
            try{
                String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, test); //put in DB
                //set @id from _id and update the annotation
                BasicDBObject dboWithObjectID = new BasicDBObject((BasicDBObject)test);
                String newid = Constant.RERUM_ID_PREFIX+newObjectID;
                dboWithObjectID.put("@id", newid); //give @id
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, test, dboWithObjectID);
                jo = JSONObject.fromObject(dboWithObjectID);
                createResult = "<font color='green'>available</font>";
            }
            catch(Exception e){
                createResult = "<font color='red'>unavailable</font>";
            }
            
            //For PUT/PATCH/SET/UNSET/DELETE all use the same mongodbservice unit functionality.  findOneByExample - save - give @id - update - manage versioning (more mongo unit updates).  
            //OVERWRITE uses findOnByExample and update and so if covered by this test.
            try{
                String newObjectID = mongoDBService.save(Constant.COLLECTION_ANNOTATION, test); //put object to update to in db
                String origID = Constant.RERUM_ID_PREFIX + newObjectID;
                BasicDBObject testWithObjectID = new BasicDBObject((BasicDBObject)test);
                testWithObjectID.append("@id", newObjectID);
                mongoDBService.update(Constant.COLLECTION_ANNOTATION, test, testWithObjectID); //give it an @id (need for versioning)
                BasicDBObject query = new BasicDBObject();
                query.append("@id", origID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findOneByExample(Constant.COLLECTION_ANNOTATION, query); //Find obj that needs to be updated
                if(null != originalObject){
                    jo.element("validate_services", formattedOverwrittenDateTime);
                    test = (DBObject) JSON.parse(jo.toString());
                    mongoDBService.update(Constant.COLLECTION_ANNOTATION, originalObject, test); //update it & get versioning right
                    createResult=putUpdateResult=patchUpdateResult=patchSetResult=patchUnsetResult=deleteResult=overwriteResult="<font color='green'>available</font>";
                }
                else{
                    putUpdateResult=patchUpdateResult=patchSetResult=patchUnsetResult=deleteResult=overwriteResult="<font color='red'>Error finding 11111</font>";
                }
            }
            catch(Exception e){
                putUpdateResult=patchUpdateResult=patchSetResult=patchUnsetResult=deleteResult=overwriteResult="<font color='red'>unavailable</font>";
            }
            
            //For query, it uses findByExample
            try{
                String origID = Constant.RERUM_ID_PREFIX + "11111";
                BasicDBObject query = new BasicDBObject();
                query.append("@id", origID);
                BasicDBObject originalObject = (BasicDBObject) mongoDBService.findByExample(Constant.COLLECTION_ANNOTATION, query);
                if(null != originalObject){
                    queryResult = "<font color='green'>available</font>";
                }
                else{
                    queryResult = "<font color='red'>unavailable</font>";
                }
            }
            catch(Exception e){
                queryResult = "<font color='red'>unavailable</font>";
            }
            
            /**
             * Note if there are other MongoDBAbstractDAO.java unit functions that MUST be working, it would be wise to test them here. 
             */
            
            String generatedHTML = "<!DOCTYPE html>";
            generatedHTML += "<html>";
            generatedHTML += "<head>";        
            generatedHTML += "<title> RERUM Endpoint Validator </title>";
            generatedHTML += "</head>";
            generatedHTML += "<body>";
            generatedHTML += "<p>Below is a report on the MongoDB connections for RERUM as of "+formattedOverwrittenDateTime+"</p>";
            generatedHTML += "<table><tr>";
            generatedHTML += "<th>Action</th><th>Availability</th></tr>";
            
            generatedHTML += "<tr><td>Create</td><td>"+createResult+"</td></tr>"; //Create
            generatedHTML += "<tr><td>Update (PUT)</td><td>"+putUpdateResult+"</td></tr>"; //Put Update
            generatedHTML += "<tr><td>Update, Set, Unset (PATCH)</td><td>"+patchUpdateResult+"</td></tr>"; //Patch Update
            generatedHTML += "<tr><td>Overwrite</td><td>"+overwriteResult+"</td></tr>"; //Overwrite
            generatedHTML += "<tr><td>Delete</td><td>"+putUpdateResult+"</td></tr>"; //Delete
            generatedHTML += "<tr><td>Query</td><td>"+queryResult+"</td></tr>"; //Query
            generatedHTML += "</table>";
             
            generatedHTML += "</body>";
            generatedHTML += "</html>";
            
          
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(codeOverwrite);
            response.setHeader("Content-Type", "text/html; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(generatedHTML);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Check available endpoints of RERUM.  This will tell you where we are having issues right now.";
    }// </editor-fold>

}
