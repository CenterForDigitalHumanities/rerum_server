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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author hanyan
 */
public class ClientInfoRecorder extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
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
            System.out.println("[Not Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ From Registered Server");
        }else{
            System.out.println("[Not Modifying Data Request]: ip ========== " + requestIP + "@" + sdf.format(new Date()) + " +++++ Not From Registered Server");
        }
        return ai.invoke();
    }
    
}
