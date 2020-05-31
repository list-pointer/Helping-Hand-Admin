package com.example.helpinghands_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRequestAdapter extends FirestoreRecyclerAdapter<FirestoreRequestModel, FirestoreRequestAdapter.FViewHolder> {
    private onItemClickListener listener;


    public FirestoreRequestAdapter(@NonNull FirestoreRecyclerOptions<FirestoreRequestModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final FViewHolder holder, int position, @NonNull final FirestoreRequestModel model) {

        DocumentReference userDoc =FirebaseFirestore.getInstance().collection("users").document(model.getUser_id());
        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                holder.name.setText(documentSnapshot.getString("user_name"));
                holder.req_time.setText(model.getRequest_time());
            }
        });
    }

    @NonNull
    @Override
    public FViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new FViewHolder(v);
    }

    //ViewHolder
    class FViewHolder extends RecyclerView.ViewHolder {
        TextView name, req_time;

        public FViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txt_name);
            req_time = itemView.findViewById(R.id.txt_req_time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null)
                    {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }
    }

    public interface  onItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);

    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }
}
