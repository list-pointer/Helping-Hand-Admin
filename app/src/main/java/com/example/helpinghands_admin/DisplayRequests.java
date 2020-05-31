package com.example.helpinghands_admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class DisplayRequests extends AppCompatActivity{

    private static final String TAG = "DisplayRequests";
    private String currStation;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    private FirestoreRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_requests);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation = globalVariable.getStation();

        collectionReference = db.collection("Admins/StationAdmins/" + currStation + "/Requests/ActiveRequests");

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = collectionReference.whereEqualTo("staff_id",null);

        FirestoreRecyclerOptions<FirestoreRequestModel> options = new FirestoreRecyclerOptions.Builder<FirestoreRequestModel>()
                .setQuery(query, FirestoreRequestModel.class)
                .build();

        adapter = new FirestoreRequestAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FirestoreRequestAdapter.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                FirestoreRequestModel firestoreRequestModel = documentSnapshot.toObject(FirestoreRequestModel.class);
//                Toast.makeText(DisplayRequests.this, "ID : " + documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                String id = firestoreRequestModel.getUser_id();
                Intent intent = new Intent(DisplayRequests.this, Confirmation_screen.class);
                intent.putExtra("id", id);
                intent.putExtra("request_id",documentSnapshot.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void manageAssistants(View view) {
        Intent intent= new Intent(DisplayRequests.this,DisplayAssistants.class);
        startActivity(intent);
    }
}