/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import java.io.IOException;

/**
 * Signals that an error has occurred in the Pathway Commons search or communication with the cPath2 web service
 * May be used when the web service returns a HTTP status code with a value other than 200
 * @author Derek Wright
 */
public class PathwayCommonsException extends IOException
{
    private int statusCode;

    /**
     * Constructor
     * @param statusCode - The HTTP status code returned by the cPath2 web service
     */
    public PathwayCommonsException(int statusCode)
    {
        this.statusCode = statusCode;
    }
    
    /*
     * The HTTP status code returned by the cPath2 web service
     * @return the status code
     */
    public int getStatusCode() 
    {
        return statusCode;
    }

    @Override
    public String getMessage() 
    {
        String message = "";
        switch(statusCode){
            case 460:
                message = "No results found";
                break;
                
            case 452:
                message = "Bad request (illegal or no arguments)";
                break;                

            default:
                message = "Unable to reach Pathway Commons";
                break;
        }
        message += " (status code: " + statusCode + ")";
        return message;
    }
}
