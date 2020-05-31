package com.example.helpinghands_admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class registerAssistant extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseStorage fstorage;
    StorageReference storageReference;

    String uid;

    ImageView profileImage;
    EditText assistantName;
    EditText employeeNumber;
    EditText aadharNumber;

    String assistant_Name;
    String assistant_EmployeeNumber;
    String assistant_aadharNumber;
    String assistant_phone;

    String currStation;


    private static final int GalleryPick=1;
    Uri profileImageUri;
    String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_assistant);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fstorage=FirebaseStorage.getInstance();
        storageReference=fstorage.getReference().child("StaffProfileImages");

        uid=fAuth.getCurrentUser().getUid();

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation=globalVariable.getStation();

        Intent i = getIntent();
        assistant_phone=i.getStringExtra("phone");

        profileImage=(ImageView)findViewById(R.id.assistant_profile_image);
        assistantName = findViewById(R.id.et_assistantName);
        employeeNumber=findViewById(R.id.et_employeeNumber);
        aadharNumber=findViewById(R.id.et_aadharNumber);

    }

    public void submitData(View view){

        assistant_Name=assistantName.getText().toString().trim();
        assistant_EmployeeNumber=employeeNumber.getText().toString().trim();
        assistant_aadharNumber=aadharNumber.getText().toString().trim();

        if(assistant_Name.isEmpty()){
            assistantName.setError("Enter name of assistant.");
            assistantName.requestFocus();
            return;
        }
        if(assistant_EmployeeNumber.isEmpty()){
            employeeNumber.setError("Enter employee number.");
            employeeNumber.requestFocus();
            return;
        }
        if(assistant_aadharNumber.isEmpty()){
            aadharNumber.setError("Enter Aadhar number.");
            aadharNumber.requestFocus();
            return;
        }
        if(assistant_aadharNumber.length()<12){
            aadharNumber.setError("Invalid Aadhar number.");
            aadharNumber.requestFocus();
            return;
        }

        DocumentReference assistantDoc=fStore.collection("Admins")
                .document("StationAdmins")
                .collection(currStation)
                .document("Assistants")
                .collection("StationAssistants")
                .document(uid);

        Map<String,Object> user =new HashMap<>();
        user.put("assistant_name",assistant_Name);
        user.put("assistant_phone",assistant_phone);
        user.put("assistant_employee_number",assistant_EmployeeNumber);
        user.put("assistant_aadhar",assistant_aadharNumber);
        user.put("isOnline","false");
        user.put("isAvailable","true");
        user.put("counter",0);
        user.put("current_clientID",null);

        if(profileImageUri==null)
        {
            user.put("assistant_profileImage",null);
        }

        assistantDoc.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("test", "onSuccess: Assistant Details Saved");
            }
        });

        if(profileImageUri!=null)
        {
            final StorageReference filePath=storageReference.child(uid+".jpg");
            filePath.putFile(profileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            DocumentReference assistantDoc=fStore.collection("Admins")
                                    .document("StationAdmins")
                                    .collection(currStation)
                                    .document("Assistants")
                                    .collection("StationAssistants")
                                    .document(uid);
                            Map<String,Object> user =new HashMap<>();
                            user.put("assistant_profileImage",task.getResult().toString());
                            assistantDoc.set(user,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(),"Changes Saved",Toast.LENGTH_SHORT).show();

//                                    Intent goBack=new Intent(registerAssistant.this,DisplayAssistants.class);
//                                    startActivity(goBack);
                                }
                            });

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(registerAssistant.this, "Image upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void changeProfileImage(View view)
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                Uri resultUri=result.getUri();
                profileImageUri=resultUri;
                profileImage.setImageURI(null);
                profileImage.setImageURI(resultUri);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fAuth.signOut();
    }
}
