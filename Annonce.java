public class Annonce {

    private static int classId = 1;
    private int annonceId;
    private int prix;
    private String domaine,pseudo, descriptif;

    public int getAnnonceId() {
        return annonceId;
    }

    public int getPrix() {
        return prix;
    }

    public String getDomaine() {
        return domaine;
    }

    public static void setClassId(int classId) {
        Annonce.classId = classId;
    }


    public String getDescriptif() {
        return descriptif;
    }

    public void setAnnonceId(int id) {
        this.annonceId = id;
    }

    public static int getClassId() {
        return classId;
    }


    public void setPrix(int prix) {
        this.prix = prix;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public void setDescriptif(String descriptif) {
        this.descriptif = descriptif;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPseudo() {
        return pseudo;
    }

    public Annonce(String domaine, int prix, String descriptif, String pseudo) {
        this.annonceId = classId;
        this.prix = prix;
        this.domaine = domaine;
        this.descriptif = descriptif;
        this.pseudo = pseudo;
        classId++;
    }

}
