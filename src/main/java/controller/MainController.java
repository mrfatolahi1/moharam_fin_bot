package controller;

import database.Loader;
import models.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainController extends TelegramLongPollingBot{
    private final ArrayList<Chat> chats;

    private Estate estate;

    public MainController() {
        this.chats = new ArrayList<>();
        this.estate = Estate.MAIN_MENU;
    }

    @Override
    public String getBotUsername() {
        return "moharam_fin_bot";
    }

    @Override
    public String getBotToken() {
        return "5572811468:AAHCDhD8K0v-03LwMLLl2hEj-DBY_w10qxQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
//        if (update.getMessage().getText() == null || update.getMessage().getText() == ""){
//            return;
//        }
        (new Thread(() -> {
            long chatID = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            Chat chat = getChat(chatID);
            if (chat != null){
                System.out.println("online chat");
                chat.handleNewUpdate(update);
            } else
            if (Loader.loadUser(update.getMessage().getFrom().getId()) != null) {
                System.out.println("saved chat");
                Chat newChat = new Chat(MainController.this, chatID, Loader.loadUser(update.getMessage().getFrom().getId()), estate);
                newChat.setEstate(Estate.MAIN_MENU);
                chats.add(newChat);
                newChat.handleNewUpdate(update);
            }else
            {
                System.out.println("new user");
                Chat newChat = new Chat(MainController.this, chatID, new User(update.getMessage().getFrom().getId(), null, username, null, null));
                chats.add(newChat);
                newChat.handleNewUpdate(update);
            }
        })).start();



    }

    public void sendMessageToUser(Object object) throws TelegramApiException {

        if (object.getClass() == SendMessage.class){
            execute((SendMessage) object);
        } else
        if (object.getClass() == SendPhoto.class){
            execute((SendPhoto) object);
        }
    }

    public Estate getEstate() {
        return estate;
    }

    public void setEstate(Estate estate) {
        this.estate = estate;
    }

    public ArrayList<Chat> getChats() {
        return chats;
    }

    public Chat getChat(long chatID){
        for (Chat chat : chats){
            if (chat.getChatID() == chatID){
                return chat;
            }
        }

        return null;
    }

    public Chat getChat(String username){
        for (Chat chat : chats){
            if (Objects.equals(chat.getPerson().getUsername(), username)){
                return chat;
            }
        }

        return null;
    }

    public String downloadPhoto(Update update){
        List<PhotoSize> photos = update.getMessage().getPhoto();

        // We fetch the bigger photo
        PhotoSize photo = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);

        Objects.requireNonNull(photo);

        // We create a GetFile method and set the file_id from the photo
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photo.getFileId());
        try {
            // We execute the method using AbsSender::execute method.
            File file = execute(getFileMethod);
            // We now have the file_path
            System.out.println("file.getFilePath() = " + file.getFilePath());
            System.out.println("downloadFile(file.getFilePath()).getAbsolutePath() = " + downloadFile(file.getFilePath()).getAbsolutePath());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return "";
    }

}
