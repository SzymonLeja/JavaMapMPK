package com.example.app;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ApiConnection {
    private static HttpURLConnection con;
    public static void requestAPI(ArrayList<DataObject> dataList, ArrayList<String> busParams) throws IOException{
        var urlParameters = "";
        for(String param: busParams){
            urlParameters+= "busList[][]="+param+"&";
        }
        var url = "https://mpk.wroc.pl/bus_position";
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        try {
            var myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (var wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }
            StringBuilder content;
            try (var br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String line;
                content = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
            //JSON Parsing by string replace - why? coz I couldn't download json parser lib for some weird reason, so I was like "Fine. I'll do it myself"
            String contentString = content.toString();
            contentString = contentString.replace("[","");
            contentString = contentString.replace("]","");
            contentString = contentString.replace("\r", "");
            contentString = contentString.replace("\n", "");
            contentString = contentString.replace("\"", "");
            contentString = contentString.replaceAll("},","}o");
            String[] dataArray = contentString.split("o");
            if(!contentString.isEmpty() && !contentString.isBlank()){
                for(String response: dataArray){
                    response = response.replace("{","");
                    response = response.replace("}"," ");
                    String[] responseArray = response.split(",");
                    ArrayList<String> params = new ArrayList<String>();
                    for(String reponseData: responseArray){
                        String[] splitData = reponseData.split(":");
                        splitData[1]=splitData[1].replace(" ", "");
                        String param = splitData[1];
                        params.add(param);
                    }
                    DataObject data = new DataObject(params);
                    dataList.add(data);
                }
            }

        } finally {

            con.disconnect();
        }
    }
}
