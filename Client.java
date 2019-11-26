import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client {



    public static void main(String[] args) {


        try {

            String msg_received = "";
            Socket clientSocket = new Socket(args[0], 1027);

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            msg_received = inFromServer.readLine().replaceAll(":::", "\n");

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
                        outToServer.writeBytes("SIGNIN:::" + rep1 + ":::" + rep2+'\n');
                        outToServer.flush();
                        break;
                    case "N":
                        System.out.println("enter your pseudo : ");
                        String rep3 = sc.nextLine();
                        pseudo = rep3;
                        System.out.println("enter your password : ");
                        String rep4 = sc.nextLine();
                        outToServer.writeBytes("SIGNUP:::" + rep3 + ":::" + rep4+'\n');
                        outToServer.flush();
                        break;
                    default:
                        break;

                }

                if (rep.equals("O") || rep.equals("N")) {
                    String x = inFromServer.readLine();
                    x = x.trim();
                    String[] res = x.split(":::");
                    System.out.println(res[0] + " : " + res[1]);
                    if (!res[0].equals("ERROR")) {
                        break;
                    }
                }
            }

            String y = inFromServer.readLine();
            String[] res = y.split(":::"); // menu
            for (int i = 0; i < 9; i++)
                System.out.println(res[i]);

            ServerSocket clientSV = new ServerSocket(0);
            outToServer.writeBytes("PORT:::" + clientSV.getLocalPort()+'\n');
            outToServer.flush();

            ThreadClient tc = new ThreadClient(clientSV,pseudo);
            tc.start();

            String currStr;
            System.out.print("\n==> ");
            while (true) {
                if ((currStr = inFromUser.readLine()).toUpperCase().equals("EXIT")) {
                    break;
                }

                String msg_to_send = getMsgToSend(currStr);
                String[] tab = msg_to_send.split(":::");
                String clientRequest = tab[0];

                outToServer.writeBytes(msg_to_send + '\n');
                outToServer.flush();

                System.out.println("online : "+tc.online);
                if (tc.online) {

                    if(clientRequest.toUpperCase().equals("OK")){
                        Socket s = tc.connectionSocket;
                        DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                        String tmp;
                        outToClient.writeBytes("HELLO:::"+pseudo+"\n");
                        outToClient.flush();
                        while (true) {
                            System.out.print(pseudo+" : ");
                            Scanner sc = new Scanner(System.in);
                            tmp = sc.nextLine();
                            if(s.isClosed()) break;
                            if(tmp.toUpperCase().equals("CLOSE")){
                                outToClient.writeBytes("CLOSE\n");
                            }else {
                                outToClient.writeBytes("WHISP:::"+tmp + "\n");
                            }
                            outToClient.flush();
                            if (tmp.toUpperCase().equals("CLOSE")) break;
                        }
                        tc.online = false;
                        outToClient.close();
                        s.close();
                    } else {
                        tc.online = false;
                        Socket s = tc.connectionSocket;
                        DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                        outToClient.writeBytes( "CLOSE\n");
                        outToClient.flush();
                        outToClient.close();
                        s.close();
                    }
                    System.out.print("\n==> ");
                } else {

                    if (clientRequest.equals("GET_ANN") || clientRequest.equals("GET_ANN_BY")) {
                        String x = inFromServer.readLine();
                        String[] annonce = x.split("&&&");
                        if (x.equals("")) {
                            System.out.println("Pas d'annonce pour le moment !");
                        } else {
                            for (String s : annonce) {
                                String[] tmp = s.split(":::");
                                System.out.println("ID      : " + tmp[0]);
                                System.out.println("Domaine : " + tmp[1]);
                                System.out.println("Prix    : " + tmp[2]);
                                System.out.println("Descrip : " + tmp[3]);
                                System.out.println("Client  : " + tmp[4] + "\n");
                            }
                        }
                    } else if (clientRequest.equals("REMOVE_BY")) {
                        String x = inFromServer.readLine();
                        System.out.println("Removed : " + x);
                    } else if (clientRequest.equals("POST")) {
                        String x = inFromServer.readLine();
                        System.out.println(x);
                    } else if (clientRequest.equals("GET_CLIENTS") || clientRequest.equals("GET_CLIENTS_BY")) {
                        //try {

                        String x = inFromServer.readLine();
                        String[] clients = x.split("&&&");
                        if (x.equals("")) {
                            System.out.println("Vous êtes le seul connecté pour le moment (business ain't going well in these dark days :/ !)");
                        } else {
                            for (String s : clients) {
                                String[] tmp = s.split(":::");
                                System.out.println("ID      : " + tmp[0]);
                                System.out.println("Pseudo  : " + tmp[1]);
                                System.out.println("Adresse : " + tmp[2]);
                                if (Integer.parseInt(tmp[3]) != 0)
                                    System.out.println("Port    : " + tmp[3]);

                            }
                        }
                    } else if (clientRequest.equals("CONNECT_TO")) {

                        System.out.println("Connection established with another client !! ");
                        Socket clientSoc = new Socket(tab[1], Integer.parseInt(tab[2]));
                        System.out.println("in adr : " + clientSoc.getInetAddress() + ", port : " + clientSoc.getPort());
                        String env_msg;
                        DataOutputStream outToClient = new DataOutputStream(clientSoc.getOutputStream());
                        outToClient.writeBytes("HELLO:::"+pseudo+"\n");
                        outToClient.flush();
                        ThreadReaderC x = new ThreadReaderC(clientSoc,pseudo);
                        x.start();
                        while (true) {
                            System.out.print(pseudo+" : ");
                            Scanner s = new Scanner(System.in);
                            env_msg = s.nextLine();
                            if (clientSoc.isClosed()) {
                                System.out.println("the communication is over");
                                break;
                            }
                            if(env_msg.toUpperCase().equals("CLOSE")){
                                outToClient.writeBytes("CLOSE\n");
                            } else {
                                outToClient.writeBytes("WHISP:::"+env_msg + "\n");
                            }
                            outToClient.flush();
                            if (env_msg.toUpperCase().equals("CLOSE")) break;
                        }
                        outToClient.close();
                        clientSoc.close();
                    }
                    System.out.print("\n==> ");
                }
            }

            System.out.println("Connexion closed with server !");
            // msg du client au service pour indiquer la deconnexion
            outToServer.writeBytes("EXIT" + '\n');
            outToServer.flush();
            clientSV.close();
            clientSocket.close();
            inFromServer.close();
            outToServer.close();

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }


    public static String getMsgToSend(String currStr) {

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
                System.out.print("\t Valeur : ");
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
                String ip, port;
                sc2 = new Scanner(System.in);
                System.out.print("\tAdress : ");
                ip = sc2.nextLine();
                System.out.print("\t Port  : ");
                port = sc2.nextLine();
                return "CONNECT_TO:::" + ip + ":::" + port;

            case "OK":
                return "OK";


            default:
                break;

        }

        return "";
    }
}
