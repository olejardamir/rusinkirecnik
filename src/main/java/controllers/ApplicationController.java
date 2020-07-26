
package controllers;

import ninja.Context;
import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;
import ninja.params.PathParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Singleton
public class ApplicationController {

    public Result index() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/java/assets/misc/lastPosition.txt"));
        String l = reader.readLine();
        reader.close();
        String[] sp = l.split(" ");
        return Results.redirect("/"+sp[0]+"/"+sp[1]);
    }
    

    public static Result display(
            @PathParam("section") Integer section,
            @PathParam("line") Integer line
    ) throws IOException {

        String row = getLine(section, line);
        if(row==null){
            return Results.text().render("NE POSTOJI... FAJL: "+section+" RED: "+line);
        }

        List<RecnikData> dataPojo = extractData(row);

        return Results.html()
                .render("dataPojo",dataPojo)
                .render("line",line)
                .render("section",section)
                ;
    }


    private static List<RecnikData> extractData(String row){
        List<RecnikData> ret = new ArrayList<>();

        row = row.substring(row.indexOf("@@@")+3);
        String data = row.substring(0,row.indexOf("|||"));
        String pattern = row.substring(row.lastIndexOf("|")+1);

        String[] dataArr = data.split(" ");

        for(int t = 0;t<dataArr.length;t++){
            RecnikData recnikData = new RecnikData();
            recnikData.data = dataArr[t];
            recnikData.pattern = pattern.charAt(t)+"";
            recnikData.pivot = t+"";
            ret.add(recnikData);
        }

        return ret;
    }

    private static String getLine(@PathParam("section") Integer section, @PathParam("line") Integer line) throws IOException {
        File f = new File("src/main/java/assets/sections/"+section+".txt");
        if(!f.exists()) return null;

        String l;
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int cnt=0;
        while((l=reader.readLine())!=null){
            if(cnt==line) {reader.close(); return l;}
            cnt++;
        }
        reader.close();
        return null;
    }

    public static Result next(
            Context context) throws IOException {


        Integer section = context.getParameterAsInteger("fsection");
        Integer line = context.getParameterAsInteger("fline");
        String newPattern = getNewPattern(context);

        rememberPosition(section, line);
        String searchStr = "section: "+section+" line: "+line+"@@@";
        System.out.println(searchStr);


        File f = new File("src/main/java/assets/sections/"+section+".txt");
        File tmp = new File("src/main/java/assets/misc/tmp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

        String l;
        BufferedReader reader = new BufferedReader(new FileReader(f));
        while((l=reader.readLine())!=null){
            if(l.contains(searchStr)){
                String newLine = l.substring(0,l.indexOf("|||"))+"|||"+newPattern;
                writer.write(newLine+"\n");
            }
            else{
                writer.write(l+"\n");
            }
        }
        reader.close();
        writer.close();
        f.delete();
        tmp.renameTo(f);


        line++;
        if(line==1000){section++;line=0;}
        return Results.redirect("/"+section+"/"+line);
    }

    private static String getNewPattern(Context context) {
        Map<String, String[]> paths = context.getParameters();
        int cnt=0;
        for(String k:paths.keySet()){
            if(k.startsWith("fname")){cnt++;}
        }

        String newPattern = "";
        for(int t=0;t<cnt;t++){
            newPattern = newPattern+context.getParameter("fname"+t);
        }
        return newPattern;
    }

    private static void rememberPosition(@PathParam("section") Integer section, @PathParam("line") Integer line) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/assets/misc/lastPosition.txt"));
        writer.write(section+" "+line);
        writer.close();
    }

    public static class RecnikData {
        public String data;
        public String pattern;
        public String pivot;

    }
}
