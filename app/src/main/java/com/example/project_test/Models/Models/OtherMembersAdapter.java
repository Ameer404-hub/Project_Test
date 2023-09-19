package com.example.project_test.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_test.R;

import java.util.ArrayList;

public class OtherMembersAdapter extends RecyclerView.Adapter<OtherMembersAdapter.OtherMembersViewHolder> {

    Context context;
    ArrayList<UserSignupData> OtherMembersList;

    public OtherMembersAdapter(Context context, ArrayList<UserSignupData> otherMembersList) {
        this.context = context;
        OtherMembersList = otherMembersList;
    }

    @NonNull
    @Override
    public OtherMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View OtherMembersView = LayoutInflater.from(context).inflate(R.layout.other_members_single_itmeview,parent,false);
        return new OtherMembersAdapter.OtherMembersViewHolder(OtherMembersView);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherMembersViewHolder holder, int position) {
        UserSignupData getMembers = OtherMembersList.get(position);
        holder.memberName.setText(getMembers.getFirstName() + " " + getMembers.getLastName());
        holder.memberPhone.setText(getMembers.getPhone());
    }

    @Override
    public int getItemCount() {
        return OtherMembersList.size();
    }

    public static class OtherMembersViewHolder extends RecyclerView.ViewHolder {

        TextView memberName, memberPhone;

        public OtherMembersViewHolder(@NonNull View itemView) {
            super(itemView);

            memberName = itemView.findViewById(R.id.Name);
            memberPhone = itemView.findViewById(R.id.Phone);
        }
    }
}
