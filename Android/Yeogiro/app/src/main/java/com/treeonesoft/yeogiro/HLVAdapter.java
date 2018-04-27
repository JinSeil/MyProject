package com.treeonesoft.yeogiro;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class HLVAdapter extends RecyclerView.Adapter<HLVAdapter.ViewHolder> {
    ArrayList<User> mList;

    Context context;

    ItemClickListener mListener;

    public HLVAdapter(Context context, ArrayList<User> userList, ItemClickListener listener) {
        super();
        this.context = context;
        mList = userList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_card_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.tvName.setText(mList.get(i).getDisplayName());
        if( mList.get(i).getPhotoUrl() != null && mList.get(i).getPhotoUrl().startsWith("http") )
        {
            Log.d("SESIN_AD", "photoUrl = " + mList.get(i).getPhotoUrl());
            Glide.with(context).load(mList.get(i).getPhotoUrl()).into(viewHolder.imgThumbnail);
        }
        else
        {
            Log.d("SESIN_AD", "photoUrl null");
            viewHolder.imgThumbnail.setImageResource(R.drawable.noprofile);
        }
//        viewHolder.imgThumbnail.setImageResource(mList.get(i).getPhotoUrl());

        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (mListener != null)
                    mListener.onClick(view, position, isLongClick);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ImageView imgThumbnail;
        public TextView tvName;
        private ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.ivProfileImg);
            tvName = itemView.findViewById(R.id.tvName);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getPosition(), true);
            return true;
        }
    }
}
