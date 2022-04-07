package com.example.project_test.Models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_test.EditPurchaseItem;
import com.example.project_test.PurchaseList;
import com.example.project_test.R;

import java.util.ArrayList;


public class PurchasesListAdapter extends RecyclerView.Adapter<PurchasesListAdapter.PurchasesLisViewHolder> {

    Context context;
    ArrayList<ItemData> purchasesList;

    public PurchasesListAdapter(Context context, ArrayList<ItemData> purchasesList) {
        this.context = context;
        this.purchasesList = purchasesList;
    }

    @NonNull
    @Override
    public PurchasesLisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View purchasesView = LayoutInflater.from(context).inflate(R.layout.purchases_list_single_itemview,parent,false);
        return new PurchasesLisViewHolder(purchasesView);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasesLisViewHolder holder, int position) {
        ItemData itemListForPurchases = purchasesList.get(position);
        holder.itemName.setText(itemListForPurchases.getName());
        holder.itemStatus.setText(itemListForPurchases.getStatus());
        holder.itemQty.setText(itemListForPurchases.getQty());
        holder.itemDesc.setText(itemListForPurchases.getDesc());
        holder.itemPlacedBy.setText(itemListForPurchases.getPlacedBy());
        holder.itemPurchasedBy.setText(itemListForPurchases.getPurchasedBy());
        boolean isVisible = itemListForPurchases.visibility;
        holder.showMoreLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return purchasesList.size();
    }

    public class PurchasesLisViewHolder extends RecyclerView.ViewHolder{

        TextView itemName, itemStatus, itemQty, itemDesc, itemPlacedBy, itemPurchasedBy;
        LinearLayout showMoreLayout, TapToShow;
        ImageView ItemStatusIcon, OpenItemBtn, DownArrow;

        public PurchasesLisViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = itemView.findViewById(R.id.itemName);
            itemStatus = itemView.findViewById(R.id.itemStatus);
            itemQty = itemView.findViewById(R.id.itemQty);
            itemDesc = itemView.findViewById(R.id.itemDesc);
            itemPlacedBy = itemView.findViewById(R.id.itemPlacedby);
            itemPurchasedBy = itemView.findViewById(R.id.itemPurchasedby);
            showMoreLayout = itemView.findViewById(R.id.showMore);
            TapToShow = itemView.findViewById(R.id.tapToShow);
            ItemStatusIcon = itemView.findViewById(R.id.itemStatusIcon);
            OpenItemBtn = itemView.findViewById(R.id.openItem);
            DownArrow = itemView.findViewById(R.id.downArrow);

            TapToShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ItemData moreData = purchasesList.get(getAdapterPosition());
                    moreData.setVisibility(!moreData.isVisibility());
                    if(moreData.visibility == true)
                        DownArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);
                    else if(moreData.visibility == false)
                        DownArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);

                    if(moreData.getStatus().equals("Awaiting"))
                        ItemStatusIcon.setImageResource(R.drawable.awaiting);
                   else if(moreData.getStatus().equals("Not available"))
                        ItemStatusIcon.setImageResource(R.drawable.not_available);
                    else if(moreData.getStatus().equals("Purchased"))
                        ItemStatusIcon.setImageResource(R.drawable.purchased);

                    notifyDataSetChanged();

                    OpenItemBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String itemID = moreData.getName().toString();
                            Intent Dialog = new Intent(context.getApplicationContext(), EditPurchaseItem.class);
                            Dialog.putExtra("itemID", itemID);
                            context.startActivity(Dialog);
                        }
                    });
                }
            });
        }
    }

}
