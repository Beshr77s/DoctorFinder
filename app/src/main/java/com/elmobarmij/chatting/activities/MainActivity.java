package com.elmobarmij.chatting.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.elmobarmij.chatting.adapters.RecentConversionsAdapter;
import com.elmobarmij.chatting.databinding.ActivityMainBinding;
import com.elmobarmij.chatting.listeners.ConversionListeners;
import com.elmobarmij.chatting.models.ChatMessage;
import com.elmobarmij.chatting.models.User;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements ConversionListeners {
    private ActivityMainBinding binding;
    private ReferencesManger referencesManger;
    private List<ChatMessage> conversions;
    private RecentConversionsAdapter conversionsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        try {
            init();
            loadUserDetails();
//             getToken();
            setListeners();
//            listenMessages();
        }catch (Exception e){
            showToast(e.toString());
        }


    }
    private void init(){
        referencesManger = new ReferencesManger(getApplicationContext());
        conversions = new ArrayList<>();
        conversionsAdapter = new RecentConversionsAdapter(conversions,this::onConversionClicked);
        binding.recentConRV.setAdapter(conversionsAdapter);
        database = FirebaseFirestore.getInstance();

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
        if (error !=null){
            return ;
        }if (value !=null){
            for (DocumentChange change :value.getDocumentChanges()){
                if (change.getType() == DocumentChange.Type.MODIFIED){
                    ChatMessage message = new ChatMessage();
                    message.receivedId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    message.senderId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                    if (referencesManger.getString(Constants.KEY_USER_ID).equals(message.senderId)){
                        message.conversionImage = change.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        message.conversionName = change.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        message.conversionId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else {
                        message.conversionImage = change.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        message.conversionName = change.getDocument().getString(Constants.KEY_SENDER_NAME);
                        message.conversionId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    message.message = change.getDocument().getString(Constants.KEY_LastMessage);
                    message.dateObject = change.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    conversions.add(message);
                }
                else if (change.getType() == DocumentChange.Type.ADDED){
                    for (int i = 0; i < conversions.size(); i++) {
                        String senderId =  change.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId =  change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receivedId.equals(receiverId)){
                            conversions.get(i).message =  change.getDocument().getString(Constants.KEY_LastMessage);;
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversions,(obj1, obj2) ->obj1.dateObject.compareTo(obj2.dateObject));
                conversionsAdapter.notifyDataSetChanged();
                binding.recentConRV.smoothScrollToPosition(0);
//
                conversionsAdapter.notifyItemRangeChanged(conversions.size(),conversions.size());
                binding.recentConRV.smoothScrollToPosition(conversions.size() - 1);
//
            if (conversions.size()>0){
                binding.recentConRV.setVisibility(View.VISIBLE);
//                binding.progress.setVisibility(View.GONE);
            }
        }

    };
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_SENDER_ID,
                        referencesManger.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,
                        referencesManger.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private String getReadAbleData(Date date) {
        return new SimpleDateFormat("dd, yyyy -hh:mm a", Locale.getDefault()).format(date);
    }
    private void setListeners() {
        binding.imageLogout.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),UsersActivity.class));
        });
    }
    private void signOut() {
    }
    private void loadUserDetails() {
        binding.tvName.setText(referencesManger.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(referencesManger.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        referencesManger.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                .document(referencesManger.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)

        .addOnSuccessListener(unused -> {
            showToast("Update FCM !");
        }).addOnFailureListener(e -> {
            showToast("UnUpdate FCM !");
        });
    }
    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}