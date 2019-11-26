import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ThreadClient extends Thread {

    public Socket connectionSocket;
    ServerSocket clientSV;
    boolean online =false;
    String myPseudo;


    public ThreadClient(ServerSocket c,String s){
        this.clientSV = c;
        this.myPseudo = s;
    }


    public void run(){
        while(true) {
            // attente de connexion
            connectionSocket = null;
            try {
                connectionSocket = clientSV.accept();
                online = true;
                String tmp_msg = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                System.out.println("someone is trying to connect with ! confirm by tapping \"OK\"\nAny other answer will be considered as a No !");
                System.out.print("==> ");

                tmp_msg  = reader.readLine(); // le hello:::pseudo
                String hisPseudo = tmp_msg.split(":::")[1];

                while (online && !(tmp_msg = reader.readLine()).toUpperCase().equals("CLOSE")) {
                    String [] tmptab  = tmp_msg.split(":::");
                    System.out.println("\n"+hisPseudo+" : " + tmptab[1]);
                    System.out.print(myPseudo+" : ");
                }
                online = false;
                reader.close();
                System.out.println("Good bye!");
                System.out.print("==> ");
            } catch (IOException e) {
                System.out.println("le client s'est deconecté");
                System.out.print("==> ");
                online = false;
                break;
            }
        }

    }
}
