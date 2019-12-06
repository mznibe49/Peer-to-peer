import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;


public class Client {
    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.trustStore",
                "client.jks");

        System.setProperty("javax.net.ssl.trustStorePassword", "EZ4ENCE");
        try {

            String msg_received = "";
            SSLSocketFactory socketFactory = (SSLSocketFactory)
                    SSLSocketFactory.getDefault();
            SSLSocket clientSocket =
                    (SSLSocket)socketFactory.createSocket(args[0], 1027);


            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            msg_received = inFromServer.readLine();
            msg_received = AES.decrypt(msg_received, "EZ4ENCE").replaceAll(":::", "\n");

            System.out.println("FROM SERVER: " + msg_received);

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            String pseudo = "";


            while (true) {

                System.out.println("Do you have an account ? O/N");
                Scanner sc = new Scanner(System.in);
                String rep = sc.nextLine();
                switch (rep) {
                    case "O":
                        System.out.println("enter your pseudo : ");
                        String rep1 = sc.nextLine();
                        pseudo = rep1;
                        System.out.println("enter your password : ");
                        String rep2 = sc.nextLine();
                        outToServer.writeBytes(AES.encrypt("SIGNIN:::" + rep1 + ":::" + rep2, "EZ4ENCE")+'\n');
                        outToServer.flush();
                        break;
                    case "N":
                        System.out.println("Enter your pseudo : ");
                        String rep3 = sc.nextLine();
                        pseudo = rep3;
                        Console console = System.console();
                        char[] hidden = console.readPassword("Enter your password : ", '*');
                        String rep4 = new String(hidden);
                        System.out.println(AES.encrypt("SIGNIN:::" + rep3 + ":::" + rep4, "EZ4ENCE")) ;
                        outToServer.writeBytes(AES.encrypt("SIGNUP:::" + rep3 + ":::" + rep4, "EZ4ENCE")+'\n');
                        outToServer.flush();
                        break;
                    default:
                        break;

                }

                if (rep.equals("O") || rep.equals("N")) {

                    String x = inFromServer.readLine();
                    System.out.println(x);
                    x= AES.decrypt(x, "EZ4ENCE");
                    System.out.println(x);
                    x = x.trim();
                    String[] res = x.split(":::");
                    System.out.println(res[0] + " : " + res[1]);
                    if (!res[0].equals("ERROR")) {
                        break;
                    }
                }
            }
            String msg2 = "\nListe des commandes \n";
            String msg3 = "\tGET_ANN : pour voir tout les annonces \n";
            String msg4 = "\tGET_ANN_BY (prix/domaine --> key) (value) : filtrer les annonces\n";
            String msg5 = "\tPOST : ajouter une annonce\n";
            String msg6 = "\tREMOVE_BY (id) : supprimer une de ses annonces\n";
            String msg7 = "\tREMOVE_ALL : supprimer tout ses annonces\n";
            String msg8 = "\tGET_CLIENTS : voir tout les clients connectés au serveur\n";
            String msg9 = "\tGET_CLIENTS_BY :(pseudo/adr etc)(value) : filtrer les clients\n";
            String msg10 = "\tEXIT : pour se deconnecter";
            String res = msg2+msg3+msg4+msg5+msg6+msg7+msg8+msg9+msg10;
            System.out.println(res);
            //String

            String rec_msg = "";

            ServerSocket clientSV = new ServerSocket(0);
            outToServer.writeBytes(AES.encrypt("PORT:::" + clientSV.getLocalPort(), "EZ4ENCE")+'\n');

            outToServer.flush();

            ThreadClient tc = new ThreadClient(clientSV,pseudo);
            tc.start();

            String currStr;
            System.out.print("\n==> ");
            while (true) {
                if ((currStr = inFromUser.readLine()).toUpperCase().equals("EXIT")) {
                    break;
                }
                String msg_to_send = getMsgToSend(currStr,inFromServer,outToServer);
                String[] tab = msg_to_send.split(":::");
                String clientRequest = tab[0];

                outToServer.writeBytes(AES.encrypt(msg_to_send , "EZ4ENCE")+'\n');
                outToServer.flush();

                if (tc.online) {

                    if(clientRequest.toUpperCase().equals("OK")){
                        Socket s = tc.connectionSocket;
                        DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                        String tmp;
                        outToClient.writeBytes(AES.encrypt("HELLO:::"+pseudo , "EZ4ENCE")+'\n');
                        outToClient.flush();
                        writeInSoc(s,outToClient,pseudo);

                        tc.online = false;
                        outToClient.close();
                        s.close();
                    } else {
                        tc.online = false;
                        Socket s = tc.connectionSocket;
                        DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                        outToClient.writeBytes(AES.encrypt("CLOSE" , "EZ4ENCE")+'\n');
                        outToClient.flush();
                        outToClient.close();
                        s.close();
                    }
                    System.out.print("\n==> ");
                } else {

                    if (clientRequest.equals("GET_ANN") || clientRequest.equals("GET_ANN_BY")) {
                        printGetResult(inFromServer);
                    } else if (clientRequest.equals("REMOVE_BY")) {
                        String x = inFromServer.readLine();
                        x =  AES.decrypt(x, "EZ4ENCE");
                        System.out.println(x);
                    }  else if (clientRequest.equals("REMOVE_ALL")) {
                        String x = inFromServer.readLine();
                        x = AES.decrypt(x, "EZ4ENCE");
                        System.out.println(x);
                    }else if (clientRequest.equals("POST")) {
                        String x = inFromServer.readLine();
                        x =AES.decrypt(x, "EZ4ENCE");
                        System.out.println(x);
                    } else if (clientRequest.equals("GET_CLIENTS") || clientRequest.equals("GET_CLIENTS_BY")) {
                        printGetResult(inFromServer);
                    } else if (clientRequest.equals("CONNECT_TO")) {

                        System.out.println("Trying to establish connection with client: "+tab[1]);
                        outToServer.writeBytes(AES.encrypt("GET_CLIENTS_BY:::PSEUDO:::"+tab[1] , "EZ4ENCE")+'\n');
                        outToServer.flush();
                        String x = inFromServer.readLine(); // get_client_by:::size
                        x = AES.decrypt(x, "EZ4ENCE").split(":::")[1];
                        System.out.println("x : "+x);
                        if (x.equals("0")) {
                            System.out.println("Client non existant !");
                        } else {

                            x = inFromServer.readLine();
                            x = AES.decrypt(x, "EZ4ENCE");
                            System.out.println("HOHO :"+x);
                            String[] tmp  = x.split(":::");
                            if (Integer.parseInt(tmp[3]) != 0){
                                Socket clientSoc = new Socket(tmp[2], Integer.parseInt(tmp[3]));
                                System.out.println("in adr : " + clientSoc.getInetAddress() + ", port : " + clientSoc.getPort());
                                String env_msg;
                                DataOutputStream outToClient = new DataOutputStream(clientSoc.getOutputStream());
                                outToClient.writeBytes(AES.encrypt("HELLO:::" + pseudo  , "EZ4ENCE")+'\n');
                                outToClient.flush();
                                ThreadReaderC b = new ThreadReaderC(clientSoc, pseudo);
                                b.start();
                                writeInSoc(clientSoc,outToClient,pseudo);
                                outToClient.close();
                                clientSoc.close();
                            }else{
                                System.out.println("Disconnected Client !");
                            }
                        }

                    }else if(clientRequest.equals("PORT")){
                        tc.interrupt();
                        clientSV.close();
                        clientSV = new ServerSocket(Integer.parseInt(tab[1]));
                        tc = new ThreadClient(clientSV,pseudo);
                        tc.start();
                        System.out.println("Port mis à jour à: "+tab[1]);
                    }
                    System.out.print("\n==> ");
                }
            }

            System.out.println("Connexion closed with server !");
            // msg du client au service pour indiquer la deconnexion
            outToServer.writeBytes(AES.encrypt("EXIT"  , "EZ4ENCE")+'\n');
            outToServer.flush();
            clientSV.close();
            clientSocket.close();
            inFromServer.close();
            outToServer.close();

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void writeInSoc(Socket s,DataOutputStream outToClient, String pseudo) throws IOException {
        String tmp;
        while (true) {
            System.out.print(pseudo+" : ");
            Scanner sc = new Scanner(System.in);
            tmp = sc.nextLine();
            if(s.isClosed()) break;
            if(tmp.toUpperCase().equals("CLOSE")){
                outToClient.writeBytes(AES.encrypt("CLOSE"  , "EZ4ENCE")+'\n');
            }else {
                outToClient.writeBytes(AES.encrypt("WHISP:::" + tmp  , "EZ4ENCE")+'\n');
            }
            outToClient.flush();
            if (tmp.toUpperCase().equals("CLOSE")) break;
        }
    }

    public static void printGetResult(BufferedReader inFromServer) throws IOException {
        String res = inFromServer.readLine();
        res = AES.decrypt(res, "EZ4ENCE");
        String tmp0 [] = res.split(":::");
        int size = Integer.parseInt(tmp0[1]);
        int k = 0;
        while(k<size){
            String s = inFromServer.readLine();
            s = AES.decrypt(s, "EZ4ENCE");
            String[] tmp = s.split(":::");
            if(tmp0[0].equals("GET_ANN") || (tmp0[0].equals("GET_ANN_BY"))){
                System.out.println("ID      : " + tmp[0]);
                System.out.println("Domaine : " + tmp[1]);
                System.out.println("Prix    : " + tmp[2]);
                System.out.println("Descrip : " + tmp[3]);
                System.out.println("Client  : " + tmp[4] + "\n");
            } else { // get_clients or get_clients_by
                System.out.println("ID      : " + tmp[0]);
                System.out.println("PSEUDO : " + tmp[1]);
                System.out.println("ADRESS    : " + tmp[2]);
                System.out.println("PORT : " + tmp[3]);
            }

            k++;
        }
        if(k==0) System.out.println("No announce found ! :s");
    }

    public static boolean isPortAvailable(String host, int port){
        boolean result = true;

        try {

            (new Socket(host, port)).close();
            // Successful connection means the port is taken.
            result = false;
        } catch (IOException e) {
            // Port Isn't Used !!
        }

        return result;
    }

    public static String getMsgToSend(String currStr,BufferedReader inFromServer,DataOutputStream outToServer) {

        switch (currStr.toUpperCase()) {

            case "POST":
                String domaine, prix = "", desc;
                boolean err = true;
                Scanner sc = new Scanner(System.in);
                System.out.print("\tDomaine : ");
                domaine = sc.nextLine();
                while (err) {
                    System.out.print("\tPrix : ");
                    prix = sc.nextLine();
                    try {
                        Integer.parseInt(prix); // pour faire manifester l'exception
                        err = false;
                    } catch (NumberFormatException e) {
                        System.out.print("\tVeuillez mettre la bonne format au prix");
                        err = true;
                    }
                }
                System.out.print("\tDescription : ");
                desc = sc.nextLine();
                return "POST:::" + domaine + ":::" + prix + ":::" + desc;

            case "GET_ANN":
                return "GET_ANN";

            case "GET_CLIENTS":
                return "GET_CLIENTS";

            case "GET_ANN_BY":
                String byWhat, contentOfWhat;
                Scanner sc2 = new Scanner(System.in);
                System.out.print("\tBy 'ID' , 'DOMAINE','PRIX' ? ");
                byWhat = sc2.nextLine();
                System.out.print("\t Valeur : ");
                contentOfWhat = sc2.nextLine();
                return "GET_ANN_BY:::" + byWhat + ":::" + contentOfWhat;

            case "GET_CLIENTS_BY":
                sc2 = new Scanner(System.in);
                System.out.print("\tBy 'ID' , 'PSEUDO','ADRESS','PORT' ? ");
                byWhat = sc2.nextLine();
                System.out.print("\tValeur : ");
                contentOfWhat = sc2.nextLine();
                return "GET_CLIENTS_BY:::" + byWhat + ":::" + contentOfWhat;

            case "REMOVE_ALL":
                return "REMOVE_ALL";

            case "REMOVE_BY": // a faire
                Scanner sc3 = new Scanner(System.in);
                System.out.print("\tBy 'ID' : ");
                byWhat = sc3.nextLine();
                return "REMOVE_BY:::ID:::" + byWhat;

            case "CONNECT_TO":
                String pseudo;
                sc2 = new Scanner(System.in);
                System.out.print("\tPseudo : ");
                pseudo = sc2.nextLine();

                return "CONNECT_TO:::" + pseudo;

            case "PORT":
                sc2 = new Scanner(System.in);
                System.out.println("\tPort : ");
                String port = sc2.nextLine();
                boolean av = true;
                try {
                    outToServer.writeBytes(AES.encrypt("GET_CLIENTS_BY:::PORT:::"+port  , "EZ4ENCE")+'\n');
                    outToServer.flush();
                    String myIp = getIpFromLocal();
                    String res = inFromServer.readLine();
                    res = AES.decrypt(res, "EZ4ENCE");
                    String x = res.split(":::")[1]; // get_client_by:::size
                    System.out.println("x : "+x);
                    if (x.equals("0")) {
                        if(isPortAvailable("localhost",Integer.parseInt(port))) {
                            return "PORT:::" + port;
                        }
                    }else{
                        String tmp0 [] = res.split(":::");
                        int size = Integer.parseInt(x);
                        int k = 0;
                        while(k<size){
                            String s = inFromServer.readLine();
                            s = AES.decrypt(s, "EZ4ENCE");
                            String[] tmp = s.split(":::");
                            if(tmp[3].equals(port) && tmp[2].equals(myIp)){
                                av = false;
                                break;
                            }
                            k++;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(av){
                    return "PORT:::" + port;
                }else{
                    System.out.println("Port isn't available");
                }
                break;
            case "OK":
                return "OK";


            default:
                break;

        }

        return "";
    }
    public static String getIpFromLocal() {
        String res = "";
        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); ) {
                NetworkInterface e = n.nextElement();
                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); ) {
                    InetAddress addr = a.nextElement();
                    res = addr.getHostAddress();
                    String [] tmp = res.split("\\.");
                    if(tmp.length== 4) return res;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}

