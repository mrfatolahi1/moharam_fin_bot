package controller.adminpanel.adddeleteadminspanel;

import controller.adminpanel.AdminPanel;
import database.Loader;
import database.Saver;
import models.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class AddDeleteAdminsPanel {
    private final AdminPanel adminPanel;
    private AddDeleteAdminsPanelEstate estate;

    public AddDeleteAdminsPanel(AdminPanel adminPanel) {
        this.adminPanel = adminPanel;
        this.estate = AddDeleteAdminsPanelEstate.ADD_DELETE_ADMINS_PANEL;
    }

    public void showAddDeleteAdminsPanel(Update update){
        if (update.getMessage().getFrom().getId() != 411069917){
            String messageText = "فقط فتح‌اللهی (@mrfatolahi1) می‌توانید ادمین ها را ویرایش کند :)";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            adminPanel.sendMessageToUser(sendMessage);
            adminPanel.showAdminPanel(update);
            return;
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "یکی از گزینه‌ها را انتخاب کنید:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("افزودن مدیر");
        row1.add("حذف مدیر");
        row2.add("بازگشت به پنل مدیریت");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        estate = AddDeleteAdminsPanelEstate.ADD_DELETE_ADMINS_PANEL;
        adminPanel.sendMessageToUser(sendMessage);
    }

    public void handleNewUpdate(Update update){
        if (estate == AddDeleteAdminsPanelEstate.ADD_DELETE_ADMINS_PANEL){
            handleAddDeleteAdminsPanelRequest(update);
        } else
        if (estate == AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN){
            addNewAdmin(update);
        } else
        if (estate == AddDeleteAdminsPanelEstate.MASTER_IS_REMOVING_AN_ADMIN){
            removeAdmin(update);
        }
    }

    private void handleAddDeleteAdminsPanelRequest(Update update){
        if (update.getMessage().getText().equals("افزودن مدیر")){//
            requestNewAdminID();
        } else
        if (update.getMessage().getText().equals("حذف مدیر")){//
            requestAdminID();
        } else
        if (update.getMessage().getText().equals("بازگشت به پنل مدیریت")){
            showAdminPanel(update);
        } else {
            showAddDeleteAdminsPanel(update);
        }
    }

    private void requestNewAdminID(){
        estate = AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN;
        String messageText1 = Loader.getUsersIDsList();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی مدیر جدید را وارد کنید (با استفاده از لیست بالا):";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void addNewAdmin(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAddDeleteAdminsPanel(update);
                return;
            }
        }

        long longId = 0;
        User newAdmin = null;
        try {
            String newAdminId = update.getMessage().getText();
            longId = Long.parseLong(newAdminId);
            newAdmin = Loader.loadUser(longId);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN;
            sendMessageToUser(sendMessage);
            return;
        }
        if (newAdmin == null){
            String messageText = "کاربر یافت نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN;
            sendMessageToUser(sendMessage);
            return;
        }

        Saver.addAdmin(newAdmin);
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "مدیر جدید با موفقیت افزوده شد.");
        sendMessageToUser(sendMessage);
        showAdminPanel(update);
    }

    private void requestAdminID(){
        estate = AddDeleteAdminsPanelEstate.MASTER_IS_REMOVING_AN_ADMIN;
        String messageText1 = Loader.getAdminsIDsListAsString();
        SendMessage sendMessage1 = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText1);
        adminPanel.sendMessageToUser(sendMessage1);
        String messageText = "آیدی عددی مدیری که می‌خواهید حذف کنید را وارد کنید (با استفاده از لیست بالا):";
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("انصراف");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);
        adminPanel.sendMessageToUser(sendMessage);
    }

    private void removeAdmin(Update update){
        if (update.getMessage().getText() != null){
            if (update.getMessage().getText().equals("انصراف")) {
                showAddDeleteAdminsPanel(update);
                return;
            }
        }

        long longId = 0;
        User admin = null;
        try {
            String newAdminId = update.getMessage().getText();
            longId = Long.parseLong(newAdminId);
            admin = Loader.loadUser(longId);
        } catch (Exception e){
            String messageText = "فرمت اطلاعات وارد شده صحیح نیست، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN;
            sendMessageToUser(sendMessage);
            return;
        }
        if (admin == null){
            String messageText = "کاربر یافت نشد، مجددا تلاش کنید.";
            SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), messageText);
            estate = AddDeleteAdminsPanelEstate.MASTER_IS_ADDING_NEW_ADMIN;
            sendMessageToUser(sendMessage);
            return;
        }

        Saver.removeAdmin(admin);
        SendMessage sendMessage = new SendMessage(String.valueOf(adminPanel.getChat().getChatID()), "مدیر با موفقیت حذف شد.");
        sendMessageToUser(sendMessage);
        showAdminPanel(update);
    }

    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    public AddDeleteAdminsPanelEstate getEstate() {
        return estate;
    }

    public void setEstate(AddDeleteAdminsPanelEstate estate) {
        this.estate = estate;
    }

    private void showAdminPanel(Update update){
        estate = AddDeleteAdminsPanelEstate.ADD_DELETE_ADMINS_PANEL;
        adminPanel.showAdminPanel(update);
    }

    public void sendMessageToUser(Object object){
        adminPanel.sendMessageToUser(object);
    }
}
