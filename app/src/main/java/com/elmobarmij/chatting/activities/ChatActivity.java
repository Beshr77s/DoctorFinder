package com.elmobarmij.chatting.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.elmobarmij.chatting.adapters.ChatAdapter;
import com.elmobarmij.chatting.databinding.ActivityChatBinding;
import com.elmobarmij.chatting.listeners.ImageListeners;
import com.elmobarmij.chatting.models.ChatMessage;
import com.elmobarmij.chatting.models.User;
import com.elmobarmij.chatting.network.ApiClient;
import com.elmobarmij.chatting.network.ApiService;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements ImageListeners {
    private ActivityChatBinding binding;
    private User receiveUser;
    private List<ChatMessage> chatMessages;
    private ReferencesManger referencesManger;
    private FirebaseFirestore database;
    private ChatAdapter chatAdapter;
    private String conversionID= null;
    private Boolean isReceivedAvailability;
    private String encodeImage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceivedUser();
        init();
        setListeners();
        listenMessages();
        checkFoeConversion();

    }
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiveUser.id)
                .addSnapshotListener(ChatActivity.this,(value, error) -> {
                    if (error!=null){
                        return;
                    }
                    if (value != null){
//
                        String isAv = value.getString(Constants.KEY_AVAILABILITY);
                        binding.tvName.setText(receiveUser.name+"\n"+"["+isAv+"]");

                        if (isAv.equals("Online")){
                            isReceivedAvailability = true;
                        }else {
                            isReceivedAvailability = false;
                        }
                        receiveUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if (receiveUser.image == null){
                            receiveUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverImage(getUserImage(value.getString(receiveUser.image)));
                            chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                        }
//                        if (isReceivedAvailability){
//                            binding.tvIsAv.setVisibility(View.VISIBLE);
//                            binding.tvIsAv.setText("Online");

//                        }else {
//                            binding.tvIsAv.setVisibility(View.GONE);
                            ;

//                        }
                    }
                });
    }
    private void sendNotification(String messageBody){
        ApiClient.getRetrofit().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader()
                ,messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()){

                }else {
                    showToast("Error : "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {

            }
        });
    }
    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,referencesManger.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiveUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,referencesManger.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_SENDER_ID,receiveUser.id)
                .addSnapshotListener(eventListener);
    }
    private void checkFoeConversion(){
        if (chatMessages.size() != 0){
            checkForConversionRemotely(
                    Constants.KEY_USER_ID,
                    receiveUser.id
            );
            checkForConversionRemotely(
                    receiveUser.id,
                    Constants.KEY_USER_ID
            );
        }
    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get().addOnCompleteListener(completionListener);
    }
    private final OnCompleteListener<QuerySnapshot> completionListener = task -> {
        if (task.isSuccessful()
                && task.getResult() !=null
                && task.getResult().getDocuments().size() >0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionID = documentSnapshot.getId();
        }
    };
    private Bitmap getUserImage(String encodeImage){
        if (encodeImage !=null) {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else
            return null;
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
        if (error !=null){
            return ;
        }if (value !=null){
            int count = chatMessages.size();
            for (DocumentChange change :value.getDocumentChanges()){
                if (change.getType() == DocumentChange.Type.ADDED){
                    ChatMessage message = new ChatMessage();
                    message.messageId = change.getDocument().getId();
                    message.receivedId = change.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    message.senderId = change.getDocument().getString(Constants.KEY_SENDER_ID);
                    message.message = change.getDocument().getString(Constants.KEY_MESSAGE);
                    message.dataTime = getReadAbleData(change.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    message.dateObject = change.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    message.image = change.getDocument().getString(Constants.KEY_IMAGE);
                    message.status = change.getDocument().getString(Constants.KEY_STATUS);
                    chatMessages.add(message);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2) ->obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0 ){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemRangeChanged(chatMessages.size(),chatMessages.size());
                binding.rvChats.smoothScrollToPosition(chatMessages.size() - 1);
            }

        }
        binding.rvChats.setVisibility(View.VISIBLE);
        binding.progress.setVisibility(View.GONE);
        if (conversionID == null){
            checkFoeConversion();
        }
    };
    private void updateConversion(String message){
        DocumentReference reference = database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .document(conversionID);
        reference.update(Constants.KEY_LastMessage,message,Constants.KEY_TIMESTAMP,new Date());
    }
    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionID = documentReference.getId());
    }
    private String getReadAbleData(Date date){
        return new SimpleDateFormat("dd, -hh:mm a", Locale.getDefault()).format(date);
    }

    private void init() {
        referencesManger = new ReferencesManger(getApplicationContext());
        chatMessages = new ArrayList<>();
         chatAdapter = new ChatAdapter(chatMessages,getUserImage(receiveUser.image),referencesManger.getString(Constants.KEY_USER_ID),
                 this::onImageClicked);
        binding.rvChats.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }
    private void sendMessage(){
        HashMap<String,Object> data = new HashMap<>();
        data.put(Constants.KEY_SENDER_ID,referencesManger.getString(Constants.KEY_USER_ID));
        data.put(Constants.KEY_RECEIVER_ID,receiveUser.id);
        data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        data.put(Constants.KEY_TIMESTAMP,new Date());
        data.put(Constants.KEY_IMAGE,encodeImage);
        data.put(Constants.KEY_STATUS,0);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                updateMessage(documentReference.getId(),1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                
            }
        });
        if (conversionID !=null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String ,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,referencesManger.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,referencesManger.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,referencesManger.getString(Constants.KEY_SENDER_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiveUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiveUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiveUser.image);
            conversion.put(Constants.KEY_STATUS,false);
            conversion.put(Constants.KEY_LastMessage,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if (!isReceivedAvailability){
            JSONArray tokens = new JSONArray();
            tokens.put(receiveUser.token);
            try {
                JSONObject object = new JSONObject();
                object.put(Constants.KEY_USER_ID,referencesManger.getString(Constants.KEY_USER_ID));
                object.put(Constants.KEY_NAME,referencesManger.getString(Constants.KEY_NAME));
                object.put(Constants.KEY_NAME,referencesManger.getString(Constants.KEY_NAME));
                object.put(Constants.KEY_FCM_TOKEN,referencesManger.getString(Constants.KEY_FCM_TOKEN));
                object.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,object);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());


                Toast.makeText(getApplicationContext(), "s", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                showToast(e.toString());
            }

        }

        binding.inputMessage.setText(null);

    }

    private void loadReceivedUser(){
        receiveUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.tvName.setText(receiveUser.name);
    }
    private void setListeners(){
        binding.icBack.setOnClickListener(v -> onBackPressed());
        binding.sendMessage.setOnClickListener(v -> sendMessage());
        binding.sendImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });
    }
    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK){
                            if (result.getData() !=null){
                                Uri imageUri = result.getData().getData();
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                                    binding.image.setImageBitmap(bitmap);
                                    encodeImage = encodeImage(bitmap);
                                    sendImage(encodeImage);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private void sendImage(String encodeImage){
        HashMap<String,Object> data = new HashMap<>();
        data.put(Constants.KEY_SENDER_ID,referencesManger.getString(Constants.KEY_USER_ID));
        data.put(Constants.KEY_RECEIVER_ID,receiveUser.id);
            data.put(Constants.KEY_MESSAGE,"image");
        data.put(Constants.KEY_TIMESTAMP,new Date());
        data.put(Constants.KEY_IMAGE,encodeImage);
        data.put(Constants.KEY_STATUS,false);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(data);
        if (conversionID !=null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String ,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,referencesManger.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,referencesManger.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,referencesManger.getString(Constants.KEY_SENDER_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiveUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiveUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiveUser.image);
            conversion.put(Constants.KEY_LastMessage,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if (!isReceivedAvailability){
            JSONArray tokens = new JSONArray();
            tokens.put(receiveUser.token);
            try {
                JSONObject object = new JSONObject();
                object.put(Constants.KEY_USER_ID,referencesManger.getString(Constants.KEY_USER_ID));
                object.put(Constants.KEY_NAME,referencesManger.getString(Constants.KEY_NAME));
                object.put(Constants.KEY_NAME,referencesManger.getString(Constants.KEY_NAME));
                object.put(Constants.KEY_FCM_TOKEN,referencesManger.getString(Constants.KEY_FCM_TOKEN));
                object.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,object);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());


                Toast.makeText(getApplicationContext(), "s", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                showToast(e.toString());
            }

        }

        binding.inputMessage.setText(null);

    }


    @Override
    public void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    public void onImageClicked(String encode) {
        Intent intent = new Intent(getApplicationContext(),PreviewActivity.class);
        intent.putExtra(Constants.KEY_IMAGE,encode);
        startActivity(intent);
    }
    private void updateMessage(String messageId ,Integer nm) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CHAT)
                .document(messageId);
        documentReference.update(Constants.KEY_STATUS,nm);
    }

}