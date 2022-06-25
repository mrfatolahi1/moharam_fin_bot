package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Loader {
    public static User loadUser(long userId) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        return objectMapper.readValue(new File("Database/Users/"+String.valueOf(userId)+".json"), User.class);
    }

    public static User loadUser(String username) throws IOException {
        int userId = loadIdWithUsername(username);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        return objectMapper.readValue(new File("Database/Users/"+String.valueOf(userId)+".json"), User.class);
    }

    private static int loadIdWithUsername(String userName) throws IOException, IOException {
        File file = new File("Database/Usernames/"+userName+".json");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        return objectMapper.readValue(file, int.class);
    }

    public static ArrayList<User> loadAllUsers(){
        ArrayList<User> users= new ArrayList<>();

        File usernamesFolder = new File("Database/Usernames");

        for (File usernameFile : usernamesFolder.listFiles()){
            try {
                users.add(loadUser(usernameFile.getName().substring(0, usernameFile.getName().length()-5)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return users;
    }

}
