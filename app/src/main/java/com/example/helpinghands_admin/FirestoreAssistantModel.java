package com.example.helpinghands_admin;

public class FirestoreAssistantModel {
    private String assistant_name , assistant_employee_number;

    public FirestoreAssistantModel() {
        //empty constructor needed
    }

    public FirestoreAssistantModel(String assistant_name, String assistant_employee_number) {
        this.assistant_name = assistant_name;
        this.assistant_employee_number = assistant_employee_number;
    }

    public String getAssistant_name() {
        return assistant_name;
    }

    public String getAssistant_employee_number() { return assistant_employee_number; }
}
