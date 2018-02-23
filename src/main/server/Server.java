package main.server;

import main.utils.MyStringUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
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
        rooms.add(new ChatRoom(4, "China"));
        rooms.add(new ChatRoom(5, "Alaska"));
        rooms.add(new ChatRoom(6, "Mars"));
        rooms.add(new ChatRoom(0, "JavaWorld"));

        Server server = new Server(rooms);
        server.startServer();
    }

    private void startServer() {
        try {
            _serverSocket = new ServerSocket(_port);
            _serverSocket.setSoTimeout(5000);
            _logger.info("Server created at: " + _serverSocket);
            _isRunning = true;

            new ThreadStopper().start();

            acceptClients();
        } catch (IOException e) {
            _logger.severe(e.getMessage());
        }
        finally {
            try {
                _serverSocket.close();
            } catch (IOException e) {
                _logger.warning(e.getMessage());
            }
        }
    }

    private void acceptClients() {
        while (_isRunning) {
            try {
                Socket socket;
                try {
                    socket = _serverSocket.accept();
                }
                catch (SocketTimeoutException e){
                    continue;
                }

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


    /*
    * @roomId is a room where @msg will be displayed.
    * If @roomId == 0 then message will be displayed in all rooms.
    *
    * */
    public synchronized void broadcast(int roomId, String msg){
        System.out.println(msg);
        _logger.info("Clients number: " + _clients.size());

        for (int i = 0; i < _clients.size(); i++) {
            if(_clients.get(i).getRoomNo() != roomId && roomId != 0)
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
        _logger.info("Server is shutting down... ");
        broadcast(0, "Server is shutting down... ");

        try {
            _clients.forEach(ServerThread::stopThread);
            _serverSocket.close();
        } catch (IOException e) {
            _logger.warning(e.getMessage());
        }
    }

    public String getStringRepresentationOfRooms(){
        if(_rooms == null)
            return "";

        return _rooms.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    public ChatRoom findRoomByNumber(int id){
        return _rooms.stream().filter(r -> r.getId() == id).findAny().get();
    }

    class ThreadStopper extends Thread{
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);

            while(_isRunning){
                String command = scanner.nextLine();
                if(command.equals("/quit")){
                    stopServer();
                    return;
                }

                if(command.matches("^/drop " + MyStringUtils.USERNAME_PATTERN + "$")){
                    String toBeDropped = command.split(" ")[1];
                    Optional<ServerThread> cl = _clients.stream()
                            .filter(client -> client.getUsername().equals(toBeDropped))
                            .findAny();

                    if(cl.isPresent()){
                        cl.get().stopThread();
                        _clients.remove(cl.get());
                        _logger.info("Dropping the user: <" + toBeDropped +">");
                        continue;
                    }

                   _logger.warning("User <" + toBeDropped + "> not found");
                }
            }
        }
    }
}
