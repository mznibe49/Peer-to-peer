import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class ThreadActivity extends Thread {

    Socket connectionSocket;
    Compte client;
    //int idClient;

    public ThreadActivity(Socket connectionSocket/*, int incrementedIdClient*/){
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {


        String msg1 = "Welcome  !:::";
        String msg2 = "Liste des commandes ::::";
        String msg3 = "\tGET_ANN : pour voir tout les annonces :::";
        String msg4 = "\tGET_ANN_BY (prix/domaine --> key) (value) : filtrer les annonces:::";
        String msg5 = "\tPOST : ajouter une annonce:::";
        String msg6 = "\tREMOVE_BY (id) : supprimer une de ses annonces:::";
        String msg7 = "\tREMOVE_ALL : supprimer tout ses annonces:::";
        String msg8 = "\tGET_CLIENTS : voir tout les clients connectés au serveur:::";
        String msg9 = "\tGET_CLIENTS_BY :(pseudo/adr etc)(value) : filtrer les clients:::";
        String msg10 = "\tEXIT : pour se deconnecter:::\n";
        String rec_msg = "";
        String loginMsg = "Welcome ! Pliz Sign(in/up) using \"Pseudo PasseWord\" \n ";

        try {
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            int portClient = 0;
            InetAddress adr = connectionSocket.getInetAddress();
            outToClient.writeBytes(loginMsg); // envoie de message

            System.out.println("HI");
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            //Compte c;

            while(true){
                rec_msg = inFromClient.readLine();
                String [] tab = rec_msg.split(":::");
                String cr = tab[0];

                boolean allGood = false;
                switch (cr) {
                    case "SIGNUP" :
                        System.out.println("\nUn client demande de creer un compte !");
                        String res1="";
                        if(!verifExistAcc(Server.cli_list,tab[1])){

                            res1="SUCCESS:::Account has been created ! \n";
                            System.out.println("Success !");
                            Compte c = new Compte(adr,tab[1],tab[2]);
                            client = c;
                            Server.cli_list.add(c);

                            allGood=true;

                        }else{
                            res1="ERROR:::Existing Account try to connect ! \n";
                        }
                        outToClient.writeBytes(res1);
                        outToClient.flush();
                        break;
                    case "SIGNIN" :
                        System.out.println("\nUn client demande de ce connecter !");
                        res1="";
                        if(verifExistAcc(Server.cli_list,tab[1])){
                            if(verifPwd(Server.cli_list,tab[1],tab[2])){
                                if(!verifDejaCo(Server.cli_list,tab[1],portClient)) {
                                    res1 = "SUCCESS:::Connected ! \n";
                                    allGood = true;
                                    client =  new Compte(adr,tab[1],tab[2]);
                                }else{
                                    System.out.println("cl "+tab[1]);
                                    res1="ERROR:::You're Already Connected ! \n";
                                }
                            }else{
                                res1="ERROR:::Wrong Password try again ! \n";
                            }
                        }else{
                            res1="ERROR:::Account doesn't exist try to create new one ! \n";
                        }
                        outToClient.writeBytes(res1);
                        outToClient.flush();
                        break;
                    default:
                        System.out.println("Please try to SignUp or SignIn");
                }
                if(allGood){
                    break;
                }

            }
            outToClient.writeBytes(msg1+msg2+msg3+msg4+msg5+msg6+msg7+msg8+msg9+msg10); // envoie de message
            outToClient.flush();

            rec_msg = inFromClient.readLine();
            String [] tmp = rec_msg.split(":::");
            client.setPortClient(Integer.parseInt(tmp[1]));
            for(Compte m : Server.cli_list){
                if(m.getPseudo().equals(client.getPseudo())){
                    m.setPortClient(client.getPortClient());
                }
            }



            // lec server

            while(!(rec_msg = inFromClient.readLine()).equals("EXIT")){
                // tant qu'il deco pas on lit etc.. ff chu blasé il est 9h13 j ai la flm d aller en cours de spec
                // port du client
                String tab[] = rec_msg.split(":::");
                String clientRequest = tab[0];
                switch (clientRequest){
                    case "POST" :

                        Annonce a = new Annonce(tab[1],Integer.parseInt(tab[2]),tab[3],client.getPseudo());
                        Server.ann_list.add(a);
                        System.out.println("\nUne nouvelle annonce ajouté par le client : "+client.getPseudo());
                        System.out.println("\tDomaine : "+tab[1]);
                        System.out.println("\tPrix : "+tab[2]);
                        System.out.println("\tDescription : "+tab[3]);
                        outToClient.writeBytes("SUCCESS\n"); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;

                    case "GET_ANN":

                        System.out.println("\nLe client  : "+client.getPseudo()+" demande l'affichage de tous les annonces");
                        String res = makeMsgAnn(Server.ann_list);
                        outToClient.writeBytes(res); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;
                    case "GET_CLIENTS":

                        System.out.println("\nLe client  : "+client.getPseudo()+" demande l'affichage des clients connectés");
                        res = makeMsgCli(Server.cli_list);
                        outToClient.writeBytes(res); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;

                    case "GET_ANN_BY":
                        System.out.println("\nLe client  : "+client.getPseudo()+" demande l'affichage d'une annonce "+tab[0]+tab[1]+tab[2]);
                        String res1 = makeMsgAnnBy(Server.ann_list,tab[1].toLowerCase(),tab[2].toLowerCase());
                        outToClient.writeBytes(res1); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;

                    case "GET_CLIENTS_BY":
                        System.out.println("\nLe client  : "+client.getPseudo()+" demande l'affichage d'un client "+tab[0]+tab[1]+tab[2]);
                        res1 = makeMsgCliBy(Server.cli_list,tab[1].toLowerCase(),tab[2].toLowerCase());
                        outToClient.writeBytes(res1); // envoie de l'ensemble des annonces a partir de la list
                        outToClient.flush();
                        break;

                    case "REMOVE_ALL" :
                        System.out.println("\nLe client  : "+client.getPseudo()+" demande de supprimer tous ses annonces");
                        removeAllFromList(client.getPseudo());
                        break;

                    case "REMOVE_BY" :
                        System.out.println("\nLe client  : "+client.getPseudo()+" demande de supprimer l'annonce numero "+tab[2]);
                        boolean verif = isMyAnnounce(client.getPseudo(),Integer.parseInt(tab[2]));
                        res1 = makeMsgRemoveBy(verif);
                        outToClient.writeBytes(res1);
                        outToClient.flush();
                        break;

                    default:
                        System.out.println("R.I.P");

                }
            }

            resetClientPort(Server.cli_list,client.getPseudo());
            inFromClient.close();
        } catch (Exception exp){
            exp.printStackTrace();
        }
    }

    private boolean verifExistAcc(ArrayList<Compte> cli_list,String pseudo){
        for( Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)) {
                return true;
            }
        }
        return false;

    }

    public void resetClientPort(ArrayList<Compte> list,String pseudo){
        for( Compte c: list){
            if(c.getPseudo().equals(pseudo)){
                System.out.println(" Le client "+pseudo+" has been reset !");
                c.setPortClient(0);
            }
        }
    }

    private boolean verifDejaCo(ArrayList<Compte> cli_list,String pseudo,int port){
        for( Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)){
                if(c.getPortClient()!=0) {
                    return true;
                }else{
                    c.setPortClient(port);
                    return false;
                }
            }
        }
        return false;

    }

    private boolean verifPwd(ArrayList<Compte> cli_list,String pseudo,String pwd){
        for( Compte c : cli_list) {
            if (c.getPseudo().equals(pseudo)){
                if(c.getPassW().equals(pwd)) {
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;

    }

    private String makeMsgRemoveBy(boolean verif) {
        String s1 = "Votre annonce a bien ete supprime"+"\n";
        String s2 = "Cette annonce ne vous appartient pas ou n'existe pas !"+"\n";
        return verif ? s1 : s2;
    }

    private boolean isMyAnnounce(String pseudo, int idAnnonce) {
        Iterator<Annonce> it = Server.ann_list.iterator();
        while (it.hasNext()){
            Annonce a = it.next();
            if(a.getPseudo() == pseudo && a.getAnnonceId() == idAnnonce){
                it.remove();
                return true;
            }
        }
        return false;
    }

    private void removeAllFromList(String pseudo) {
        Iterator<Annonce> it = Server.ann_list.iterator();
        while (it.hasNext()){
            if(it.next().getPseudo() == pseudo){
                it.remove();
            }
        }
    }

    public String makeMsgAnn(ArrayList<Annonce> list){
        String res="";
        for(Annonce a : list){
            res += a.getAnnonceId()+":::"+a.getDomaine()+":::"+a.getPrix()+":::"+a.getDescriptif()+":::"+a.getPseudo()+"&&&"; // pour differencier
        }
        res+="\n";
        return res;
    }

    public String makeMsgCli(ArrayList <Compte> list){
        String res="";
        for(Compte a : list){
            if(a.getPseudo()!=client.getPseudo()){
                res += a.getId()+":::"+a.getPseudo()+":::"+a.getAdress()+":::"+a.getPortClient()+"&&&"; // pour differencier
            }
        }
        res+="\n";
        return res;
    }

    public String makeMsgAnnBy(ArrayList<Annonce> list,String byWhat,String contentOfWhat){
        String res="";
        switch (byWhat){
            case "id":
                for(Annonce a : list){
                    if(a.getAnnonceId() == Integer.parseInt(contentOfWhat)){
                        res += a.getAnnonceId()+":::"+a.getDomaine()+":::"+a.getPrix()+":::"+a.getDescriptif()+":::"+a.getPseudo()+"&&&";
                    }// pour differencier
                }
                break;
            case "domaine":
                for(Annonce a : list){
                    if(a.getDomaine().equals(contentOfWhat)){
                        res += a.getAnnonceId()+":::"+a.getDomaine()+":::"+a.getPrix()+":::"+a.getDescriptif()+":::"+a.getPseudo()+"&&&";
                    }// pour differencier
                }
                break;
            case "prix":
                for(Annonce a : list){
                    if(a.getPrix() == Integer.parseInt(contentOfWhat)){
                        res += a.getAnnonceId()+":::"+a.getDomaine()+":::"+a.getPrix()+":::"+a.getDescriptif()+":::"+a.getPseudo()+"&&&";
                    }// pour differencier
                }
                break;
            default:
                System.out.println("R.I.P");
        }

        res+="\n";
        return res;
    }

    public String makeMsgCliBy(ArrayList<Compte> list,String byWhat,String contentOfWhat) throws UnknownHostException {
        String res="";
        switch (byWhat){
            case "id":
                for(Compte a : list){
                    if(a.getId() == Integer.parseInt(contentOfWhat)){
                        res += a.getId()+":::"+a.getPseudo()+":::"+a.getAdress()+":::"+a.getPortClient()+"&&&";
                    }// pour differencier
                }
                break;
            case "pseudo":
                for(Compte a : list){
                    if(a.getPseudo().equals(contentOfWhat)){
                        res += a.getId()+":::"+a.getPseudo()+":::"+a.getAdress()+":::"+a.getPortClient()+"&&&";
                    }// pour differencier
                }
                break;
            case "adress":
                for(Compte a : list){
                    if(a.getAdress() == InetAddress.getByName(contentOfWhat)){
                        res += a.getId()+":::"+a.getPseudo()+":::"+a.getAdress()+":::"+a.getPortClient()+"&&&";
                    }// pour differencier
                }
                break;
            case "port":
                for(Compte a : list){
                    if(a.getPortClient() == Integer.parseInt(contentOfWhat)){
                        res += a.getId()+":::"+a.getPseudo()+":::"+a.getAdress()+":::"+a.getPortClient()+"&&&";
                    }// pour differencier
                }
                break;
            default:
                System.out.println("R.I.P");
        }

        res+="\n";
        return res;
    }

    public void notifyClient(ArrayList<Compte>list, String pseudo) throws IOException {
        for(Compte c : list){
            if(c.getPseudo().equals(pseudo)){
                Socket s= new Socket(c.getAdress(), c.getPortClient());
                DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                outToClient.writeBytes(""); // envoie de message
            }
        }
    }
}
