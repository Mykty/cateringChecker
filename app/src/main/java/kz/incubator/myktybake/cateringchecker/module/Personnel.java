package kz.incubator.myktybake.cateringchecker.module;

public class Personnel{
    String info;
    String id_number;
    String card_number;
    String photo;
    String type;
    String time;

    public Personnel(){}

    public Personnel(String info, String id_number, String card_number, String photo, String type){
        this.info = info;
        this.id_number = id_number;
        this.card_number = card_number;
        this.photo = photo;
        this.type = type;
    }

    public Personnel(String info, String id_number, String card_number, String photo, String type, String time){
        this.info = info;
        this.id_number = id_number;
        this.card_number = card_number;
        this.photo = photo;
        this.type = type;
        this.time = time;
    }
    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
