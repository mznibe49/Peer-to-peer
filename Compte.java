import java.net.InetAddress;
import java.util.ArrayList;

public class Compte {

    private static int ClassId =1;
    private int id;
    private int portClient;
    private ArrayList<Annonce> list = new ArrayList<>();
    private String pseudo;
    private String passW;
    private String adress;

    public Compte(String adress, String pseudo, String passW){
        this.id = ClassId;
        this.portClient = 0;
        this.adress = adress;
        this.pseudo = pseudo;
        this.passW = passW;
        ClassId++;
    }

    public int getId() {
        return id;
    }

    public int getPortClient() {
        return portClient;
    }

    public String getPassW() {
        return passW;
    }

    public String getPseudo() {
        return pseudo;
    }

    public ArrayList<Annonce> getList() {
        return list;
    }

    public void setList(ArrayList<Annonce> list) {
        this.list = list;
    }

    public void setPassW(String passW) {
        this.passW = passW;
    }

    public void setPortClient(int portClient) {
        this.portClient = portClient;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
}
