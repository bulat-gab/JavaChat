package main.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Server {
    private static Logger _logger = Logger.getLogger(Server.class.getName());
    private List<ServerThread> _clients;
    private ServerSocket _serverSocket;
    private int _port;
    private boolean _isRunning = false;
    private List<ChatRoom> _rooms;

    public Server(List<ChatRoom> rooms) {
        this._clients = new ArrayList<>();
        this._port = Config.port;
        this._rooms = rooms;
    }

    public static void main(String[] args) {
        List<ChatRoom> rooms = new ArrayList<>();
        rooms.add(new ChatRoom(1, "Russia"));
        rooms.add(new ChatRoom(2, "USA"));
        rooms.add(new ChatRoom(3, "Europe"));

        Server server = new Server(rooms);
        server.startServer();
    }

    private void startServer() {
        try {
            _serverSocket = new ServerSocket(_port);
            _logger.info("Server created at: " + _serverSocket);
            _isRunning = true;

            acceptClients();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClients() {
        while (_isRunning) {
            try {

                Socket socket = _serverSocket.accept();
                _logger.info("client accepted " + socket.getInetAddress()
                        + ":" + socket.getPort() + "\n");

                ServerThread serverThread = new ServerThread(socket, this);
                _clients.add(serverThread);
                serverThread.start();
            }
            catch(IOException e){
                _logger.info("Error accepting client " + e);
            }
         }
    }

    public synchronized void broadcast(int roomNo, String msg){
        System.out.println(msg);
        _logger.info("Clients number: " + _clients.size());

        for (int i = 0; i < _clients.size(); i++) {
            if(_clients.get(i).getRoomNo() != roomNo)
                continue;

            DataOutputStream out = _clients.get(i).getWriter();
            try {
                if(out != null)
                    out.writeUTF(msg);
                else
                    _clients.remove(i);
            }
            catch (IOException e){
                _logger.warning("Error sending message to "
                        + _clients.get(i).getUsername() + " " + e);
                _clients.remove(i);
            }
        }
    }

    public void stopServer(){
        _isRunning = false;
    }

    public List<ChatRoom> getRooms() {
        return _rooms;
    }

    public String getStringRepresentationOfRooms(){
        if(_rooms == null)
            return "";

        return _rooms.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    public ChatRoom findRoomByNumber(int id){
        return _rooms.stream().filter(r -> r.getId() == id).findFirst().get();
    }

}
