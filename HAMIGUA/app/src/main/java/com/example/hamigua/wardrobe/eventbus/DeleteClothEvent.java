package com.example.hamigua.wardrobe.eventbus;

public class DeleteClothEvent {

    public String deleteSuccessful;
    public String category;
    public int position;

    public DeleteClothEvent(String deleteSuccessful, String category, int position) {
        this.deleteSuccessful = deleteSuccessful;
        this.category = category;
        this.position = position;
    }

    public String getDeleteSuccessful() {
        return deleteSuccessful;
    }

    public String getCategory() {
        return category;
    }

    public int getPosition() {
        return position;
    }
}
