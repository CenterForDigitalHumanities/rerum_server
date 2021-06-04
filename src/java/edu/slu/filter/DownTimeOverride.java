/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.filter;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author bhaberbe
 */
public class DownTimeOverride extends MethodFilterInterceptor {

    @Override
    protected String doIntercept(ActionInvocation ai) throws Exception {
        String offSwitch = getRerumProperty("down"); //Get this switch from the properties file.
        if(offSwitch.equals("true")){
            HttpServletResponse response = ServletActionContext.getResponse();
            String body = "The RERUM API is down for maintenance.  Please try again later.  Sorry for the inconvenience.";
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE");
            response.setStatus(503);
            response.getWriter().write(body);
            System.out.println("DOWN TIME 503");
            //Make sure to return here, this will stop the API from moving into the next intercept
            return "503";
        }
        else{
            //.invoke() will continue to the next intercept
            return ai.invoke();
        }
    }
    
    /**
    * Private function to get information from the rerum properties file
    
    * @param prop the name of the property to retrieve from the file.
    * @return the value for the provided property
    */    
   private static String getRerumProperty(String prop) {
      ResourceBundle rb = ResourceBundle.getBundle("rerum");
      String propVal = "";
      try {
         propVal = rb.getString(prop);
      } catch (MissingResourceException e) {                     
         System.err.println("Token ".concat(prop).concat(" not in Propertyfile!"));
      }
      return propVal;
   }
    
}
