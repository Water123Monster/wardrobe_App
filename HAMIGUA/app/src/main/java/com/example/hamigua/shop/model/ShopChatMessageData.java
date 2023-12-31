package com.example.hamigua.shop.model;

import java.util.Date;

public class ShopChatMessageData {

    public String senderID, receiverID, message, dateTime, image;
    public Date dateObject;

    public String conversationID, conversationName, conversationImage;

    /*
    public Shop_Chat_Message_Data(String senderID, String receiverID, String message, String dateTime, Date dateObject) {

        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.dateTime = dateTime;
        this.dateObject = dateObject;
    }
    */

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getMessage() {
        return message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getConversationName() {
        return conversationName;
    }
}
