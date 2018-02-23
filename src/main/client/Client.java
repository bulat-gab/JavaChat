package main.client;

import main.server.Config;
import main.utils.MyStringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {
    private static Logger _logger = Logger.getLogger(Client.class.getName());
    private String _username;
    private Socket _socket;
    private DataOutputStream _out;
    private DataInputStream _in;
    private Scanner _scanner;

    public Client(String _username) {
        this._username = _username;
        _scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String username = sc.nextLine();
        if(!MyStringUtils.validateUsername(username)){
            System.out.println("Username name must not be empty or longer than 10 symbols");
            return;
        }

        Client client = new Client(username);
        client.startClient();
    }

    public void startClient(){
        try {
            connectAndChooseRoom();

            // ClientThread listens from Server
            ClientThread clientThread = new ClientThread(_socket);
            clientThread.start();

            String msg = "";
            infoMessage();
            while(!_socket.isClosed() && !"/quit".equalsIgnoreCase(msg)){
                msg = _scanner.nextLine();
                _out.writeUTF(msg);
                _out.flush();
            }
        }
        catch (IOException e) {
            _logger.warning("Connection error: " + e);
        }
        finally {
            stopClient();
        }
    }

    private void infoMessage() {
        System.out.println("Welcome, " + _username + "!");
        System.out.println("Type /quit to leave a chat.");
        System.out.println("Type a /room #roomNo to enter change a room.\n");
    }

    private void connectAndChooseRoom()  {
        try{
            _socket = new Socket(InetAddress.getLocalHost(), Config.port);
            _out = new DataOutputStream(_socket.getOutputStream());
            _in = new DataInputStream(_socket.getInputStream());

            _logger.info("Connected to " + _socket);
            _out.writeUTF(_username);

            String rooms = _in.readUTF();
            System.out.println(rooms);

            int roomId;
            try {
                roomId = Integer.parseInt(_scanner.nextLine());
                _out.writeUTF(roomId + "");
            }
            catch (NumberFormatException e){
                _logger.warning("Wrong room number: " );
                stopClient();
                throw e;
            }
        }
        catch (IOException e){
            _logger.warning("Connection error: " + e);
        }
    }

    public void stopClient(){
        try{
            _out.close();
            _in.close();
            _socket.close();
        }
        catch (IOException e){
            // Not much I can do
        }
    }
}
