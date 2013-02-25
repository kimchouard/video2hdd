/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package video2hdd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author CHOUARD
 */
public class Tuto {
    

    //------------------------------------------------------------------------------------------------
    //                                    Attributs
    //------------------------------------------------------------------------------------------------
    private static String nom;
    
    private static Document htmlObj;
    private static String rtmpBase;
    private static String baseURL;
    
    private Vector<Cat> vCats;
    private static int nbCat;
    private static int nbVids = 0;

    private static Process rtmp;
    //------------------------------------------------------------------------------------------------
    //                                    Méthodes
    //------------------------------------------------------------------------------------------------
    
    //------------------------------------------------------------------------------------------------
    //                                    Récupération du lien

        //------------------------------------------------------------------------------------------------
        //                                  getBaseURL

        // Retourne le la base du lien http://.
        private String getBaseURL(String aURL) {
            String[] URLs = aURL.split("/");
            return URLs[0]+"/"+URLs[1]+"/"+URLs[2]+"/"+URLs[3]+"/";
        }
        //                              Fin de getBaseURL
        //------------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------------
        //                                  firstURL

        // Retourne le HTML depuis une URL.
        private String firstURL(String aURL) throws IOException {
            Document html = Jsoup.connect(aURL).get();
            html = Jsoup.parse(html.html());
            
            Elements Menu = html.getElementsByClass("toc_list").get(0).getElementsByClass("menu");
            
            for (int i=0; i < Menu.size(); i++ ) {
                Elements actualMenu = Menu.get(i).getElementsByClass("other");
                
                for (int j=0; j < actualMenu.size(); j++ ) {
                    Element actualVid = actualMenu.get(j);
                    if (!actualVid.getElementsByClass("movie-playable").isEmpty()) {
                        String lien = actualVid.getElementsByClass("video-title").html();
                        String[] liens = lien.split("href=\"");
                        lien = liens[1];
                        liens = lien.split("\"");
                        lien = liens[0];
                        return lien;
                    }
                }
            }

            return "";
        }
        //                              Fin de firstURL
        //------------------------------------------------------------------------------------------------


        //------------------------------------------------------------------------------------------------
        //                                  getSource

        // Retourne le HTML depuis une URL.
        private static Document getSource(String aURL) throws IOException {
            Document html = Jsoup.connect(aURL).get();
            html = Jsoup.parse(html.html());

            return html;
        }
        //                              Fin de getSource
        //------------------------------------------------------------------------------------------------
    
        //------------------------------------------------------------------------------------------------
        //                                  getXML

        // Retourne le XML extrait d'un code source HTML.
        private static String getXML(Document aFinalHTML) {
            String xml = "";

            //création de la régex
            Pattern regex = Pattern.compile("configuration: \"(.+?)\"");
            // création de la recherche
            Matcher results = regex.matcher(aFinalHTML.html());

            //Si l'élément est trouvé
            if ((results.find()) && (results.group(1) != "")) {
                xml = results.group(1);
            }

            return xml;
        }
        //                              Fin de getXML
        //------------------------------------------------------------------------------------------------


        //------------------------------------------------------------------------------------------------
        //                                  getRTMP

        // Retourne l'URL du flux RTMP extrait d'un XML.
        private static Elements getRTMP(Document aXML) {
            return aXML.getElementsByTag("src");
        }
        //                              Fin de getRTMP
        //------------------------------------------------------------------------------------------------


        //------------------------------------------------------------------------------------------------
        //                                  getLink

        // Récupère le lien RTMP depuis une url d'une page vidéo
        private static String getLink(Document aHTML) throws IOException {
            String XMLUrl = getXML(aHTML);

            Document XML = getSource(XMLUrl);

            Elements RTMPUrls = getRTMP(XML);
            String URL = RTMPUrls.get(0).html();
            String URLs[] = URL.split("_");
            URL = URLs[0]+"_"+URLs[1]+"_"+URLs[2]+"_";

            System.out.println(URL);

            return URL;
        }
        //                              Fin de getLink
        //------------------------------------------------------------------------------------------------
        
    //                              Fin de la récupération du lien
    //------------------------------------------------------------------------------------------------
    
    
    //------------------------------------------------------------------------------------------------
    //                                  getVids

    // Récupère le le sommaire de la vidéo
    private static String traiterNom(String aNom) {
        String sNom = aNom.replace(":", "-");
        sNom = sNom.replace("?", "");
        sNom = sNom.replace("\\", "");
        sNom = sNom.replace("/", "");
        sNom = sNom.replace("*", "");
        sNom = sNom.replace("<", "");
        sNom = sNom.replace(">", "");
        sNom = sNom.replace("|", "");
        sNom = sNom.replace("strong", "");
        
        return sNom;
    }
    //                              Fin de getLink
    //------------------------------------------------------------------------------------------------
        
    //                              Fin de la récupération du lien
    //------------------------------------------------------------------------------------------------
    
    
    //------------------------------------------------------------------------------------------------
    //                                  getVids

