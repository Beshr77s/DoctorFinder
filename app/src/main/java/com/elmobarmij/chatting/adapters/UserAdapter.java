package com.elmobarmij.chatting.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmobarmij.chatting.databinding.ItemContainerUsersBinding;
import com.elmobarmij.chatting.listeners.UserListeners;
import com.elmobarmij.chatting.models.User;
import com.elmobarmij.chatting.utiles.Constants;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private final UserListeners listeners;

    public UserAdapter(List<User> users,UserListeners listeners) {
        this.users = users;
        this.listeners=listeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUsersBinding binding =
                ItemContainerUsersBinding
                        .inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        private ItemContainerUsersBinding binding;
        UserViewHolder(ItemContainerUsersBinding itemContainerUsersBinding){
            super(itemContainerUsersBinding.getRoot());
            this.binding = itemContainerUsersBinding;
        }
        void setUserData(User user){
            binding.tvName.setText(user.name);
            binding.tvEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v ->listeners.onUserClicked(user) );
        }
    }
    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
}
