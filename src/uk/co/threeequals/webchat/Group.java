package uk.co.threeequals.webchat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author michaelwaterworth
 */
public class Group {
    private final List<ClientHandler> clients;
    private final LimitedQueue<Message> messages;//Hold a small number of messages in the Group and add them to queue of client on connect - similar ot IRC
    public static final int MESSAGEQUEUELENGTH = 10;
    public static final String LOGOUTMESSAGE = " has logged out";
    private String adminPassword;
    
    public Group(){
        clients = new ArrayList<>();
        messages = new  LimitedQueue<>(MESSAGEQUEUELENGTH);
    }
    
    public void setPassword(String pw){
        adminPassword = pw;
    }
    
    public Boolean checkPassword(String guess){
        if(guess.matches(adminPassword)){
            return true;
        }
        return false;
    }
    
    public boolean joinGroup(ClientHandler clientH){
        for (ClientHandler client : clients) {
            if(clientH.getName() == null ? client.getName() == null : clientH.getName().equals(client.getName())){//Don't send message back to originator
                return false;
            }
        }
        clientH.bulkReceiver(messages);//Send all outstanding messages to client on connect
        return clients.add(clientH);
    }
    
    public void leaveGroup(ClientHandler clientH){
        clients.remove(clientH);
        Message m = new Message(clientH.getName() + LOGOUTMESSAGE);
        receiveMessage(m);
    }
    
    public void receiveMessage(Message m){
        if(m.to == null){
            broadcastMessage(m);
        } else {
           privateMessage(m);
        }
    }
    
    private void broadcastMessage(Message m){
        messages.add(m);
        for (ClientHandler client : clients) {
            if(m.from == null ? client.getName() != null : !m.from.equals(client.getName())){//Don't send message back to originator
                client.broadcastReceiver(m);
            }
        }
    }
    
    private void privateMessage(Message m){
        for (ClientHandler client : clients) {
            if(m.to == null ? client.getName() == null : m.to.equals(client.getName())){//Send message only to named recipient
                client.broadcastReceiver(m);
            }
        }
    }
    
    public void getClientNames(ClientHandler cl){
        String clientNames = "";
        for (ClientHandler client : clients) {
            clientNames += client.getName() + ", ";
        }
        receiveMessage(new Message(clientNames, cl.getName(),""));
    }
    
    public List<Message> getMessageBuffer(){
        return messages;
    }
}