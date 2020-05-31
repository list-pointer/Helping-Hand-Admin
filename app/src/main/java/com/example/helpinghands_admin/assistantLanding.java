package com.example.helpinghands_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class assistantLanding extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    RelativeLayout rl_userData,rl_guardianData,rl_userPhoto;
    Button viewLoc;

    TextView userName,userPhone,user_Disability,guardianName,guardianPhone,tv_noRequestsAvailable;
    ImageView profilePhoto;
    ImageView logoutButton;

    String currStation;
    String assistantUID;
    String expectedEndTime;
    String request_id;

    Handler handler = new Handler();
    Runnable refresh;

    Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_landing);

        fAuth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();

        assistantUID=fAuth.getCurrentUser().getUid();

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation=globalVariable.getStation();

        rl_userPhoto=(RelativeLayout)findViewById(R.id.assistantlanding_rl_userPhoto);
        rl_userData=(RelativeLayout)findViewById(R.id.assistantlanding_rl_userdata);
        rl_guardianData=(RelativeLayout)findViewById(R.id.assistantlanding_rl_guardiandata);

        viewLoc=(Button)findViewById(R.id.confiramtion_ViewLocationButton_assistantLanding);
        tv_noRequestsAvailable=(TextView)findViewById(R.id.assistantLanding_tv_norequesttext);

        userName=(TextView)findViewById(R.id.confirmation_et_username_assistantLanding);
        userPhone=(TextView)findViewById(R.id.confirmation_et_usernumber_assistantLanding);
        user_Disability=(TextView)findViewById(R.id.confirmation_et_userdisability_assistantLanding);
        guardianName=(TextView)findViewById(R.id.confirmation_et_guardianname_assistantLanding);
        guardianPhone=(TextView)findViewById(R.id.confirmation_et_guardiannumber_assistantLanding);

        profilePhoto=(ImageView)findViewById(R.id.assistantLanding_profile_image);
        logoutButton=(ImageView)findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logoutAssistant();

            }
        });

        refresh = new Runnable() {
            public void run() {

                setValues();

                handler.postDelayed(refresh, 5000);
            }
        };
        handler.post(refresh);
    }

    public void setValues() {

        DocumentReference assistantDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(assistantUID);

        assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                final String clientID=documentSnapshot.getString("current_clientID");
                if(clientID!=null)
                {
                    rl_userPhoto.setVisibility(View.VISIBLE);
                    rl_userData.setVisibility(View.VISIBLE);
                    rl_guardianData.setVisibility(View.VISIBLE);
                    viewLoc.setVisibility(View.VISIBLE);
                    tv_noRequestsAvailable.setVisibility(View.GONE);

                    DocumentReference clientDoc = fstore.collection("users").document(clientID);
                    clientDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            userName.setText(documentSnapshot.getString("user_name"));
                            userPhone.setText(documentSnapshot.getString("user_phone"));
                            user_Disability.setText(documentSnapshot.getString("user_disabilities"));
                            guardianName.setText(documentSnapshot.getString("guardian_name"));
                            guardianPhone.setText(documentSnapshot.getString("guardian_phone1"));
                            latitude = Double.parseDouble(documentSnapshot.getString("request_latitude"));
                            longitude = Double.parseDouble(documentSnapshot.getString("request_longitude"));

                            String downloadUrl=documentSnapshot.getString("user_profileImage");
                            Picasso.get().load(downloadUrl).into(profilePhoto, new Callback() {
                                @Override
                                public void onSuccess() {

//                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                            handler.removeCallbacks(refresh);

                            fstore.collection("Admins")
                                    .document("StationAdmins")
                                    .collection(currStation)
                                    .document("Requests")
                                    .collection("ActiveRequests")
                                    .whereEqualTo("user_id",clientID)
                                    .whereEqualTo("staff_id",assistantUID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            DocumentSnapshot d=queryDocumentSnapshots.getDocuments().get(0);
                                            request_id=d.getId();
                                            expectedEndTime=getDelayedEndTime(d.getString("end_time"));

                                            refresh = new Runnable() {
                                                public void run() {

                                                    checkIfServiceEnded();
                                                    handler.postDelayed(refresh, 20000);
                                                }
                                            };
                                            handler.post(refresh);

                                        }
                                    });
                        }
                    });
                }
                else
                {
                    rl_userPhoto.setVisibility(View.GONE);
                    rl_userData.setVisibility(View.GONE);
                    rl_guardianData.setVisibility(View.GONE);
                    viewLoc.setVisibility(View.GONE);
                    tv_noRequestsAvailable.setVisibility(View.VISIBLE);
//                    Toast.makeText(assistantLanding.this, "No requests available", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void viewLocation(View view) {
        Toast.makeText(this, "View Location clicked", Toast.LENGTH_SHORT).show();
        String uri = "https://www.google.com/maps/search/?api=1&query="+ latitude + "," + longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    void logoutAssistant() {

        DocumentReference assistantDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(assistantUID);

        Map<String,Object> assistant = new HashMap<>();
        assistant.put("isOnline","false");
        assistant.put("counter",0);
        assistantDoc.set(assistant, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                fAuth.signOut();
                handler.removeCallbacks(refresh);
                Intent intent = new Intent(assistantLanding.this,role.class);
                startActivity(intent);

            }
        });
    }

    public void checkIfServiceEnded() {

        DocumentReference assistantDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(assistantUID);

        assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String state=documentSnapshot.getString("isAvailable");
                if(state!=null && state.equals("true"))
                {
                    handler.removeCallbacks(refresh);
                    Intent i = new Intent(assistantLanding.this,assistantLanding.class);
                    startActivity(i);
                }
                else
                {
                    checkEndService();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(assistantLanding.this, "Assistant data not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkEndService() {

        final String currTime=getTime();

        if(currTime.equals(expectedEndTime))
        {

            final DocumentReference requestDoc=fstore.collection("Admins")
                    .document("StationAdmins")
                    .collection(currStation)
                    .document("Requests")
                    .collection("ActiveRequests")
                    .document(request_id);

            requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    DocumentReference userDoc = fstore.collection("users").document(documentSnapshot.getString("user_id"));

                    Map<String,Object> userData = new HashMap<>();
                    userData.put("assistant_id",null);
                    userData.put("request_state","idle");
                    userDoc.set(userData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "User link cleared");
                        }
                    });

                    final DocumentReference assistantDoc=fstore.collection("Admins")
                            .document("StationAdmins")
                            .collection(currStation)
                            .document("Assistants")
                            .collection("StationAssistants")
                            .document(documentSnapshot.getString("staff_id"));

                    assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            long counter=documentSnapshot.getLong("counter")+1;

                            Map<String,Object> assistantData = new HashMap<>();
                            assistantData.put("current_clientID",null);
                            assistantData.put("isAvailable","true");
                            assistantData.put("counter",counter);
                            assistantDoc.set(assistantData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Test", "Assistant link cleared");
                                }
                            });

                        }
                    });

                    DocumentReference deletedRequestsDoc=fstore.collection("Admins")
                            .document("StationAdmins")
                            .collection(currStation)
                            .document("Requests")
                            .collection("CompletedRequests")
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
                            Log.d("Test", "Request moved to Completed Requests");
                        }
                    });

                    requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "Request Deleted");

                            handler.removeCallbacks(refresh);
                            Intent i = new Intent(assistantLanding.this,assistantLanding.class);
                            startActivity(i);
                        }
                    });
                }
            });
        }
    }

    public String getDelayedEndTime(String end_time) {

        String eTime="";
        int hour=Integer.parseInt(end_time.substring(0,2));
        int min=Integer.parseInt(end_time.substring(3,5));
        min=min+2;

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
            eTime=eTime+"0"+hour;
        }
        else
        {
            eTime+=hour;
        }
        eTime=eTime+":";
        if(min<10)
        {
            eTime=eTime+"0"+min;
        }
        else
        {
            eTime+=min;
        }

        return eTime;
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
