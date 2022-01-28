/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.mongoEntity;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

/**
 *
 * @author hanyan
 */
public class Annotation implements ServletRequestAware, ServletResponseAware{
    private String content;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder bodyString;
    private ServletInputStream bodyStream;
    private BufferedReader bodyReader;
    
    /*
    * Support when object is not {content:{key:val}} but rather {key:val}
    */
    public Annotation() throws IOException {
        System.out.println("In annotation .java no content situation");
        String requestBody;
        bodyString = new StringBuilder();
        String line="";
        System.out.println("See lines from the reader 1...");
        while ((line = bodyReader.readLine()) != null)
        {
          System.out.println("line _ is "+line);
          bodyString.append(line + "\n");
        }

        requestBody = bodyString.toString();
        System.out.println("See string built 1...");
        System.out.println(requestBody);
        this.content = requestBody;
    }

    public Annotation(String content) {
        System.out.println("In annotation .java content situation");
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        System.out.println("In annotation .java setting the content");
        this.content = content;
    }

    @Override
    public void setServletRequest(HttpServletRequest hsr) {
        System.out.println("In annotation .java servlet request spot.");
        this.request = hsr;
    }

    @Override
    public void setServletResponse(HttpServletResponse hsr) {
        this.response = hsr;
    }
}
