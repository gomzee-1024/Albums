package com.utils.gdkcorp.albums.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.utils.gdkcorp.albums.R;
import com.utils.gdkcorp.albums.models.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gautam Kakadiya on 11-08-2017.
 */

public class ChipAdapter extends RecyclerView.Adapter<ChipAdapter.ChipHolder> {

    private ArrayList<User> mList;
    private Context context;
    private ChipAdapterInterface mClickListener;
    public ChipAdapter(Context context,ArrayList<User> list,ChipAdapterInterface clickListner){
        this.context = context;
        mList = list;
        mClickListener = clickListner;
    }

    @Override
    public ChipHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chip_item,parent,false);
        ChipHolder holder = new ChipHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChipHolder holder, int position) {
        holder.chipLabel.setText(mList.get(position).getLabel());
        holder.chipIcon.setImageDrawable(mList.get(position).getAvatarDrawable());
        Picasso.with(context).load(mList.get(position).getProfile_pic_url()).placeholder(R.drawable.ic_account_circle_grey_300_24dp).into(holder.chipIcon);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addAll(ArrayList<User> list){
        for(int i=0;i<list.size();++i){
            User user = list.get(i);
            boolean alreadyPresent = false;
            for(int j=0;j<mList.size();++j){
                User user1 = mList.get(j);
                if(user1.getUser_id().equals(user.getUser_id())){
                    alreadyPresent = true;
                    break;
                }
            }
            if(!alreadyPresent){
                mList.add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void remove(int position){
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<User> getSelectedUserList(){
        return mList;
    }

    public class ChipHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private User friend;
        private CircleImageView chipIcon;
        private TextView chipLabel;
        private ImageView removeButton;
        public ChipHolder(View itemView) {
            super(itemView);
            chipIcon = (CircleImageView) itemView.findViewById(R.id.chip_icon);
            chipLabel = (TextView) itemView.findViewById(R.id.chip_label);
            removeButton = (ImageView) itemView.findViewById(R.id.chip_delete);
            removeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onClickRemove(view,getAdapterPosition());
        }
    }
    public interface ChipAdapterInterface{
        public void onClickRemove(View v,int position);
    }
}
