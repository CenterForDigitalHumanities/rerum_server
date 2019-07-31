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
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author hanyan
 */
public class DownTimeOverride extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        PrintWriter out = response.getWriter();
        String body = "We are performing maintainance right now.  Please try again later.  Sorry for the inconvenience.";
        response.setStatus(500);
        throw new Exception(body);
    }
    
}
