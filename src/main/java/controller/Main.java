package controller;

//import org.telegram.telegrambots.meta.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
//        // TODO Initialize Api Context
//        ApiContextInitializer.init();
        // TODO Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        // TODO Register our bot
        try {
            botsApi.registerBot(new MainController());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
