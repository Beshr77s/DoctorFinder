package com.elmobarmij.chatting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elmobarmij.chatting.R;
import com.elmobarmij.chatting.adapters.UserAdapter;
import com.elmobarmij.chatting.databinding.ActivityUsersBinding;
import com.elmobarmij.chatting.listeners.UserListeners;
import com.elmobarmij.chatting.models.User;
import com.elmobarmij.chatting.utiles.Constants;
import com.elmobarmij.chatting.utiles.ReferencesManger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListeners {
    private ActivityUsersBinding binding;
    private ReferencesManger referencesManger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        referencesManger = new ReferencesManger(getApplicationContext());

        try {
            getUsers();
            setListeners();
        }catch (Exception e){

        }

    }

    private void setListeners() {
        binding.icBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers() throws Exception{
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                   loading(false);
                   String currentUserId = referencesManger.getString(Constants.KEY_USER_ID);
                   if (task.isSuccessful() && task.getResult() != null){
                       List<User> users = new ArrayList<>();
                       for (QueryDocumentSnapshot snapshot :task.getResult()) {
                           if (currentUserId.equals(snapshot.getId())) {
                               continue;
                           }
                           User user = new User();
                           user.name = snapshot.getString(Constants.KEY_NAME);
                           user.email = snapshot.getString(Constants.KEY_EMAIL);
                           user.image = snapshot.getString(Constants.KEY_IMAGE);
                           user.token =snapshot.getString(Constants.KEY_FCM_TOKEN);
                           user.id = snapshot.getId();
                           users.add(user);
                       }
                           if (users.size() > 0){
                               UserAdapter adapter = new UserAdapter(users,this::onUserClicked);
                               binding.rvUsers.setAdapter(adapter);
                               binding.rvUsers.setVisibility(View.VISIBLE);
                           }else {
                               showErrorMessage();
//                               showToast("dd");
                           }
                       }else {
                       showErrorMessage();
                   }
                });
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(){
        binding.tvErrorMessage.setText(String.format("%s","No User Available"));
        binding.tvErrorMessage.setVisibility(View.GONE);
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.shimmer.getRoot().setVisibility(View.VISIBLE);
//            binding.progress.setVisibility(View.VISIBLE);
        }else {
            binding.shimmer.getRoot().setVisibility(View.GONE);
//            binding.progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}