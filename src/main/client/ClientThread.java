package main.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/*
* Class for listening incoming messages from main.server
*
* */

public class ClientThread extends Thread{
    private static Logger _logger = Logger.getLogger(ClientThread.class.getName());
    private Socket _socket;

    public ClientThread(Socket socket){
        this._socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(_socket.getInputStream());

            while(!_socket.isClosed()){
                String message = in.readUTF();
                System.out.println(message);
            }

        } catch (IOException e) {
            _logger.warning("Error while listening from server: " + e);
        }
    }
}
