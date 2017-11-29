/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.filter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import edu.slu.common.Constant;
import edu.slu.util.MongoDBUtil;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author hanyan && bhaberbe
 * Check if the IP of the request is registered with RERUM.  
 * Return 403 so struts knows to go to 403.jsp.  Take over the response so you can return a 403 status.
 * 
 */
public class RequestServerAuthenticationFilter extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
        //get remote server host ip
        HttpServletRequest request = ServletActionContext.getRequest();
        String requestIP = request.getRemoteAddr();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //check if the domain name and ip is in database
        BasicDBObject query = new BasicDBObject();
        query.append("ip", requestIP);
        DB db = MongoDBUtil.getDb();
        DBCollection coll = db.getCollection(Constant.COLLECTION_ACCEPTEDSERVER);
        DBCursor cursor = coll.find(query);
        List<DBObject> ls_results = cursor.toArray();
        if(ls_results.size() > 0){
            System.out.println("[Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ From Registered Server");
        }else{
            System.out.println("[Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ Not From Registered Server");
        }
        if(ls_results.size() > 0){
            return ai.invoke();
        }else{
            System.out.println("403 because session ip not registered");
            HttpServletResponse respond_403 = ServletActionContext.getResponse();
            respond_403.setStatus(403);
            respond_403.addHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = respond_403.getWriter();
            out.write("You must register your IP with this service.  Visit <a>http://store.rerm.io/rerumserver/</a>");
            //return ai.invoke();
            return "403";
        }
    }

}
