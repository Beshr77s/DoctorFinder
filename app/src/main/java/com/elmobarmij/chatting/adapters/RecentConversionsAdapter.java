package com.elmobarmij.chatting.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmobarmij.chatting.databinding.ItemContainerRecentConversisionBinding;
import com.elmobarmij.chatting.databinding.ItemContainerSentMessageBinding;
import com.elmobarmij.chatting.listeners.ConversionListeners;
import com.elmobarmij.chatting.listeners.UserListeners;
import com.elmobarmij.chatting.models.ChatMessage;
import com.elmobarmij.chatting.models.User;

import java.util.List;

public class RecentConversionsAdapter extends RecyclerView.Adapter<RecentConversionsAdapter.ConversitonsViewHolder> {
    private List<ChatMessage> messages;
    private ConversionListeners listeners;

    public RecentConversionsAdapter(List<ChatMessage> messages, ConversionListeners listeners) {
        this.messages = messages;
        this.listeners = listeners;
    }



    @NonNull
    @Override
    public ConversitonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversitonsViewHolder(ItemContainerRecentConversisionBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversitonsViewHolder holder, int position) {
        holder.setData(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ConversitonsViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecentConversisionBinding binding;
        ConversitonsViewHolder(ItemContainerRecentConversisionBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
        private void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getUserImage(chatMessage.conversionImage));
            binding.tvRecentMessage.setText(chatMessage.message);
            binding.tvName.setText(chatMessage.conversionName);
            User user = new User();
            user.id = chatMessage.conversionId;
            user.name = chatMessage.conversionName;
            user.image = chatMessage.conversionImage;
            binding.getRoot().setOnClickListener(v ->listeners.onConversionClicked(user) );


        }
    }


    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
}
