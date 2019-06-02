package com.lancy.bookreview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatRecyclerAdaptor
        extends RecyclerView.Adapter<ChatRecyclerAdaptor.ViewHolder> {
    private ArrayList<Chat> chatMessages;
    private Context context;

    public ChatRecyclerAdaptor(Context context, ArrayList<Chat> messages) {
        this.context = context;
        this.chatMessages = messages;
    }

    @Override
    public int getItemCount() {
        if (chatMessages == null) {
            return 0;
        }

        return chatMessages.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_chat_list, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Chat chat = chatMessages.get(i);

        viewHolder.messageTextView.setText(chat.message);
    }

    // View holder for recycler view.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.messageTextView);
            view = itemView;
        }
    }
}