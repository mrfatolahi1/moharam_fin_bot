package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.Transaction;
import models.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Saver {
    public static void saveUser(User user) {
        try {
            File folder = new File("Database/Users/" + user.getUsername() + ".json");
            if (!folder.exists()) {
                folder.createNewFile();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, user);
            saveUsername(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTransaction(Transaction transaction){
        try {
            File folder = new File("Database/Transactions/" + transaction.getTime().toString() + ".json");
            if (!folder.exists()) {
                folder.createNewFile();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, transaction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveUsername(User user){
        try {
            File folder = new File("Database/Usernames/" + user.getUsername() + ".json");
            if (!folder.exists()) {
                folder.createNewFile();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(fileWriter, user.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

