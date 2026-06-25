package com.example.quinielapajarovsroman;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_standing, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.name);
        holder.userPoints.setText(String.valueOf(user.points));
        holder.exactCount.setText(user.exactAciertos + " AC.");
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userPoints, exactCount;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userPoints = itemView.findViewById(R.id.userPoints);
            exactCount = itemView.findViewById(R.id.exactCount);
        }
    }
}
