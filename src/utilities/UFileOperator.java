/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ppp
 */
public class UFileOperator{

    private String path;
    
    public UFileOperator(String p){
        path = p;
    }

    //reads file from path and returns as string list (of lines)
    public List<String> readFile(){
        List<String> listS = new ArrayList<>();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(path));
            while ((sCurrentLine = br.readLine()) != null) listS.add(sCurrentLine);
        } 
        catch (IOException e){System.out.println(e);}
        finally {
                try { if (br != null) br.close();}
                catch (IOException ex){}
        }
        return listS;
    }
    //writes file to path
    public void writeFile(List<String> content){
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(path));
            for(int i=0; i<content.size(); i++){
                writer.write(content.get(i));
                if(i<content.size()-1) writer.newLine();
            }
        }
        catch (IOException e){}
        finally
        {
            try{ if ( writer != null) writer.close( ); }
            catch (IOException e){}
        }
    }
}//UFileOperator
