/*
 * (C) 2015 MSRoth - msroth.wordpress.com
 * D2 DQL Editor
 */

package com.dm_misc.D2;

import com.dm_misc.collections.dmRecordSet;
import com.dm_misc.dctm.DCTMBasics;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.IDfAttr;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/DQLQueryServlet")
public class DQLQueryServlet extends HttpServlet {
    final int maxRows = 1000;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter("user");
        String ticket = request.getParameter("ticket");
        String docbase = request.getParameter("docbase");
        String dql = request.getParameter("dql"); 

        IDfSession DCTMsession = null;
        StringBuilder output = new StringBuilder();

        System.out.println(this.getServletName() + " -- DEBUG -- user=" + user);
        System.out.println(this.getServletName() + " -- DEBUG -- docbase=" + docbase);
        System.out.println(this.getServletName() + " -- DEBUG -- dql=" + dql);
        System.out.println(this.getServletName() + " -- DEBUG -- ticket=" + ticket);

        try {
            //Login to DCTM
            DCTMsession = DCTMBasics.logon(docbase, user, ticket);
            System.out.println(this.getServletName() + " -- DEBUG -- logged in");
        } catch (Exception e) {
            System.out.println(this.getServletName() + " -- ERROR -- An error occured.  Likely your ticket expired.  Reset the workspace to continue.");
            System.out.println(this.getServletName() + " -- ERROR -- " + e.getMessage());
            throw new ServletException(e);
        }

        try {

            // do query
            IDfCollection col = DCTMBasics.runDQLQuery(dql, DCTMsession);

            // get record set
            dmRecordSet rs = new dmRecordSet(col);

            // if results do this
            if (rs.getRowCount() > 0) {
                System.out.println(this.getServletName() + " -- DEBUG -- query returned " + rs.getRowCount() + " rows");
                output.append("<h3>Rows returned: " + rs.getRowCount() + "</h3>");
                if (rs.getRowCount() > maxRows)
                    output.append("(only " + maxRows + " rows displayed)<br/>");
                output.append("<table id='dql'>");
                
                // get all of the column names of the record set 
                ArrayList<IDfAttr> cols = rs.getColumnDefs();

                // print col names as headers 
                output.append("<tr>");
                output.append("<th>Row No.</th>");
                for (IDfAttr a : cols) {
                    output.append("<th>" + a.getName() + "</th>");
                }
                output.append("</tr>");
                
                // print record set content under each column heading 
                boolean alt = false;
                while (rs.hasNext()) {
                    IDfTypedObject tObj = rs.getNextRow();
                    
                    // break if too many rows returned
                    if (rs.getCurrentRowNumber() > maxRows) 
                        break;
                    
                    // set alternating row colors
                    if (alt) {
                        output.append("<tr class='alt'>");
                        alt = false;
                    } else {
                        output.append("<tr>");
                        alt = true;
                    }
                    
                    // output results
                    output.append("<td>" + (rs.getCurrentRowNumber() + 1) + ".</td>");
                    for (IDfAttr a : cols) {
                        output.append("<td>" + tObj.getString(a.getName()) + "</td>");
                    }
                    output.append("</tr>");
                }
                output.append("</table>");
            } else {
                output.append("<h3>No results returned for query.</h3>");
            }

        } catch (Exception e) {
            System.out.println(this.getServletName() + " -- ERROR -- " + e.getMessage());
            response.getWriter().print("<b>ERROR: " + e.getMessage() + "</b>");
        }

        // print return string
        response.getWriter().print(output.toString());

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
        return "D2 DQL Query Servlet";
    }// </editor-fold>

}

// <SDG><