import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9099);
            Socket socket = serverSocket.accept();
            System.out.println("Client accepted " + socket);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String msg = "";

            while(!"/quit".equalsIgnoreCase(msg)){
                System.out.println("Waiting for incoming messages... ");
                msg = in.readUTF();
                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
