
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.*;


public class Server {

    //public static int incrementedIdClient = 1;

    public static ArrayList<Annonce> ann_list = new ArrayList<Annonce>();
    public static ArrayList<Compte> cli_list = new ArrayList<Compte>();

    public static void main( String [] args){

        try {
            final ServerSocket welcomeSocket = new ServerSocket(1027);
            Thread thread = new Thread(){
                public void run(){
                    while(true) {
                        Scanner s = new Scanner(System.in);
                        String server_request= s.nextLine().toUpperCase().trim();
                        switch(server_request){
                            case "GET_CLIENTS" :
                                System.out.println("=======================================");
                                System.out.println("===== Liste de clients connectés ======");
                                for(Compte c : cli_list){
                                    if(c.getPortClient()!=0){
                                        System.out.println("Pseudo : "+c.getPseudo());
                                        System.out.println("Adress : "+c.getAdress());
                                        System.out.println("Port   : "+c.getPortClient()+"\n");
                                    }
                                };
                                System.out.println(("======================================="));
                                break;
                            case "GET_ANN" :
                                System.out.println("========================================");
                                System.out.println("=====     Liste des Annonces     =======");
                                for(Annonce a : ann_list){
                                    System.out.println("Id     : "+a.getAnnonceId());
                                    System.out.println("Pseudo : "+a.getPseudo());
                                    System.out.println("Domaine: "+a.getDomaine());
                                    System.out.println("Prix   : "+a.getPrix());
                                    System.out.println("Descr  : "+a.getDescriptif()+"\n");
                                };
                                System.out.println(("======================================="));
                                break;
                            default :    System.out.println("Requête non prise en charge : "+ server_request);break;
                        }
                    }
                }
            };
            thread.start();

            while(true){

                // attente de connexion
                final Socket connectionSocket = welcomeSocket.accept();

                // thread pour gerer les activités des chaque clients
                ThreadActivity tha = new ThreadActivity(connectionSocket);
                tha.start();
            }
        } catch (Exception exp){
            exp.printStackTrace();
        }

    }


}
