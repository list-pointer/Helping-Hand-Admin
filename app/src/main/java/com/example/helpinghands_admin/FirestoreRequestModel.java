package com.example.helpinghands_admin;

//public class FirestoreRequestModel {
//    private String user_id, start_time, end_time, request_time, request_date, staff_id;
//    private String item_id;
//}

public class FirestoreRequestModel {
    private String user_id, start_time, end_time, request_time, request_date, staff_id;
    //private String request_time;

    public FirestoreRequestModel() {
        //empty constructor needed
    }

    public FirestoreRequestModel(String user_id, String start_time, String end_time, String request_time, String request_date, String staff_id) {
        this.user_id = user_id;
        this.start_time = start_time;
        this.end_time = end_time;
        this.request_time = request_time;
        this.request_date = request_date;
        this.staff_id = staff_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getRequest_time() {
        return request_time;
    }

    public String getStart_time(){ return start_time;}
    public String getEnd_time(){ return end_time;}
    public String getRequest_date(){ return request_date;}
    public String getStaff_id(){ return staff_id;}
}