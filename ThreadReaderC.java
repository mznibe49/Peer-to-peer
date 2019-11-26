import javax.sound.midi.Soundbank;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ThreadReaderC extends Thread {

    Socket connectionSocket;
    boolean online = false;
    String mypseudo;

    public ThreadReaderC(Socket connectionSocket,String s){
        this.connectionSocket = connectionSocket;
        this.mypseudo = s;
    }

    @Override
    public void run() {

        try {


            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String rec_msg;
            online = true;
            rec_msg  = inFromClient.readLine(); // le hello:::pseudo
            String hisPseudo = rec_msg.split(":::")[1];
            while( !connectionSocket.isClosed() &&  !(rec_msg = inFromClient.readLine()).toUpperCase().equals("CLOSE")){
                String [] tmptab  = rec_msg.split(":::");
                System.out.println("\n"+hisPseudo+": "+tmptab[1]);
                System.out.print(mypseudo+" : ");
            }
            System.out.println("Good bye From RC");
            connectionSocket.close();
        } catch (Exception exp){
            System.out.println("connexion is over");
            System.out.print("==> ");

        }
    }






}
