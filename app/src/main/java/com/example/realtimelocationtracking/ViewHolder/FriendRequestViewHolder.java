package com.example.realtimelocationtracking.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimelocationtracking.Interface.IRecyclerItemClickListener;
import com.example.realtimelocationtracking.R;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder{
    public TextView txt_user_email;
    public CardView cardView;
    public ImageView btn_accept,btn_decline;

    public FriendRequestViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = itemView.findViewById(R.id.txt_user_email);
        cardView = itemView.findViewById(R.id.cardView);
        btn_accept = itemView.findViewById(R.id.btn_accept);
        btn_decline = itemView.findViewById(R.id.btn_decline);
    }

}