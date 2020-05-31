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

public class DisplayAssistants extends AppCompatActivity {
    private static final String TAG = "DisplayAssistants";
    private String currStation;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    private FirestoreAssistantAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_assistants);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        currStation = globalVariable.getStation();

        collectionReference = db
                .collection("Admins/StationAdmins/" + currStation + "/Assistants/StationAssistants");

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = collectionReference.orderBy("assistant_name");

        FirestoreRecyclerOptions<FirestoreAssistantModel> options = new FirestoreRecyclerOptions.Builder<FirestoreAssistantModel>()
                .setQuery(query, FirestoreAssistantModel.class)
                .build();

        adapter = new FirestoreAssistantAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FirestoreAssistantAdapter.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                FirestoreAssistantModel firestoreAssistantModel = documentSnapshot.toObject(FirestoreAssistantModel.class);
//                Toast.makeText(DisplayAssistants.this, "ID : " + documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DisplayAssistants.this, editAssistantProfile.class);
                intent.putExtra("id", documentSnapshot.getId());
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

    public void addAssistant(View view) {
        Intent addAssistantIntent = new Intent(DisplayAssistants.this,registerAssistantNumber.class);
        startActivity(addAssistantIntent);
    }
}
