package com.example.helpinghands_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class editAssistantProfile extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    TextView eID,eName,ePhone,eAadhar;
    ImageView ePhoto;

    String currStation;

    String assistantUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assistant_profile);

        Intent intent = getIntent();
        assistantUID = intent.getStringExtra("id");

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation=globalVariable.getStation();

        fAuth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();

        eID=(TextView)findViewById(R.id.editProfile_et_id);
        eName=(TextView)findViewById(R.id.editProfile_et_name);
        ePhone=(TextView)findViewById(R.id.editProfile_et_number);
        eAadhar=(TextView)findViewById(R.id.editProfile_et_aadharNumber);

        ePhoto=(ImageView)findViewById(R.id.editProfile_profile_image);

        setValues();
    }

    public void setValues()
    {
        DocumentReference assistantDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(assistantUID);

        assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eID.setText(documentSnapshot.getString("assistant_employee_number"));
                eName.setText(documentSnapshot.getString("assistant_name"));
                ePhone.setText(documentSnapshot.getString("assistant_phone"));
                eAadhar.setText(documentSnapshot.getString("assistant_aadhar"));

                String downloadUrl=documentSnapshot.getString("assistant_profileImage");
                Picasso.get().load(downloadUrl).into(ePhoto, new Callback() {
                    @Override
                    public void onSuccess() {

//                                    progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });
    }


    public void deleteAssistant(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(editAssistantProfile.this);
        builder.setMessage("Do you wish to delete this Assistant ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                startDeletion();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


//        assistantDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Intent i = new Intent(editAssistantProfile.this,DisplayAssistants.class);
//                startActivity(i);
//            }
//        });
    }

    void startDeletion()
    {
        final DocumentReference assistantDoc=fstore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(assistantUID);


        assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                DocumentReference deletedAssistants = fstore.collection("Admins")
                        .document("StationAdmins")
                        .collection(currStation)
                        .document("Assistants")
                        .collection("DeletedAssistants")
                        .document(assistantUID);

                Map<String,Object> delAssistant = new HashMap<>();
                delAssistant.put("assistant_employee_number",documentSnapshot.get("assistant_employee_number"));
                delAssistant.put("assistant_name",documentSnapshot.get("assistant_name"));
                delAssistant.put("assistant_phone",documentSnapshot.get("assistant_phone"));
                delAssistant.put("assistant_aadhar",documentSnapshot.get("assistant_aadhar"));
                delAssistant.put("assistant_profileImage",documentSnapshot.get("assistant_profileImage"));

                deletedAssistants.set(delAssistant).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Assistant moved to Deleted Assistants");

                        assistantDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Test", "Assistant Deleted");
                                Intent i = new Intent(editAssistantProfile.this,DisplayAssistants.class);
                                startActivity(i);
                            }
                        });
                    }
                });

            }
        });
    }
}
