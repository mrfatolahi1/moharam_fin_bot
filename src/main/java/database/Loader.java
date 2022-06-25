package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Transaction;
import models.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Loader {
    public synchronized static User loadUser(long userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            return objectMapper.readValue(new File("Database/Users/"+userId+".json"), User.class);
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized static User loadUser(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            return objectMapper.readValue(new File(filePath), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static ArrayList<Transaction> getUserTransactions(User user){
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

    public synchronized static ArrayList<Transaction> loadAllTransactions(){
        ArrayList<Transaction> transactionsList = new ArrayList<>();
        File directory=new File("Database/Transactions/");
        int fileCount= Objects.requireNonNull(directory.list()).length;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        for (int i = 1 ; i <= fileCount ; i++){
            try {
                transactionsList.add(objectMapper.readValue(new File("Database/Transactions/"+i+".json"), Transaction.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return transactionsList;
    }

    public synchronized static ArrayList<Long> getAdminsIDs() {
        try {
            ArrayList<Long> adminsIDsList = new ArrayList<>();
            Scanner scanner = new Scanner(new File("src/main/resources/Admins"));
            while (scanner.hasNextLine()) {
                adminsIDsList.add(Long.parseLong(scanner.nextLine()));
            }

            return adminsIDsList;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
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

    public synchronized static ArrayList<User> loadAllUsers(){
        ArrayList<User> usersList = new ArrayList<>();
        File directory = new File("Database/Users");

        for (String fileName : directory.list()){
            String filePath = "Database/Users/" + fileName;
            usersList.add(loadUser(filePath));
        }

        return usersList;
    }

    public synchronized static String getUsersIDsList(){
        ArrayList<User> usersList = Loader.loadAllUsers();
        String output = "لیست خادمان به همراه آیدی‌های عددی آنان:\n\n";

        for (User user : usersList){
            output = output + user.getName() + ": " + user.getId() + "\n";
        }

        return output;
    }

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
