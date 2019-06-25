package com.lancy.bookreview.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lancy.bookreview.R;
import com.lancy.bookreview.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersListRecyclerAdapter
        extends RecyclerView.Adapter<UsersListRecyclerAdapter.ViewHolder> {

    private ArrayList<User> users;
    private Context context;
    private RecyclerViewSelection selection;

    public UsersListRecyclerAdapter(Context context,
                                     ArrayList<User> users,
                                     RecyclerViewSelection handler) {
        this.users = users;
        this.context = context;
        selection = handler;
    }

    @Override
    public int getItemCount() {
        if (users == null) {
            return 0;
        }

        return users.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_users_chat_list, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user = users.get(i);

        viewHolder.usernameTextView.setText(user.username);
        if (user.image != null && user.image.length() > 0) {
            Picasso.get().load(user.image).into(viewHolder.userIconImageView);
        } else {
            viewHolder.userIconImageView.setImageResource(R.drawable.ic_person);
        }

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection.selected(user);
            }
        });
    }

    // View holder for recycler view.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameTextView;
        public CircleImageView userIconImageView;
        public View view;

        public ViewHolder(View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            userIconImageView = itemView.findViewById(R.id.profileImageView);
            view = itemView;
        }
    }

    public interface RecyclerViewSelection {
        public void selected(User user);
    }
}
