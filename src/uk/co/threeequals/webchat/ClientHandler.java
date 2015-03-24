package uk.co.threeequals.webchat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author michaelwaterworth
 */
public class ClientHandler {
    public static String NAMESUCCESSMESSAGE = "Thanks. You're logged in as ";
    public static String NAMECHOOSEMESSAGE = "Please choose a username:";
    public static String NAMETAKENMESSAGE = "Sorry that username is taken";
    public static String LOGGEDINMESSAGE = "Logged in successfully";
    public static String LOGGEDINFAILUREMESSAGE = "Password incorrect";
    public static String TOPARSESTRING = "#to:";
    public static String LOGINPARSESTRING = "#login:";
    public static String CLIENTSPARSESTRING = "#getclients";
    
    private String name;
    private List<Message> queued;
    private Group group;
    private boolean admin = false;
    
    public String getName(){
        return name;
    }
    
    public ClientHandler(Group g){
        //System.out.println("New Client Handler");
        queued = new ArrayList<>();
        group = g;
        chooseNamePrompt();
        name = null;
    }
    
    public void timeOut(){
        //trash class
    }
    
    public void broadcastReceiver(Message m){
        queued.add(m);
        //Accept messages from a group - same place as private messages
    }
    
    public void bulkReceiver(List<Message> m){
        for (Message message : m) {
            queued.add(message);
        }
    }
    
    public void processSendMessage(String str){
        Message mess;
        if(name == null){
            setName(str);
        }
        else if (str.startsWith(TOPARSESTRING)){
            str = str.replace(TOPARSESTRING, "");//Remove to
            String[] splitStr = str.split(";");
            mess = new Message(splitStr[1], splitStr[0], name);
            sendMessage(mess);
        } else if(str.startsWith(LOGINPARSESTRING)){
            checkLogin(str);
        } else if(str.startsWith(CLIENTSPARSESTRING) && admin){
            getClients();
        } else {
            mess = new Message(str, null, getName());
        }
    }
    
    public void sendMessage(Message m){
        //Message from the Remote Client
        //Do some clever switching depending on special chars.
        group.receiveMessage(m);
    }
    
    public void getClients(){
        group.getClientNames(this);
    }
    
    public Message[] getMessages(){
        Message[] rtnMessages = queued.toArray(new Message[queued.size()]);
        queued.clear();
        return rtnMessages;
    }
    
    private void checkLogin(String str){
        str = str.replace(LOGINPARSESTRING, "");//Remove login command
        if(str == "password"){
            login();
            return;
        } else {
            broadcastReceiver(new Message(LOGGEDINFAILUREMESSAGE, null, null));
        }
    }
    
    private void login(){
        Message loginMsg = new Message(LOGGEDINMESSAGE);
        queued.add(loginMsg);
        admin = true;
    }
    
    private void chooseNameTakenPrompt(){
        Message m = new Message(NAMETAKENMESSAGE);
        queued.add(m);
    }
    
    private void chooseNamePrompt(){
        Message m = new Message(NAMECHOOSEMESSAGE);
        queued.add(m);
    }
    
    private void chooseNameSuccess(){
        Message m = new Message(NAMESUCCESSMESSAGE + name);
        queued.add(m);
    }
    
    private boolean setName(String n){
        name = n;
        if(group.joinGroup(this)){
            chooseNameSuccess();
            return true;
        }
        name = null;
        chooseNameTakenPrompt();
        chooseNamePrompt();
        return false;
    }
    
}