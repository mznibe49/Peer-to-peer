import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class ThreadActivity extends Thread {

    Socket connectionSocket;
    Compte client;
    //int idClient;

    public ThreadActivity(Socket connectionSocket/*, int incrementedIdClient*/) {
        this.connectionSocket = connectionSocket;
    }


    public String getIpFromLocal() {
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

    @Override
    public void run() {


        String rec_msg = "";

        String loginMsg = "Welcome !\n";

        try {
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            int portClient = 0;
            String s = connectionSocket.getRemoteSocketAddress().toString().split(":")[0].substring(1);
            try {
                InetAddress Ip = InetAddress.getLocalHost();
                if(s.equals("127.0.0.1")){
                    s = getIpFromLocal();

                }
            } catch (Exception e){
                e.printStackTrace();
            }
            String adr = s;
            outToClient.writeBytes(loginMsg); // envoie de message

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            while (true) {
                rec_msg = inFromClient.readLine();
                //if(rec_msg == null) break;
                String[] tab = rec_msg.split(":::");
                String cr = tab[0];
                boolean allGood = false;
                switch (cr) {
                    case "SIGNUP":
                        System.out.println("\nUn client demande de creer un compte !");
                        String res1 = "";
                        if (!verifExistAcc(Server.cli_list, tab[1])) {

                            res1 = "SUCCESS:::Account has been created ! \n";
                            Compte c = new Compte(adr, tab[1], tab[2]);
                            client = c;
                            Server.cli_list.add(c);

                            allGood = true;

                        } else {
                            res1 = "ERROR:::Existing Account try to connect ! \n";
                        }
                        outToClient.writeBytes(res1);
                        outToClient.flush();
                        break;
                    case "SIGNIN":
                        System.out.println("\nUn client demande de ce connecter !");
                        res1 = "";
                        if (verifExistAcc(Server.cli_list, tab[1])) {
                            if (verifPwd(Server.cli_list, tab[1], tab[2])) {
                                if (!verifDejaCo(Server.cli_list, tab[1], portClient)) {
                                    res1 = "SUCCESS:::Connected ! \n";
                                    allGood = true;
                                    client = new Compte(adr, tab[1], tab[2]);
                                } else {
                                    System.out.println("cl " + tab[1]);
                                    res1 = "ERROR:::You're Already Connected ! \n";
                                }
                            } else {
                                res1 = "ERROR:::Wrong Password try again ! \n";
                            }
                        } else {
                            res1 = "ERROR:::Account doesn't exist try to create new one ! \n";
                        }
                        outToClient.writeBytes(res1);
                        outToClient.flush();
                        break;
                    default:

                }
                if (allGood) {
                    break;
                }

            }

            rec_msg = inFromClient.readLine();
            String[] tmp = rec_msg.split(":::");
            client.setPortClient(Integer.parseInt(tmp[1]));
            for (Compte m : Server.cli_list) {
                if (m.getPseudo().equals(client.getPseudo())) {
                    m.setPortClient(client.getPortClient());
                }
            }


            while (true) {
                rec_msg = inFromClient.readLine();
                if(rec_msg == null){
                    rec_msg = "EXIT";
                    System.out.println("Un clinet s'est deconnecté d'une maniere inapproprié");
                }
                boolean b = rec_msg.equals("EXIT");
                if(b) break;
                // tant qu'il deco pas on lit etc.. ff chu blasé il est 9h13 j ai la flm d aller en cours de spec
                // port du client
                String tab[] = rec_msg.split(":::");
                String clientRequest = tab[0];
                switch (clientRequest) {
                    case "POST":
                        Annonce a = new Annonce(tab[1], Integer.parseInt(tab[2]), tab[3], client.getPseudo());
                        Server.ann_list.add(a);
                        System.out.println("\nUne nouvelle annonce ajouté par le client : " + client.getPseudo());
                        System.out.println("\tDomaine : " + tab[1]);
                        System.out.println("\tPrix : " + tab[2]);
                        System.out.println("\tDescription : " + tab[3]);
                        outToClient.writeBytes("SUCCESS\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;

                    case "GET_ANN":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande l'affichage de tous les annonces");
                        int nb_msg = Server.ann_list.size();
                        outToClient.writeBytes("GET_ANN:::"+nb_msg+"\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        int k = 0;
                        while(k<nb_msg){
                            System.out.println("serv envoie : "+Server.ann_list.get(k));
                            String res = makeMsgAnn(Server.ann_list,k);
                            outToClient.writeBytes(res); // envoie de l'ensemble des annonces a partir de la list
                            outToClient.flush();
                            k++;
                        }
                        break;

                    case "GET_CLIENTS":
                        String res;
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande l'affichage des clients connectés");
                        nb_msg = Server.cli_list.size();
                        outToClient.writeBytes("GET_CLIENTS:::"+nb_msg+"\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        k = 0;
                        while(k<nb_msg){
                            System.out.println("serv envoie : "+Server.cli_list.get(k));
                            res = makeMsgCli(Server.cli_list,k);
                            outToClient.writeBytes(res); // envoie de l'ensemble des annonces a partir de la list
                            outToClient.flush();
                            k++;
                        }
                        break;

                    case "GET_ANN_BY":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande l'affichage d'une annonce " + tab[0] + tab[1] + tab[2]);
                        ArrayList<String> res1 = makeMsgAnnBy(Server.ann_list, tab[1].toLowerCase(), tab[2].toLowerCase());
                        outToClient.writeBytes("GET_ANN_BY:::"+res1.size()+"\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        k = 0;
                        while(k<res1.size()){
                            outToClient.writeBytes(res1.get(k));
                            outToClient.flush();
                            k++;
                        }
                        break;

                    case "GET_CLIENTS_BY":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande l'affichage d'un client " + tab[0] + tab[1] + tab[2]);
                        res1 = makeMsgCliBy(Server.cli_list, tab[1].toLowerCase(), tab[2]);
                        outToClient.writeBytes("GET_CLIENTS_BY:::"+res1.size()+"\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        k = 0;
                        while(k<res1.size()){
                            outToClient.writeBytes(res1.get(k));
                            outToClient.flush();
                            k++;
                        }
                        break;

                    case "REMOVE_ALL":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande de supprimer tous ses annonces");
                        removeAllFromList(client.getPseudo());
                        break;

                    case "REMOVE_BY":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande de supprimer l'annonce numero " + tab[2]);
                        boolean verif = isMyAnnounce(client.getPseudo(), Integer.parseInt(tab[2]));
                        String r = makeMsgRemoveBy(verif);
                        outToClient.writeBytes(r);
                        outToClient.flush();
                        break;
                    case "PORT":
                        System.out.println("\nLe client  : " + client.getPseudo() + " demande de changer son port d'ecoute ");
                        for (Compte bo: Server.cli_list) {
                            if(bo.getPseudo().equals(client.getPseudo())){
                                bo.setPortClient(Integer.parseInt(tab[1]));
                            }
                        }
                        break;
                    default:
                        System.out.println("R.I.P");

                }
            }

            resetClientPort(Server.cli_list, client.getPseudo());
            inFromClient.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private boolean verifExistAcc(ArrayList<Compte> cli_list, String pseudo) {
        for (Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)) {
                return true;
            }
        }
        return false;

    }

    public void resetClientPort(ArrayList<Compte> list, String pseudo) {
        for (Compte c : list) {
            if (c.getPseudo().equals(pseudo)) {
                System.out.println(" Le client " + pseudo + " has been reset !");
                c.setPortClient(0);
            }
        }
    }

    private boolean verifDejaCo(ArrayList<Compte> cli_list, String pseudo, int port) {
        for (Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)) {
                if (c.getPortClient() != 0) {
                    return true;
                } else {
                    c.setPortClient(port);
                    return false;
                }
            }
        }
        return false;

    }

    private boolean verifPwd(ArrayList<Compte> cli_list, String pseudo, String pwd) {
        for (Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)) {
                if (c.getPassW().equals(pwd)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;

    }

    private String makeMsgRemoveBy(boolean verif) {
        String s1 = "SUCCESS" + "\n";
        String s2 = "ERROR" + "\n";
        return verif ? s1 : s2;
    }

    private boolean isMyAnnounce(String pseudo, int idAnnonce) {
        Iterator<Annonce> it = Server.ann_list.iterator();
        while (it.hasNext()) {
            Annonce a = it.next();
            if (a.getPseudo() == pseudo && a.getAnnonceId() == idAnnonce) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    private void removeAllFromList(String pseudo) {
        Iterator<Annonce> it = Server.ann_list.iterator();
        while (it.hasNext()) {
            if (it.next().getPseudo() == pseudo) {
                it.remove();
            }
        }
    }

    public String makeMsgAnn(ArrayList<Annonce> list, int index) {
        return list.get(index).getAnnonceId() + ":::" + list.get(index).getDomaine() + ":::" + list.get(index).getPrix() + ":::" + list.get(index).getDescriptif() + ":::" + list.get(index).getPseudo()+"\n"; // pour differencier
    }

    public String makeMsgCli(ArrayList<Compte> list, int index) {
        return list.get(index).getId()+":::"+list.get(index).getPseudo()+":::"+list.get(index).getAdress()+":::"+list.get(index).getPortClient()+"\n";
    }

    public ArrayList<String> makeMsgAnnBy(ArrayList<Annonce> list, String byWhat, String contentOfWhat) {
        String res = "";
        ArrayList<String> tmp = new ArrayList<>();
        switch (byWhat) {
            case "id":
                for (Annonce a : list) {
                    if (a.getAnnonceId() == Integer.parseInt(contentOfWhat)) {
                        res = a.getAnnonceId() + ":::" + a.getDomaine() + ":::" + a.getPrix() + ":::" + a.getDescriptif() + ":::" + a.getPseudo() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            case "domaine":
                for (Annonce a : list) {
                    if (a.getDomaine().equals(contentOfWhat)) {
                        res = a.getAnnonceId() + ":::" + a.getDomaine() + ":::" + a.getPrix() + ":::" + a.getDescriptif() + ":::" + a.getPseudo() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            case "prix":
                for (Annonce a : list) {
                    if (a.getPrix() == Integer.parseInt(contentOfWhat)) {
                        res = a.getAnnonceId() + ":::" + a.getDomaine() + ":::" + a.getPrix() + ":::" + a.getDescriptif() + ":::" + a.getPseudo() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            default:
                System.out.println("R.I.P");
        }

        return tmp;
    }

    public ArrayList<String> makeMsgCliBy(ArrayList<Compte> list, String byWhat, String contentOfWhat) throws UnknownHostException {
        String res = "";
        ArrayList<String> tmp = new ArrayList<>();
        switch (byWhat) {
            case "id":
                for (Compte a : list) {
                    if (a.getId() == Integer.parseInt(contentOfWhat)) {
                        res = a.getId() + ":::" + a.getPseudo() + ":::" + a.getAdress() + ":::" + a.getPortClient() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            case "pseudo":
                for (Compte a : list) {
                    if (a.getPseudo().equals(contentOfWhat)) {
                        res = a.getId() + ":::" + a.getPseudo() + ":::" + a.getAdress() + ":::" + a.getPortClient() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            case "adress":
                for (Compte a : list) {
                    if (a.getAdress().equals(contentOfWhat)/* InetAddress.getByName(contentOfWhat)*/) {
                        res = a.getId() + ":::" + a.getPseudo() + ":::" + a.getAdress() + ":::" + a.getPortClient() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            case "port":
                for (Compte a : list) {
                    if (a.getPortClient() == Integer.parseInt(contentOfWhat)) {
                        res = a.getId() + ":::" + a.getPseudo() + ":::" + a.getAdress() + ":::" + a.getPortClient() + "\n";
                        tmp.add(res);
                    }// pour differencier
                }
                break;
            default:
                System.out.println("R.I.P");
        }

        //res += "\n";
        return tmp;
    }

    public void notifyClient(ArrayList<Compte> list, String pseudo) throws IOException {
        for (Compte c : list) {
            if (c.getPseudo().equals(pseudo)) {
                Socket s = new Socket(c.getAdress(), c.getPortClient());
                DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                outToClient.writeBytes(""); // envoie de message
            }
        }
    }
}
