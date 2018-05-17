package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListView extends ArrayAdapter<String> {

    private ArrayList<String> urls;
    private ArrayList<String> usernames;
    private ArrayList<String> uuid;
    private User user;
    private Activity context;

    private View.OnClickListener addListener;
    private View.OnClickListener removeListener;

    CustomListView(Activity context, ArrayList<String> urls, ArrayList<String> usernames,
                   ArrayList<String> uuid, User user) {
        super(context, R.layout.listview, usernames);

        this.urls = urls;
        this.usernames = usernames;
        this.uuid = uuid;
        this.user = user;
        this.context = context;

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View r = convertView;
        ViewHolder viewHolder;

        if (r == null) {
            LayoutInflater layoutInflater = context.getLayoutInflater();
            r = layoutInflater.inflate(R.layout.listview, null, true);
            viewHolder = new ViewHolder(r);
            r.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) r.getTag();
        }


        addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Dodaj", "add friend");

                user.friendsUUID.add(uuid.get(position));

                v.setBackground(ContextCompat.getDrawable(context,
                        android.R.drawable.ic_delete));

                v.setOnClickListener(removeListener);
            }
        };

        removeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Dodaj", "remove friend");

                v.setBackground(ContextCompat.getDrawable(context,
                        android.R.drawable.ic_input_add));

                user.friendsUUID.remove(uuid.get(position));

                v.setOnClickListener(addListener);
            }
        };



        Picasso.with(r.getContext())
                .load(urls.get(position))
                .resize(50, 50)
                .centerCrop()
                .into(viewHolder.profilePictureView);

        viewHolder.textView.setText(usernames.get(position));

        if (user.friendsUUID.contains(uuid.get(position))) {
            viewHolder.addFriendButton.setBackground(ContextCompat.getDrawable(context,
                    android.R.drawable.ic_delete));

            viewHolder.addFriendButton.setOnClickListener(removeListener);

        } else {
            viewHolder.addFriendButton.setOnClickListener(addListener);
        }

        return r;
    }

    class ViewHolder {
        TextView textView;
        ImageView profilePictureView;
        Button addFriendButton;

        ViewHolder(View v) {
            textView = v.findViewById(R.id.usernameTextView);
            profilePictureView = v.findViewById(R.id.profilePicture);
            addFriendButton = v.findViewById(R.id.addFriendButton);
        }
    }
}


