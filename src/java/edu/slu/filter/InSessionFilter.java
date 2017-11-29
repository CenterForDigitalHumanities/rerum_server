/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.filter;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author hanyan
 * We would like this to return status code 403, but it returns 200 with 403 in the body.  
 * We added a patch for this in @see AnnotationAction.java through 
 * @Deprecated I cannot find a way to make this return the response with a 403 error code.  Moved session handling into @see AnnotationAction.java.
 */
public class InSessionFilter extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession();
        if(null != session){
            return ai.invoke();
        }
        else{
            System.out.println("403 because session was null");
            HttpServletResponse respond_403 = ServletActionContext.getResponse();
            respond_403.setStatus(403);
            respond_403.addHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = respond_403.getWriter();
            out.write("You did not have a valid session.");
            return HttpServletResponse.SC_UNAUTHORIZED + "";
        }
    }
    
}
