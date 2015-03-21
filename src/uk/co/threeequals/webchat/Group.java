package uk.co.threeequals.webchat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author michaelwaterworth
 */
public class Group {
    private List<ClientHandler> clients;
    private LimitedQueue<Message> messages;
    public static int MESSAGEQUEUELENGTH;
    
    Group(){
        clients = new ArrayList<>();
        messages = new  LimitedQueue<Message>(MESSAGEQUEUELENGTH);
    }
    
    public boolean joinGroup(ClientHandler clientH){
        for (ClientHandler client : clients) {
            if(clientH.getName() == client.getName()){//Don't send message back to originator
                return false;
            }
        }
        System.out.println(messages.size());
        clientH.bulkReceiver(messages);//Send all outstanding messages to client on connect
        return clients.add(clientH);
    }
    
    public void leaveGroup(ClientHandler clientH){
        clients.remove(clientH);
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
            if(m.from != client.getName()){//Don't send message back to originator
                client.broadcastReceiver(m);
            }
        }
    }
    
    private void privateMessage(Message m){
        for (ClientHandler client : clients) {
            if(m.to == client.getName()){//Send message only to named recipient
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