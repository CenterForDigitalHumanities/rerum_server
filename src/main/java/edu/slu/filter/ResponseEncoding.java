/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.slu.filter;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author hanyan && bhaberbe
 * @Deprecated there is no need for this anymore, response manipulation is handled in ObjectAction.java
 * @see ObjectAction.java, struts.xml
 */
public class ResponseEncoding extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
//       HttpServletResponse response = ServletActionContext.getResponse();
//        response.setCharacterEncoding("UTF-8");
//       // response.addHeader("Access-Control-Allow-Origin", "*");
//        //response.addHeader("Access-Control-Allow-Credentials", "true");
//        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
//        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT");
        //response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
        return ai.invoke(); 
    }
    
}
