package com.lancy.bookreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Book book;
    private BookSeller seller;
    private DatabaseReference databaseChatReference;
    private ArrayList<Chat> chatMessages;
    private ChatRecyclerAdaptor chatRecyclerAdaptor;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        linkUIElements();
        getDataFromPreviousActivity();
        getDatabaseReference();
        listenToMessages();
    }

    private void linkUIElements() {
        chatMessages = new ArrayList<>();

        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);

        messageEditText = findViewById(R.id.messageEditText);
    }

    private void getDataFromPreviousActivity() {
        Bundle extras = getIntent().getExtras();
        book = extras.getParcelable("book");
        seller = extras.getParcelable("seller");
    }

    private void getDatabaseReference() {
        if (seller.sellerUserID.compareTo(UserInterfaceHelper.userID()) > 0) {
            String chat_id = seller.sellerUserID + "_" + UserInterfaceHelper.userID();
            databaseChatReference = FirebaseDatabase.getInstance().getReference()
                    .child("chat").child(book.mISBN).child(chat_id);
        } else {
            String chat_id = seller.sellerUserID + "_" + UserInterfaceHelper.userID();
            databaseChatReference = FirebaseDatabase.getInstance().getReference()
                    .child("chat").child(book.mISBN).child(chat_id);
        }
    }

    private void listenToMessages() {
        databaseChatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessages.clear();

                for (DataSnapshot chatSnapshot: dataSnapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    chatMessages.add(chat);
                }

                chatRecyclerAdaptor = new ChatRecyclerAdaptor(ChatActivity.this, chatMessages);
                chatRecyclerAdaptor.sellerID = seller.sellerUserID;
                chatRecyclerView.setAdapter(chatRecyclerAdaptor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void sendMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseChatReference.push().setValue(constructChat(message))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    UserInterfaceHelper.hideKeyboard(messageEditText, ChatActivity.this);
                    messageEditText.getText().clear();
                }
            }
        });
    }

    private Map constructChat(String message) {
        Map chat = new HashMap();

        chat.put("message", message);
        chat.put("from", UserInterfaceHelper.userID());
        chat.put("to", seller.sellerUserID);
        chat.put("timestamp", System.currentTimeMillis());

        return chat;
    }

    public void sendButtonTapped(View view) {
        sendMessage(messageEditText.getText().toString());
    }
}
