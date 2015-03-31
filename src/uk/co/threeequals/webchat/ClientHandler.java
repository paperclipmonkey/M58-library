package uk.co.threeequals.webchat;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ClientHandler {
    public static String COMMANDSMESSAGE = "Commands are: \n #login:[password] - login as channel admin \n #to:[name];[message] - send private message";
    public static String COMMANDADMINSMESSAGE = "Commands are: \n #getclients - get connected clients \n #logout - Logout as channel admin \n ";
    public static String NAMESUCCESSMESSAGE = "Thanks. You're logged in as ";
    public static String NAMECHOOSEMESSAGE = "Please choose a username:";
    public static String NAMETAKENMESSAGE = "Sorry that username is taken";
    public static String LOGGEDINMESSAGE = "Logged in successfully";
    public static String LOGGEDOUTMESSAGE = "Thanks. You've logged out successfully";
    public static String LOGGEDINFAILUREMESSAGE = "Password incorrect";
    public static String TOPARSESTRING = "#to:";
    public static String LOGINPARSESTRING = "#login:";
    public static String LOGOUTPARSESTRING = "#logout";
    public static String CLIENTSPARSESTRING = "#getclients";
    
    private String name;
    private final LinkedList<Message> queued;
    private final Group group;
    private boolean admin = false;
    private Date lastContact = new Date();
    
    /**
    * Get Client's name
     * @return String name of client
    */
    public String getName(){
        return name;
    }
    
    /**
    * Get Date client was last seen
     * @return Date client last seen
    */
    public Date getDate(){
        return lastContact;
    }
    
    /**
    * Set Date client was last seen
     * @param d Date user was last seen
    */
    public void setDate(Date d){
         lastContact = d;
    }
    
    /**
    * ClientHandler object which is used to store state for connected clients on the server 
    * The g argument must specify a {@link Group}.
    * <p>
    * Returns a ClientHander object with a message queue and message already created. Ready to use.
    *
    * @param  g  The group the client belongs to - think IRC chat server rooms
    */
    public ClientHandler(Group g){
        queued = new LinkedList<>();
        group = g;
        chooseNamePrompt();
        name = null;
    }
    
    /**
    * Add a new message to the message queue for this Client
    * @param m Message to be added to queue
    */
    public void broadcastReceiver(Message m){
        queued.add(m);
        //Accept messages from a group - same place as private messages
    }
    
    /**
    * Add a List of messages to the message queue for this Client
    * @param m Message List to be added to queue
    */
    public void bulkReceiver(List<Message> m){
        for (Message message : m) {
            queued.add(message);
        }
    }
    
    /**
    * Called with String of message text sent by client
    * @param str Message sent by client
    */
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
        } else if(str.startsWith(LOGOUTPARSESTRING) && admin){
            logout();
        } else if(str.startsWith(CLIENTSPARSESTRING) && admin){
            getClients();
        } else {
            mess = new Message(str, null, getName());
            sendMessage(mess);
        }
    }
    
    /**
    * Abstracted message sending function. Takes messages from client and sends them to group
    * @param m Message to be broadcast to group
    */
    public void sendMessage(Message m){
        //Message from the Remote Client
        //Do some clever switching depending on special chars.
        group.receiveMessage(m);
    }
    
    /**
    * If Administrator get clients connected to group
    */
    public void getClients(){
        group.getClientNames(this);
    }
    
    /**
    * Get a single message from the start of the message queue (earliest)
    * Remove returned message from queue
     * @return non-idempotent message from bottom (first) of queue
    */
    public Message getMessage(){
        return queued.poll();
    }
    
    /**
    * Get message array of Message objects from queue
    * Remove returned messages from queue
    * @return non-idempotent all messages from queue
    */
    public Message[] getMessages(){
        Message[] rtnMessages = queued.toArray(new Message[queued.size()]);
        queued.clear();
        return rtnMessages;
    }
    
    /**
    * Check if user knows shared secret
    */
    private void checkLogin(String str){
        str = str.replace(LOGINPARSESTRING, "");//Remove login command
        System.out.println("Password attempt: " + str);
        if("password".equals(str)){
            System.out.println("User " + getName() + " has logged in");
            login();
        } else {
            broadcastReceiver(new Message(LOGGEDINFAILUREMESSAGE));
        }
    }
    
    /**
    * Successfully login to group as administrator
    */
    private void login(){
        Message loginMsg = new Message(LOGGEDINMESSAGE);
        queued.add(loginMsg);
        
        Message loginCommandsMsg = new Message(COMMANDADMINSMESSAGE);
        queued.add(loginCommandsMsg);
        
        admin = true;
    }
    
    /**
    * Successful logout of server by administrator
    */
    private void logout(){
        Message loginMsg = new Message(LOGGEDOUTMESSAGE);
        queued.add(loginMsg);
        admin = false;
    }
    
    /**
    * Add message to message queue if chosen name already taken
    */
    private void chooseNameTakenPrompt(){
        Message m = new Message(NAMETAKENMESSAGE);
        queued.add(m);
    }
    
    /**
    * Add message to message queue asking user to choose a name
    */
    private void chooseNamePrompt(){
        Message m = new Message(NAMECHOOSEMESSAGE);
        queued.add(m);
    }
    
    /**
    * Confirmation of chosen name
    */
    private void chooseNameSuccess(){
        Message m = new Message(NAMESUCCESSMESSAGE + name);
        queued.add(m);        
        
        Message p = new Message(COMMANDSMESSAGE);
        queued.add(p);
        //Add the last x group messages to the local buffer
        bulkReceiver(group.getMessageBuffer());
    }
    
    /**
    * Attempt to set chosen name with Group - needs to be unique
    */
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