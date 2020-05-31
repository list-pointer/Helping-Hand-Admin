package com.example.helpinghands_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class registerAssistantNumber extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    private String mVerificationId;

    String currStation;
    String phone;
    TextView label_otp;
    EditText et_otp,et_phone;
    Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_assistant_number);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation=globalVariable.getStation();

        et_phone=(EditText)findViewById(R.id.et_assistantContactNumber);
        label_otp=(TextView)findViewById(R.id.tv_otp_registerAssistantNumber);
        et_otp=(EditText)findViewById(R.id.et_otp_registerAssitantNumber);
        loginButton=(Button)findViewById(R.id.assistantRegistrationLogin);

    }
    public void getOTP_registerAssistantNumber(View view){
        phone = et_phone.getText().toString().trim();
        if(phone.length()<10){
            et_phone.setError("Invalid number");
            et_phone.requestFocus();
            return;
        }else{

            phone=et_phone.getText().toString().trim();
            sendVerificationCode(phone);

            label_otp.setVisibility(View.VISIBLE);
            et_otp.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }


    public void login(View view) {

        String code = et_otp.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            et_otp.setError("Enter valid code");
            et_otp.requestFocus();
            return;
        }

        //verifying the code entered manually
        verifyVerificationCode(code);

    }


    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,                 //phoneNo that is given by user
                60,                             //Timeout Duration
                TimeUnit.SECONDS,                   //Unit of Timeout
                TaskExecutors.MAIN_THREAD,          //Work done on main Thread
                mCallbacks);                       // OnVerificationStateChangedCallbacks
    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                    //Getting the code sent by SMS
                    String code = phoneAuthCredential.getSmsCode();

                    //sometime the code is not detected automatically
                    //in this case the code will be null
                    //so user has to manually enter the code
                    if (code != null) {
                        et_otp.setText(code);
                        //verifying the code
                        verifyVerificationCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(registerAssistantNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("TAG",e.getMessage() );
                }

                //when the code is generated then this method will receive the code.
                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);

                    //storing the verification id that is sent to the user
                    mVerificationId = s;
                }
            };

    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    //used for signing the user
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(registerAssistantNumber.this,
                        new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //verification successful we will start the profile activity

                                    final String uid=fAuth.getCurrentUser().getUid();
                                    fStore.collection("Admins")
                                            .document("StationAdmins")
                                            .collection(currStation)
                                            .document("Assistants")
                                            .collection("StationAssistants")
                                            .document(uid)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.getResult().exists())
                                                    {
                                                        Toast.makeText(registerAssistantNumber.this, "Logged in", Toast.LENGTH_SHORT).show();
                                                        //Redirect to the assistant page

//                                                        Intent intent = new Intent(VerifyPhoneActivity.this, Search.class);
//                                                        intent.putExtra("user_phone",mobile);
//                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                        startActivity(intent);
                                                    }
                                                    else
                                                    {
                                                        Intent intent = new Intent(registerAssistantNumber.this, registerAssistant.class);
                                                        intent.putExtra("phone",phone);

                                                        startActivity(intent);
                                                    }
                                                }
                                            });


                                } else {

                                    //verification unsuccessful.. display an error message

                                    String message = "Something is wrong, we will fix it soon...";

                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        message = "Invalid code entered...";
                                    }
                                    Toast.makeText(registerAssistantNumber.this,message,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

}
