package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Transaction;
import models.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Saver extends IO{

    public synchronized static void saveUser(User user) {
        try {
            File folder = new File(rootPath + "Users/" + user.getId() + ".json");
            if (!folder.exists()) {
                folder.createNewFile();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, user);
            saveUsername(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void saveTransaction(Transaction transaction){
        try {
            File folder = new File(rootPath + "Transactions/" + transaction.getId() + ".json");
            if (!folder.exists()) {
                folder.createNewFile();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, transaction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized static void saveUsername(User user){
        try {
            File folder = new File(rootPath + "Usernames/" + user.getId() + ".json");
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

    public synchronized static void addAdmin(User newAdmin){
        try {
            File folder = new File(rootPath + "Admins.json");
            if (!folder.exists()) {
                folder.createNewFile();
            }
            ArrayList<Long> adminsList = Loader.loadAdminsIDsList();
            adminsList.add(newAdmin.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<String> saveList = new ArrayList<>();
            for (long id : adminsList){
                saveList.add(String.valueOf(id));
            }
            objectMapper.registerModule(new JavaTimeModule());
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, saveList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void removeAdmin(User admin){
        try {
            File folder = new File(rootPath + "Admins.json");
            if (!folder.exists()) {
                folder.createNewFile();
            }
            ArrayList<Long> adminsList = Loader.loadAdminsIDsList();
            adminsList.remove(admin.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<String> saveList = new ArrayList<>();
            for (long id : adminsList){
                saveList.add(String.valueOf(id));
            }
            objectMapper.registerModule(new JavaTimeModule());
            FileWriter fileWriter = new FileWriter(folder);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            objectMapper.writeValue(fileWriter, saveList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

