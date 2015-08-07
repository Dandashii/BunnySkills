package skillstats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchStats implements Serializable {
    static final long serialVersionUID = 1;
    
    static List<MatchStats> matches = new ArrayList<>();
    static List<Long> matchids = new ArrayList();
    //static long stopatid = 424751321;
    static long stopatid = 0;
    
    
    public String match_id;
    public boolean win;
    public int kills, deaths, assists, gold_per_min, xp_per_min, slot;
    public int[] heroids;
    public String[] playerids;
    public Skill skill = Skill.UNKNOWN;
    public boolean skillchecked=false;
    
    public MatchStats(String matchid, int slot, boolean win, int kills, int deaths, int assists, int gold_per_min, int xp_per_min, int[] heroes, String[] playerids) {
        this.match_id=matchid;
        this.win=win;
        this.slot=slot;
        this.kills=kills;
        this.deaths=deaths;
        this.assists=assists;
        this.gold_per_min=gold_per_min;
        this.heroids=heroes;
        this.playerids=playerids;
        this.xp_per_min=xp_per_min;
        
        matches.add(this);
        matchids.add(Long.parseLong(matchid));
    }
    
    public static void setStopId() {
        if(matchids.size()<=0) {
            return;
        }
        Collections.sort(matchids);
        Collections.reverse(matchids);
        stopatid=matchids.get(0);
    }
    
    static Thread savethread=null;
    
    public static void saveToFile() {
        if(savethread==null) {
            savethread=new Thread() {
                @Override
                public void run(){
                    List<MatchStats> matchclone = matches;
                    List<Long> matchidclone = matchids;
                    
                    File f = new File("mst.dstat");
                    if(!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException ex) {
                            System.out.println("46::IO");
                        }
                    }
                    try {
                        FileOutputStream file = new FileOutputStream("mst.dstat");
                        BufferedOutputStream buffer = new BufferedOutputStream(file);
                        ObjectOutputStream output = new ObjectOutputStream(buffer);
                        output.writeObject(matchclone);
                        output.writeObject(matchidclone);
                        output.close();
                    } catch (IOException ex) {
                        System.out.println("56::IO");
                    }
                    
                    savethread=null;
                }
            };
            savethread.start();
        }
    }
    
    public static void loadFromFile() {
        File f = new File("mst.dstat");
        if(f.exists()) {
            try {
                FileInputStream file = new FileInputStream("mst.dstat");
                BufferedInputStream buffer = new BufferedInputStream(file);
                ObjectInputStream input = new ObjectInputStream(buffer);
                try {
                    matches = (List<MatchStats>)input.readObject();
                    matchids = (List<Long>)input.readObject();
                } catch (ClassNotFoundException ex) {
                    System.out.println("73::IO");
                }
            } catch (IOException ex) {
                System.out.println("76::IO");
            }
        }
    }
    
    
    public void downloadSkill() {
        
        try {
            URL url = new URL("http://dotamax.com/match/detail/"+match_id+"/");
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder a = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                a.append(inputLine);
            }
            in.close();
            
            String source = a.toString();
            if(source.contains("处理中..")) {
                System.out.println("Unknown");
                skill=Skill.UNKNOWN;
                skillchecked=true;
            }
            else if(source.contains("Normal")) {
                System.out.println("Normal");
                skill=Skill.NORMAL;
                skillchecked=true;
            }
            else if(source.contains("Very High")) { // THIS MUST COME BEFORE "HIGH" BECAUSE CONTAINS "HIGH" KKKKK
                System.out.println("Very High");
                skill=Skill.VERY_HIGH;
                skillchecked=true;
            }
            else if(source.contains("High")) {
                System.out.println("High");
                skill=Skill.HIGH;
                skillchecked=true;
            }
        } catch (IOException ex) {
            Logger.getLogger(MatchStats.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public enum Skill {
        NORMAL,
        HIGH,
        VERY_HIGH,
        UNKNOWN
    }
}
