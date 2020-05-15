package com.example.gili.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gili.HistorySingleActivity;
import com.example.gili.R;


public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView cleanId;
    public TextView time;

    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        cleanId = (TextView) itemView.findViewById(R.id.cleanId);
        time = (TextView) itemView.findViewById(R.id.time);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("cleanId", cleanId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}