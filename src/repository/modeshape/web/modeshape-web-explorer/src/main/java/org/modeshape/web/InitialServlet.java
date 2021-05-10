/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.modeshape.common.logging.Logger;

/**
 * @author kulikov
 */
public class InitialServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Logger logger = Logger.getLogger(InitialServlet.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest( HttpServletRequest request,
                                   HttpServletResponse response ) throws ServletException, IOException {
        String authHeader = ((HttpServletRequest)request).getHeader("Authorization");
        String pathInfo= request.getPathInfo();
        
        Object marker = request.getSession().getAttribute("login-required-marker");
        
        if (marker == null && pathInfo.equals("login.do")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate", "Basic realm =\"Realm\"");
            request.getSession().setAttribute("login-required-marker", Boolean.FALSE);
            return;
        }

        if (pathInfo.equals("login.do")) {
            response.sendRedirect(request.getContextPath() + "/Console.html");
            return;
        }
        
        String url = request.getRequestURI();
        String servletPath = request.getServletPath();

        request.getSession(true).setAttribute("initial.uri", url);
        logger.debug("Store requested uri " + url);
        
        String dest = url.substring(0, url.indexOf(servletPath));
        response.sendRedirect(dest);
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
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException, IOException {
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
    protected void doPost( HttpServletRequest request,
                           HttpServletResponse response ) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * 
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
