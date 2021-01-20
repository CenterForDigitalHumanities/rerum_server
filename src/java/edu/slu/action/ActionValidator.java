/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.action;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.slu.common.Constant;
import edu.slu.action.ObjectAction;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author bhaberbe
 */
public class ActionValidator extends HttpServlet {

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
            BufferedReader bodyReader = request.getReader();
            StringBuilder bodyString = new StringBuilder();
            String line;
            String requestString;
            StringBuilder sb = new StringBuilder();
            int codeOverwrite = 500;
            LocalDateTime dt = LocalDateTime.now();
            DateTimeFormatter dtFormat = DateTimeFormatter.ISO_DATE_TIME;
            String formattedOverwrittenDateTime = dt.format(dtFormat);
            
            String generatedHTML = "<!DOCTYPE html>";
            generatedHTML += "<html>";
            generatedHTML += "<head>";        
            generatedHTML += "<title> RERUM Endpoint Validator </title>";
            generatedHTML += "</head>";
            generatedHTML += "<body>";
            generatedHTML += "<html>";
            generatedHTML += "<html>";
            generatedHTML += "<html>";
            generatedHTML += "<html>";
            generatedHTML += "</body>";
            generatedHTML += "</html>";
            
          
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(codeOverwrite);
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
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
