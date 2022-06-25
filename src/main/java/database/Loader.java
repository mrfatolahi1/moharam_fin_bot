package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Transaction;
import models.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Loader {
    public static User loadUser(long userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            return objectMapper.readValue(new File("Database/Users/"+userId+".json"), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Transaction> getUserTransactions(User user){
        ArrayList<Transaction> transactionsList = new ArrayList<>();
        for (int transactionID : user.getTransactionsIDsList()){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            try {
                 transactionsList.add(objectMapper.readValue(new File("Database/Transactions/"+transactionID+".json"), Transaction.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return transactionsList;
    }

//    public static User loadUser(String username){
//        int userId = 0;
//        try {
//            userId = loadIdWithUsername(username);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
//        try {
//            return objectMapper.readValue(new File("Database/Users/"+String.valueOf(userId)+".json"), User.class);
//        } catch (IOException e) {
//            return null;
//        }
//    }

//    private static int loadIdWithUsername(String userName) throws IOException, IOException {
//        File file = new File("Database/Usernames/"+userName+".json");
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
//        return objectMapper.readValue(file, int.class);
//    }
//
//    public static ArrayList<User> loadAllUsers(){
//        ArrayList<User> users= new ArrayList<>();
//
//        File usernamesFolder = new File("Database/Usernames");
//
//        for (File usernameFile : usernamesFolder.listFiles()){
//            users.add(loadUser(usernameFile.getName().substring(0, usernameFile.getName().length()-5)));
//        }
//
//        return users;
//    }

}
