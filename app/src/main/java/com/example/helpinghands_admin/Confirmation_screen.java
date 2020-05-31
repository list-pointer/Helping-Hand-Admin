package com.example.helpinghands_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Confirmation_screen extends AppCompatActivity {

    private FirebaseFirestore fstore = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    TextView txt_name, txt_phone, txt_disability, txt_guardian_name, txt_guardian_phone;
    String name, phone, disabilities, gname, gphone;
    Double latitude, longitude;

    String uid,request_id;

    private String currStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_screen);

        txt_name = findViewById(R.id.confirmation_et_username);
        txt_phone = findViewById(R.id.confirmation_et_usernumber);
        txt_disability = findViewById(R.id.confirmation_et_userdisability);
        txt_guardian_name = findViewById(R.id.confirmation_et_guardianname);
        txt_guardian_phone = findViewById(R.id.confirmation_et_guardiannumber);

        Intent intent = getIntent();
        uid = intent.getStringExtra("id");
        request_id = intent.getStringExtra("request_id");

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation = globalVariable.getStation();

        documentReference = fstore.document("users/"+uid);

        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        name = snapshot.getString("user_name");
                        phone = snapshot.getString("user_phone");
                        disabilities = snapshot.getString("user_disabilities");
                        gname = snapshot.getString("guardian_name");
                        gphone = snapshot.getString("guardian_phone1");
                        latitude = Double.parseDouble(snapshot.getString("request_latitude"));
                        longitude = Double.parseDouble(snapshot.getString("request_longitude"));

                        txt_name.setText(name);
                        txt_phone.setText(phone);
                        txt_disability.setText(disabilities);
                        txt_guardian_name.setText(gname);
                        txt_guardian_phone.setText(gphone);
                    }
                });
    }

    public void viewLocation(View view) {
        Toast.makeText(this, "View Location clicked", Toast.LENGTH_SHORT).show();
        String uri = "https://www.google.com/maps/search/?api=1&query="+ latitude + "," + longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    public void acceptRequest(View view){

        fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .whereEqualTo("isOnline","true")
                .whereEqualTo("isAvailable","true")
                .orderBy("counter", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.getDocuments().isEmpty())
                        {
                            DocumentSnapshot d =queryDocumentSnapshots.getDocuments().get(0);
                            String assistantUID=d.getId();


                            DocumentReference requestStateDoc = fstore.collection("users").document(uid);
                            Map<String,Object> newRequestStatus = new HashMap<>();
                            newRequestStatus.put("assistant_id",assistantUID);
                            newRequestStatus.put("request_state","accepted");
                            requestStateDoc.set(newRequestStatus, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });

                            DocumentReference assistantDoc=fstore.collection("Admins")
                                    .document("StationAdmins")
                                    .collection(currStation)
                                    .document("Assistants")
                                    .collection("StationAssistants")
                                    .document(assistantUID);

                            Map<String,Object> assistantData = new HashMap<>();
                            assistantData.put("current_clientID",uid);
                            assistantData.put("isAvailable","false");
                            assistantDoc.set(assistantData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });


                            DocumentReference requestDoc=fstore.collection("Admins")
                                    .document("StationAdmins")
                                    .collection(currStation)
                                    .document("Requests")
                                    .collection("ActiveRequests")
                                    .document(request_id);

                            String start_time=getTime();

                            Map<String,Object> requestData = new HashMap<>();
                            requestData.put("staff_id",assistantUID);
                            requestData.put("start_time",start_time);
                            requestData.put("end_time",getEndTime(start_time));

                            requestDoc.set(requestData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(Confirmation_screen.this, "Assignment successful", Toast.LENGTH_SHORT).show();

                                    Intent goBack=new Intent(Confirmation_screen.this,DisplayRequests.class);
                                    startActivity(goBack);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                        }
                        else
                        {
                            Toast.makeText(Confirmation_screen.this, "No assistants available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Confirmation_screen.this, "Cannot fetch assistants at the moment", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void rejectRequest() {

        DocumentReference requestStateDoc = fstore.collection("users").document(uid);
        Map<String,Object> newRequestStatus = new HashMap<>();
        newRequestStatus.put("request_state","rejected");
        requestStateDoc.set(newRequestStatus, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

        final DocumentReference requestDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Requests")
                .collection("ActiveRequests")
                .document(request_id);


        requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                DocumentReference deletedRequestsDoc=fstore.collection("Admins")
                        .document("StationAdmins")
                        .collection(currStation)
                        .document("Requests")
                        .collection("RejectedRequests")
                        .document(request_id);


                Map<String,Object> delRequest = new HashMap<>();
                delRequest.put("user_id",documentSnapshot.get("user_id"));
                delRequest.put("request_date",documentSnapshot.get("request_date"));
                delRequest.put("request_time",documentSnapshot.get("request_time"));
                delRequest.put("staff_id",documentSnapshot.get("staff_id"));
                delRequest.put("start_time",documentSnapshot.get("start_time"));
                delRequest.put("end_time",documentSnapshot.get("end_time"));

                deletedRequestsDoc.set(delRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request moved to Rejected Requests");
                    }
                });

                requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request Deleted");
                        Intent i = new Intent(Confirmation_screen.this,DisplayRequests.class);
                        startActivity(i);
                    }
                });
            }
        });
    }

    public void rejectAsUnavailable(){

        DocumentReference requestStateDoc = fstore.collection("users").document(uid);
        Map<String,Object> newRequestStatus = new HashMap<>();
        newRequestStatus.put("request_state","unavailable");
        requestStateDoc.set(newRequestStatus, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

        final DocumentReference requestDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Requests")
                .collection("ActiveRequests")
                .document(request_id);


        requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                DocumentReference deletedRequestsDoc=fstore.collection("Admins")
                        .document("StationAdmins")
                        .collection(currStation)
                        .document("Requests")
                        .collection("RejectedRequests")
                        .document(request_id);


                Map<String,Object> delRequest = new HashMap<>();
                delRequest.put("user_id",documentSnapshot.get("user_id"));
                delRequest.put("request_date",documentSnapshot.get("request_date"));
                delRequest.put("request_time",documentSnapshot.get("request_time"));
                delRequest.put("staff_id",documentSnapshot.get("staff_id"));
                delRequest.put("start_time",documentSnapshot.get("start_time"));
                delRequest.put("end_time",documentSnapshot.get("end_time"));

                deletedRequestsDoc.set(delRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request moved to Rejected Requests");
                    }
                });

                requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request Deleted");
                        Intent i = new Intent(Confirmation_screen.this,DisplayRequests.class);
                        startActivity(i);
                    }
                });
            }
        });

    }

    public String getEndTime(String startTime) {

        String endTime="";

        int hour=Integer.parseInt(startTime.substring(0,2));
        int min=Integer.parseInt(startTime.substring(3,5)) + 20;

        if(min>59)
        {
            min=min%60;
            hour++;
        }
        if(hour>23)
        {
            hour=hour%24;
        }

        if(hour<10)
        {
            endTime=endTime+"0"+hour;
        }
        else
        {
            endTime=endTime+hour;
        }
        endTime=endTime+":";
        if(min<10)
        {
            endTime=endTime+"0"+min;
        }
        else
        {
            endTime=endTime+min;
        }

        return endTime;
    }

    public String getTime() {

        String request_time="";

        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        if(h<10)
        {
            request_time=request_time+"0"+h;
        }
        else
        {
            request_time=request_time+h;
        }

        request_time+=":";

        if(m<10)
        {
            request_time=request_time+"0"+m;
        }
        else
        {
            request_time=request_time+m;
        }

        return request_time;
    }

    public void RejectPressed(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Confirmation_screen.this);
        builder.setTitle("Reject Request");
        builder.setMessage("Please select the reason for rejection of request");
        builder.setCancelable(true);

        builder.setPositiveButton("Invalid Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                rejectRequest();
            }
        }).setNegativeButton("Assistants Unavailable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectAsUnavailable();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
