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
 * @author hanyan && bhaberbe
 * Check if there is a valid session.  Return 403 so struts knows to go to 403.jsp.  Take over the response so you can return a 403 status.
 * 
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
