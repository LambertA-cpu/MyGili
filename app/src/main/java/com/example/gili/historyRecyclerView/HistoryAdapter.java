package com.example.gili.historyRecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gili.HistoryActivity;
//import com.google.api.Context;
import com.example.gili.R;
import com.google.firebase.database.core.Context;

import java.util.ArrayList;
import java.util.List;



    public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {

        private List<HistoryObject> itemList;
        private Context context;

        public HistoryAdapter(List<HistoryObject> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        public HistoryAdapter(ArrayList<HistoryObject> dataSetHistory, HistoryActivity historyActivity) {
        }

        @Override
        public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            HistoryViewHolders rcv = new HistoryViewHolders(layoutView);
            return rcv;
        }

        @Override
        public void onBindViewHolder(HistoryViewHolders holder, final int position) {
            holder.cleanId.setText(itemList.get(position).getCleanId());
            if(itemList.get(position).getTime()!=null){
                holder.time.setText(itemList.get(position).getTime());
            }
        }
        @Override
        public int getItemCount() {
            return this.itemList.size();
        }

    }
