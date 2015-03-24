package uk.co.threeequals.webchat;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author michaelwaterworth
 */
public class Message implements Serializable{
    public String to;
    public String from;
    public Date ts;
    public String body;
    
    public Message(){
        
    }
    
    public Message(String b){
        body = b;
        ts = new Date();
    }
    
    public Message(String b, String t, String f){
        body = b;
        to = t;
        from = f;
        ts = new Date();
    }
    
}