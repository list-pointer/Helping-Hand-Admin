package com.example.helpinghands_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntBiFunction;

public class role extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    private String mVerificationId;

    String adminOfStation,loginID,password;
    String mobileNumber,OTP;


    String AIRPORT_ROAD="Airport Road";
    String ANDHERI="Andheri";
    String ASALPHA="Asalpha";
    String AZAD_NAGAR="Azad Nagar";
    String CHAKALA="Chakala";
    String D_N_NAGAR="D N Nagar";
    String JAGRUTI_NAGAR="Jagruti Nagar";
    String MAROL_NAKA="Marol Naka";
    String SAKINAKA="Sakinaka";
    String WEH="WEH";
    String VERSOVA="Versova";
    String GHATKOPAR="Ghatkopar";

    RelativeLayout adminLoginLayout,staffLoginLayout;
    EditText et_loginID,et_password,et_phone,et_otp;
    TextView tv_getOtp, tv_selectStation;
    Button staffLoginBtn,getOtpBtn;
    Spinner station_spinner;
    ProgressBar progressBar;

    private static final String PREFS_NAME = "HelpingHandsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String currStation=sharedPrefs.getString("currStation","Versova");

            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
            globalVariable.setStation(currStation);

            Intent intent = new Intent(role.this, assistantLanding.class);
            startActivity(intent);
            finish();
        }

        adminLoginLayout=(RelativeLayout)findViewById(R.id.role_rl_adminLogin);
        staffLoginLayout=(RelativeLayout)findViewById(R.id.role_rl_staffLogin);

        tv_getOtp=(TextView)findViewById(R.id.role_tv_otp);
        tv_selectStation = findViewById(R.id.stationLabel);

        et_loginID=(EditText)findViewById(R.id.role_et_loginID);
        et_password=(EditText)findViewById(R.id.role_et_password);
        et_phone=(EditText)findViewById(R.id.role_et_phone);
        et_otp=(EditText)findViewById(R.id.role_et_otp);

        staffLoginBtn=(Button)findViewById(R.id.role_btn_staffLogin);
        getOtpBtn=(Button)findViewById(R.id.role_btn_getOtp);

        station_spinner = findViewById(R.id.stationAdmin);

        progressBar=(ProgressBar)findViewById(R.id.role_pb_progressBar);

        Spinner role =findViewById(R.id.userType_role);
        ArrayList<String> userType = new ArrayList<>();
        userType.add("ON GROUND SUPPORT STAFF");
        userType.add("STATION ADMINISTRATOR");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, userType);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role.setAdapter(arrayAdapter);
        role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userType = parent.getItemAtPosition(position).toString();
                if(userType=="ON GROUND SUPPORT STAFF")
                    userType="ASSISTANT";
                else
                    userType="STATION ADMIN";


                if(userType=="STATION ADMIN"){

                    adminLoginLayout.setVisibility(View.VISIBLE);
                    staffLoginLayout.setVisibility(View.GONE);
                    station_spinner.setVisibility(View.VISIBLE);
                    tv_selectStation.setVisibility(View.VISIBLE);
                }
                else if(userType=="ASSISTANT"){
                    staffLoginLayout.setVisibility(View.VISIBLE);
                    adminLoginLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        Spinner adminStationSpinner = findViewById(R.id.stationAdmin);
        ArrayList<String> adminStation = new ArrayList<>();
        adminStation.add(VERSOVA);
        adminStation.add(D_N_NAGAR);
        adminStation.add(AZAD_NAGAR);
        adminStation.add(ANDHERI);
        adminStation.add("WESTERN EXPRESS HIGHWAY");
        adminStation.add("Chakala(J.B.Nagar)");
        adminStation.add(AIRPORT_ROAD);
        adminStation.add(MAROL_NAKA);
        adminStation.add(SAKINAKA);
        adminStation.add(ASALPHA);
        adminStation.add(JAGRUTI_NAGAR);
        adminStation.add(GHATKOPAR);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, adminStation);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminStationSpinner.setAdapter(arrayAdapter2);
        adminStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adminOfStation = parent.getItemAtPosition(position).toString();
                if(adminOfStation.equals("Western Express Highway"))
                {
                    adminOfStation=WEH;
                }
                else if(adminOfStation.equals("Chakala(J.B.Nagar)"))
                {
                    adminOfStation=CHAKALA;
                }
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
    }

    public void getOTP(View view){

        mobileNumber=et_phone.getText().toString().trim();

        if(mobileNumber.length()<10){
            et_phone.setError("Invalid Number");
            et_phone.requestFocus();
            return;
        }
        else{
            sendVerificationCode(mobileNumber);

            et_phone.setEnabled(false);
            getOtpBtn.setEnabled(false);
            getOtpBtn.setTextSize(25f);
            new CountDownTimer(60000,1000)
            {
                @Override
                public void onTick(long millisUntilFinished) {
                    getOtpBtn.setText("Retry in : "+millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {

                    et_phone.setEnabled(true);
                    getOtpBtn.setEnabled(true);
                    getOtpBtn.setTextSize(30f);
                    getOtpBtn.setText("Get OTP");
                }
            }.start();

            progressBar.setVisibility(View.VISIBLE);
            tv_getOtp.setVisibility(View.VISIBLE);
            et_otp.setVisibility(View.VISIBLE);
            staffLoginBtn.setVisibility(View.VISIBLE);
        }
    }

    public void adminLogin(View view) {


        loginID=et_loginID.getText().toString().trim();
        password = et_password.getText().toString().trim();

        DocumentReference adminCred = fStore.collection("Admins")
                .document("StationAdmins")
                .collection(adminOfStation)
                .document("Credentials");


        adminCred.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    String i = documentSnapshot.getString("username");
                    String p = documentSnapshot.getString("password");

                    if(loginID.equals(i) && password.equals(p))
                    {
                        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
                        globalVariable.setStation(adminOfStation);


                        //Jumping to displayRequests
                        Intent intent = new Intent(role.this,DisplayRequests.class);
                        startActivity(intent);

                        Toast.makeText(role.this, "Logged in", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(role.this, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(role.this, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void staffLogin(View view) {


        String code = et_otp.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            et_otp.setError("Enter valid code");
            et_otp.requestFocus();
            return;
        }

        //verifying the code entered manually
        verifyVerificationCode(code);

    }

    //the method is sending verification code
    //the country id is concatenated
    //you can take the country id as user input as well
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
                    Toast.makeText(role.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                .addOnCompleteListener(role.this,
                        new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //verification successful we will start the profile activity

                                    String uid = fAuth.getCurrentUser().getUid();

                                    Toast.makeText(role.this,"Logged in", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);


                                    final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
                                    globalVariable.setStation(adminOfStation);

                                    SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor ed=sharedPrefs.edit();
                                    ed.putString("currStation",adminOfStation);
                                    ed.commit();

                                    DocumentReference assistantDoc=fStore.collection("Admins")
                                            .document("StationAdmins")
                                            .collection(adminOfStation)
                                            .document("Assistants")
                                            .collection("StationAssistants")
                                            .document(uid);

                                    Map<String,Object> assistant = new HashMap<>();
                                    assistant.put("isOnline","true");
                                    assistantDoc.set(assistant, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Intent intent = new Intent(role.this,assistantLanding.class);
                                            startActivity(intent);

                                        }
                                    });

                                } else {

                                    //verification unsuccessful.. display an error message

                                    String message = "Something is wrong, we will fix it soon...";

                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        message = "Invalid code entered...";
                                    }
                                    Toast.makeText(role.this,message,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

}