    // Récupère le le sommaire de la vidéo
    private static Vector<Cat> getVids(Document aHTML) {
        
        nom = traiterNom(aHTML.getElementsByClass("product-title").get(0).getAllElements().get(1).html());
        
        Elements sommaireBrut = aHTML.getElementsByClass("toc_list");
        Element sommaire = sommaireBrut.get(0);
        Elements eCats = sommaire.getElementsByClass("menu");
        nbCat = eCats.size();
        Vector<Cat> cats = new Vector(nbCat);
        
        for (int i = 0; i < nbCat; i++) {
            //Création de la catégorie
            Cat tempCat = new Cat();
            tempCat.Nom = traiterNom(StringEscapeUtils.unescapeHtml4(eCats.get(i).getElementsByClass("menu-title").get(0).html()));
            
            //Créations des sous parties
            tempCat.ssCats = new Vector();
            Elements eSsCats = eCats.get(i).getElementsByClass("other");
            tempCat.nbSsCat = eSsCats.size();
            nbVids += tempCat.nbSsCat;
            for (int j = 0; j < tempCat.nbSsCat; j++) {
                String ssTitre = traiterNom(StringEscapeUtils.unescapeHtml4(eSsCats.get(j).getElementsByClass("video-title").get(0).getAllElements().get(1).html())); 
                tempCat.ssCats.addElement(ssTitre);
            }
            
            cats.addElement(tempCat);
        }

        return cats;
    }
    //                              Fin de getVids
    //------------------------------------------------------------------------------------------------

    
    //------------------------------------------------------------------------------------------------
    //                                  getMP4
    
    // Récupère le lien RTMP depuis une url d'une page vidéo
    private static boolean getMP4(String aURL, String aOutput) {
        String cmd = "rtmpdump.exe -r "+aURL+" -o "+aOutput;
        System.out.println(cmd);
        
        try
        {
            Runtime rt = Runtime.getRuntime() ;
            rtmp = rt.exec(cmd);
            
            // Consommation de la sortie standard de l'application externe dans un Thread separe
            new Thread() {
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(rtmp.getInputStream()));
                        
                        String line = "";
                        try {
                            while((line = reader.readLine()) != null) {
                                if (line != "")
                                    System.out.println("RTMPDump : "+line);
                            }
                        } finally {
                            reader.close();
                        }
                    } catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
               }
            }.start();

            // Consommation de la sortie d'erreur de l'application externe dans un Thread separe
            new Thread() {
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(rtmp.getErrorStream()));
                        String line = "";
                        try {
                            while((line = reader.readLine()) != null) {
                                if (line != "")
                                    System.out.println("RTMPDump : "+line);
                            }
                        } finally {
                               reader.close();
                        }
                    } catch(IOException ioe) {
                            ioe.printStackTrace();
                    }
                }
            }.start();
            
            
            rtmp.waitFor();

            rtmp.destroy() ;
            System.out.println("Exécution de la commande terminée. Retourne : "+rtmp.exitValue());
            
            return true;
        }catch(Exception exc){
           System.out.println("Erreur d'execution " + cmd + exc.toString());
           
           return false;
        }
    }
    //                              Fin de getMP4
    //------------------------------------------------------------------------------------------------
    
    
    //------------------------------------------------------------------------------------------------
    //                                  downVids

    // Télécharge les vidéo à partir du fichier de sommaire
    private static boolean downVids(Vector<Cat> avCats) {
        //On crée le dossier de la vidéo
        File fNom = new File(nom);
        if (fNom.mkdirs()) {
            //System.out.println("Ajout du dossier : " + fNom.getAbsolutePath());
        } else {
            //System.out.println("Echec sur le dossier : " + fNom.getAbsolutePath());
        }; 
        
        for (int idCat=1; idCat <= nbCat; idCat++) {
            String sIdCat = "";
            Cat actualCat = avCats.get(idCat-1);
            if (idCat < 10)  sIdCat = "0"+String.valueOf(idCat);
            else sIdCat = String.valueOf(idCat);
            
            //On crée le dossier de la catégorie
            File fCat = new File(nom+"/"+sIdCat+" - "+actualCat.Nom); 
            if (fCat.mkdirs()) {
                //System.out.println("Ajout du dossier : " + fCat.getAbsolutePath());
            } else {
                //System.out.println("Echec sur le dossier : " + fCat.getAbsolutePath());
            }; 
            
            for (int idSsCat = 1; idSsCat <= actualCat.nbSsCat; idSsCat++) {
                String sIdSsCat = "";
                if (idSsCat < 10)  sIdSsCat = "0"+String.valueOf(idSsCat);
                else sIdSsCat = String.valueOf(idSsCat);
                
                String urlActual = rtmpBase + sIdCat + "_" + sIdSsCat+".mp4";
                
                String urlOutput = "\""+fCat.getAbsolutePath()+"\\"+sIdSsCat+" - "+actualCat.ssCats.get(idSsCat-1)+".mp4\"";
                
                if (!getMP4(urlActual, urlOutput)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    //                              Fin de downVids
    //------------------------------------------------------------------------------------------------

    
    
    Tuto(String aURL) throws IOException {
        baseURL = getBaseURL(aURL);
        String vidURL = baseURL + firstURL(aURL);
        
        if (!vidURL.isEmpty()) {
            htmlObj = getSource(vidURL);
            rtmpBase = getLink(htmlObj);

            vCats = getVids(htmlObj);

            System.out.println("Lancement du téléchargement de "+nbVids+" vidéos.");
            if (downVids(vCats)) {
                System.out.println("Téléchargement terminé avec succès MOFO ! Enjoy ;)");
            } else {
                System.out.println("Téléchargement échoué...");
            }
        } else {
            System.out.println("Impossible de récupérer un lien valide...");
        }
    }
}
