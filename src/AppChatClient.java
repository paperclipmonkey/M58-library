import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import javax.xml.ws.WebServiceRef;
import uk.co.threeequals.webchat.Message;

public class AppChatClient {
    @WebServiceRef(wsdlLocation="http://localhost:8080/WSChatServer/ChatServerService?wsdl")
    
    private JFrame frame;
    private JTextArea myText;
    private static JTextArea otherText;
    private JScrollPane myTextScroll;
    private JScrollPane otherTextScroll;
    private static TextThread otherTextThread;
    private static ObjectOutputStream out;
    
    private static final int HOR_SIZE = 400;
    private static final int VER_SIZE = 200;
    private static final int VER_SIZE_TYPING = 75;

    private void initComponents() {
    	frame = new JFrame("Chat Client");
        
        /* - - - - - - Returned messages pane - - - - - - - - - */
        otherText = new JTextArea();
        otherTextScroll = new JScrollPane(otherText);
        otherText.setBackground(new java.awt.Color(200, 200, 200));
        otherTextScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        otherTextScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        otherTextScroll.setMaximumSize(
            new java.awt.Dimension(HOR_SIZE, VER_SIZE));
        otherTextScroll.setMinimumSize(
            new java.awt.Dimension(HOR_SIZE, VER_SIZE));
        otherTextScroll.setPreferredSize(new java.awt.Dimension(
		    HOR_SIZE, VER_SIZE));
        otherText.setEditable(false);
               
        frame.getContentPane().add(otherTextScroll,
            java.awt.BorderLayout.NORTH);
        
        
        /* - - - - - - Typed messages pane - - - - - - - - - */
        myText = new JTextArea();
        myTextScroll = new JScrollPane(myText);			
        myTextScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		myTextScroll.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		myTextScroll.setMaximumSize(
		    new java.awt.Dimension(HOR_SIZE, VER_SIZE));
		myTextScroll.setMinimumSize(new java.awt.Dimension(HOR_SIZE, VER_SIZE_TYPING));
		myTextScroll.setPreferredSize(new java.awt.Dimension(
		    HOR_SIZE, VER_SIZE_TYPING));

        myText.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textTyped(evt);
            }
        });
        frame.getContentPane().add(myTextScroll, java.awt.BorderLayout.SOUTH);
            
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    public void showMessage(Message m){
        String apMesStr = "";
        
        if(m.to != null){
            apMesStr = apMesStr + "Private message from ";
        }
        if(m.from == null){
            m.from = "System";
        }
        
        apMesStr = apMesStr + m.from + ": ";
        apMesStr = apMesStr + m.body + "\n";
        otherText.append(apMesStr);
    }
    
    private void initConnection(String host){
        try {
            Socket mySocket = new Socket(host, 2048);
            otherTextThread = new TextThread(this, mySocket);
            OutputStream temp = mySocket.getOutputStream();
            out = new ObjectOutputStream(temp);
            otherTextThread.start();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                      try {
                          out.writeObject(new Message("Exiting"));
                      }
                      catch (Exception ex) {
                          showMessage(new Message("Exit failed"));
                      }
                      System.exit(0);
                }
            });
        }
        
        catch (Exception ex) {
            showMessage(new Message("Failed to connect to server"));
        }
    }

    private void textTyped(java.awt.event.KeyEvent evt) {
        char c = evt.getKeyChar();
        if (c == '\n'){
            try {
                JTextArea jta = (JTextArea) evt.getComponent();
                String typed = jta.getText();
                jta.setText(null);

                Message m;
                m = new Message(typed.replace("\n", ""), null, "Me");

                showMessage(m);
                out.writeObject(m);
            }
            catch (IOException ie) {
                showMessage(new Message("Failed to send message"));
            }
        }
    }
    
    
    public static void main(String[] args) {
    	if (args.length < 1) {
            System.out.println("Usage: AppChatClient host");
            return;
    	}
    	final String host = args[0];
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            AppChatClient client = new AppChatClient();
            @Override
            public void run() {
                client.initComponents();
                client.initConnection(host);
            }
    	});
    	
    }
}

class TextThread extends Thread {

    ObjectInputStream in;
    AppChatClient client;
    Socket socket;

    TextThread(AppChatClient clientC, Socket mySocket) throws IOException{
        client = clientC;
        socket = mySocket;
    }
    
    @Override
    public void run() {
        try {    	
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Object message = in.readObject();
                if ((message == null) || (!(message instanceof Message))){
                    client.showMessage(new Message("Error reading from server"));
                    return;
                }
                Message m = (Message)message;
                client.showMessage(new Message(m.body));
            }
        }
        catch (IOException | ClassNotFoundException e) {
                client.showMessage(new Message("Error reading from server"));
        }
    }
}