package com.example.helpinghands_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreAssistantAdapter extends FirestoreRecyclerAdapter<FirestoreAssistantModel, FirestoreAssistantAdapter.FViewHolder> {
    private onItemClickListener listener;
    public FirestoreAssistantAdapter(@NonNull FirestoreRecyclerOptions<FirestoreAssistantModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FViewHolder holder, int position, @NonNull FirestoreAssistantModel model) {
        holder.name.setText(model.getAssistant_name());
        holder.req_time.setText(model.getAssistant_employee_number());
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

    //ItemCLick interface
    public interface  onItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener =  listener;
    }
}
