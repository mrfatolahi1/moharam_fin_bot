package database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Transaction;
import models.TransactionType;
import models.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Loader extends IO{

    public synchronized static User loadUser(long userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            return objectMapper.readValue(new File(rootPath + "Users/"+userId+".json"), User.class);
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

    public synchronized static ArrayList<Transaction> loadUserTransactions(User user, boolean verificated){
        ArrayList<Transaction> transactionsList = new ArrayList<>();
        for (int transactionID : user.getTransactionsIDsList()){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            try {
                Transaction transaction = objectMapper.readValue(new File(rootPath + "Transactions/"+transactionID+".json"), Transaction.class);
                if (!transaction.isDeleted()){
                    if (verificated){
                        if (transaction.isVerificated()){
                            transactionsList.add(transaction);
                        }
                    } else {
                        transactionsList.add(transaction);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return transactionsList;
    }

    public synchronized static ArrayList<Transaction> loadAllTransactions(boolean verificated){
        ArrayList<Transaction> transactionsList = new ArrayList<>();
        File directory=new File(rootPath + "Transactions/");
        int fileCount= Objects.requireNonNull(directory.list()).length;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        for (int i = 1 ; i <= fileCount ; i++){
            try {
                Transaction transaction = objectMapper.readValue(new File("Database/Transactions/"+i+".json"), Transaction.class);
                if (!transaction.isDeleted()){
                    if (verificated){
                        if (transaction.isVerificated()){
                            transactionsList.add(transaction);
                        }
                    } else {
                        transactionsList.add(transaction);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return transactionsList;
    }

    public synchronized static ArrayList<Transaction> loadAllTransactions(TransactionType type, boolean verificated){
        ArrayList<Transaction> transactionsList = new ArrayList<>();
        File directory=new File(rootPath + "Transactions/");
        int fileCount= Objects.requireNonNull(directory.list()).length;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        for (int i = 1 ; i <= fileCount ; i++){
            try {
                Transaction transaction = objectMapper.readValue(new File("Database/Transactions/"+i+".json"), Transaction.class);
                if (transaction.getType() == type && !transaction.isDeleted()){
                    if (verificated){
                        if (transaction.isVerificated()){
                            transactionsList.add(transaction);
                        }
                    } else {
                        transactionsList.add(transaction);
                    }
                }
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

    public synchronized static Transaction loadTransaction(int id, boolean verificated){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        Transaction transaction = null;
        try {
            transaction = objectMapper.readValue(new File(rootPath + "Transactions/"+id+".json"), Transaction.class);
            if (!transaction.isDeleted()){
                if (verificated){
                    if (transaction.isVerificated()){
                        return transaction;
                    } else {
                        return null;
                    }
                } else {
                    return transaction;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized static ArrayList<User> loadAllUsers(){
        ArrayList<User> usersList = new ArrayList<>();
        File directory = new File(rootPath + "Users");

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
            if (user.getName() == null){
                continue;
            }
            output = output + user.getName() + ": " + user.getId() + "\n";
        }

        return output;
    }

    public synchronized static String getAdminsIDsListAsString(){
        ArrayList<Long> adminsIDsList = loadAdminsIDsList();
        String output = "لیست مدیران به همراه آیدی‌های عددی آنان:\n\n";

        for (long adminId : adminsIDsList){
            User admin = Loader.loadUser(adminId);
            if (admin.getName() == null){
                continue;
            }
            output = output + admin.getName() + ": " + admin.getId() + "\n";
        }

        return output;
    }

    public static void updateUserFromDatabase(User user){
        User loadedUser = Loader.loadUser(user.getId());

        user.setName(loadedUser.getName());
        user.setUsername(loadedUser.getUsername());
        user.setEmail(loadedUser.getEmail());
        user.setPhoneNumber(loadedUser.getPhoneNumber());
        user.setTransactionsIDsList(loadedUser.getTransactionsIDsList());
    }

    public static ArrayList<Long> loadAdminsIDsList() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            ArrayList<String> list= objectMapper.readValue(new File(rootPath + "Admins.json"), ArrayList.class);
            ArrayList<Long> output = new ArrayList<>();
            for (String str : list){
                output.add(Long.parseLong(str));
            }
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
