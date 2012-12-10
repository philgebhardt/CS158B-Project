package RMON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import Crypto.Key;
import Structure.User;

public class RMON
{
    static HashMap<String, Alarm> alarms;
    static HashMap<String, User> users;
    static ClientSideComm client;
    
    public static void main(String args[])
    {
        pullConfigInfo();
        alarms = new HashMap<String, Alarm>();
        client = new ClientSideComm(users, alarms);
        client.setDaemon(true);
        client.start();
        while(true);
    }
    
    public static void pullConfigInfo()
    {
        String line;
        users = new HashMap<String, User>();
        FileSystem fs = FileSystems.getDefault();
        Path configFile = fs.getPath("CONFIG.agent");
        
        try
        {
            InputStream in = Files.newInputStream(configFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            line = null;
            while(!(line = reader.readLine()).equals("USERS"));
            while((line = reader.readLine()) != null)
            {
                User user = createUser(line);
                users.put(user.getName(), user);
            }
            
            in.close();
            reader.close();
        }
        catch(IOException e)
        {
            System.err.format("Error reading from CONFIG file: %s%n", e.getMessage());
        }
        catch(NoSuchElementException e)
        {
            System.err.format("Error reading line from CONFIG file: %s%n", e.getMessage());
        }
    }

    public static User createUser(String input)
    {
        StringTokenizer st;
        String name;
        byte[] keyBytes = new byte[16];
        st = new StringTokenizer(input, " ");
        name = st.nextToken();
        for(int i = 0; i < 16; i++) keyBytes[i] = (byte) Integer.parseInt(st.nextToken());
        return new User(name, new Key(keyBytes));
    }
}
