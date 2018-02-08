package main.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ServerThread extends Thread{
    private static Logger _logger = Logger.getLogger(ServerThread.class.getName());
    private Socket _socket;
    private Server _server;
    private String _username;
    private DataInputStream _in;
    private DataOutputStream _out;
    private int _roomId;
    private SimpleDateFormat _dateFormat;

    public ServerThread(Socket _socket, Server server) {
        this._socket = _socket;
        this._server = server;
        _dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @Override
    public void run() {
        try {
            _in = new DataInputStream(_socket.getInputStream());
            _out = new DataOutputStream(_socket.getOutputStream());
            _username = _in.readUTF();

            if(!requestRoomIdFromClient())
                return;

            String date = _dateFormat.format(new Date());
            _server.broadcast(_roomId, String.format("%s %s has connected to the room %d", date, _username, _roomId));

            String msg = "";
            while (!_socket.isClosed() && !"/quit".equalsIgnoreCase(msg)) {
                msg = _in.readUTF();
                if(msg.isEmpty())
                    continue;
                date = _dateFormat.format(new Date());
                _server.broadcast(_roomId, String.format("%s %s> %s", date, _username, msg));
            }
        }
        catch (IOException e) {
           // Not much I can do
        }
        finally {
            close();
            String date = _dateFormat.format(new Date());
            _server.broadcast(_roomId, String.format("%s %s disconnected", date, _username));
        }
    }

    private boolean requestRoomIdFromClient() throws IOException {
        _out.writeUTF("Choose the roomNo: \n"
                + _server.getStringRepresentationOfRooms());

        try {
            _roomId = Integer.parseInt(_in.readUTF());
        }
        catch (NumberFormatException e){
            _logger.warning("Wrong roomNo");
            return false;
        }

        ChatRoom room = _server.findRoomByNumber(_roomId);
        if(room == null){
            _logger.warning("Room not found");
            return false;
        }

        return true;
    }

    private void close(){
        try {
            _out.close();
            _in.close();
            _socket.close();
        } catch (IOException e) {
            // Not much I can do
        }
    }

    public DataOutputStream getWriter() {
        return _out;
    }

    public String getUsername() {
        return _username;
    }

    public int getRoomNo() {
        return _roomId;
    }
}
