/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package video2hdd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

/**
 *
 * @author CHOUARD
 */
public class Video2hdd {

    
    //------------------------------------------------------------------------------------------------
    //                                    Main
    //------------------------------------------------------------------------------------------------
    
    public static void main(String[] args) throws MalformedURLException, IOException{
        //the data that will be entered by the user
        String URL = ""; 

        //an instance of the BufferedReader class
        //will be used to read the data
        BufferedReader reader; 

        //specify the reader variable
        //to be a standard input buffer
        reader = new BufferedReader(new InputStreamReader(System.in));

        //ask the user for their name
        System.out.print("Quel vidéo voulez vous télécharger ? ");

        try{
            //read the data entered by the user using 
            //the readLine() method of the BufferedReader class
            //and store the value in the name variable
            URL = reader.readLine(); 

            //print the data entered by the user
            System.out.println("Début du téléchargement pour la vidéo : " + URL);
        } catch (IOException ioe){
            //statement to execute if an input/output exception occurs
            System.out.println("Erreur innatendue.");
        }
        
        Tuto test1 = new Tuto(URL);
        
        //boolean movieOk = getMP4(rtmpURL);
    }
}
