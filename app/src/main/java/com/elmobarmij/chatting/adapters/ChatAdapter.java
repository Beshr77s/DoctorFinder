package com.elmobarmij.chatting.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmobarmij.chatting.databinding.ItemContainerRecivedMessageBinding;
import com.elmobarmij.chatting.databinding.ItemContainerSentMessageBinding;
import com.elmobarmij.chatting.listeners.ImageListeners;
import com.elmobarmij.chatting.models.ChatMessage;
import com.elmobarmij.chatting.utiles.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private Bitmap receivedProfileImage ;
    private final String senderId;
    private ImageListeners imageListeners;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverImage(Bitmap bitmap){
        receivedProfileImage = bitmap;
    }



    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receivedProfileImage
            , String senderId,ImageListeners imageListeners) {
        this.chatMessages = chatMessages;
        this.imageListeners = imageListeners;
        this.receivedProfileImage = receivedProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SendMessageViewHolder(ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),parent,false
            )) ;
        }else
            return new ReceivedMessageViewHolder(ItemContainerRecivedMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false
        )) ;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position),imageListeners);
        }else {
            ((ReceivedMessageViewHolder) holder).setData(
                    chatMessages.get(position),
                    receivedProfileImage
                    ,imageListeners);

        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            Log.i("ATV",chatMessages.get(position).senderId+"\n"+senderId);
            return VIEW_TYPE_SENT;
        }else {
            Log.i("ATV",chatMessages.get(position).senderId+"\n"+senderId);

            return VIEW_TYPE_RECEIVED;
        }
    }
    public static Bitmap getUserImage(String encodeImage){
        if (encodeImage !=null) {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else
            return null;
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SendMessageViewHolder(ItemContainerSentMessageBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
        private void setData(ChatMessage chatMessage,ImageListeners listeners){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);

            switch (chatMessage.status){
                case "0":
                    binding.textView.setText("Sending");
                    break;
                case "1":
                    binding.textView.setText("Delivered");
                    break;
                case "2":
                    binding.textView.setText("Seen");
                    break;
            }

            if (chatMessage.message.equals("image")){
                binding.imageView.setImageBitmap(getUserImage(chatMessage.image));
            }else {
                binding.imageView.setVisibility(View.GONE);
            }
            binding.imageView.setOnClickListener(v -> {
                listeners.onImageClicked(chatMessage.image);
            });
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecivedMessageBinding binding;
        ReceivedMessageViewHolder(ItemContainerRecivedMessageBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(ChatMessage chatMessage,Bitmap receivedProfileImage,ImageListeners listeners){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);
            if (chatMessage.status == "1"){
                updateMessage(chatMessage);
            }
            if (receivedProfileImage != null){
                binding.imageProfile.setImageBitmap(receivedProfileImage);
            }
            if (chatMessage.message.equals("image")){
                binding.imageView.setImageBitmap(getUserImage(chatMessage.image));
            }else {
                binding.imageView.setVisibility(View.GONE);
            }
            binding.imageView.setOnClickListener(v -> {
                listeners.onImageClicked(chatMessage.image);
            });

        }

        private void updateMessage(ChatMessage chatMessage) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CHAT)
                    .document(chatMessage.messageId);
            documentReference.update(Constants.KEY_STATUS,2);
        }
    }
}
