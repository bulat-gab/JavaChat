import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), 9099);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connected to " + socket);

            Scanner scanner = new Scanner(System.in);
            String msg = "";
            while(!"/quit".equalsIgnoreCase(msg)){
                msg = scanner.nextLine();
                out.writeUTF(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
