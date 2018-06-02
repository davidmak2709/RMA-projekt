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

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class CustomListView extends ArrayAdapter<String> {


    private ArrayList<String> urls;
    private ArrayList<String> userNames;
    private ArrayList<String> uuid;
    private User user;
    private Activity context;

    private View.OnClickListener addListener;
    private View.OnClickListener removeListener;

    CustomListView(Activity context, ArrayList<String> urls, ArrayList<String> userNames,
                   ArrayList<String> uuid, User user) {
        super(context, R.layout.listview, userNames);

        this.urls = urls;
        this.userNames = userNames;
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
                user.friendsUUID.add(uuid.get(position));
                user.numFriends++;
                v.setBackground(ContextCompat.getDrawable(context,
                        android.R.drawable.ic_delete));

                v.setOnClickListener(removeListener);
            }
        };

        removeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackground(ContextCompat.getDrawable(context,
                        R.drawable.ic_person_add));

                user.friendsUUID.remove(uuid.get(position));
                user.numFriends--;
                v.setOnClickListener(addListener);
            }
        };



        Picasso.with(r.getContext())
                .load(urls.get(position))
                .resize(50, 50)
                .centerCrop()
                .into(viewHolder.profilePictureView);

        viewHolder.textView.setText(userNames.get(position));

        if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid().equals(uuid.get(position))) {

            if (user.friendsUUID.contains(uuid.get(position))) {
                viewHolder.addFriendButton.setBackground(ContextCompat.getDrawable(context,
                        android.R.drawable.ic_delete));

                viewHolder.addFriendButton.setOnClickListener(removeListener);
            } else {
                viewHolder.addFriendButton.setBackground(ContextCompat.getDrawable(context,
                        R.drawable.ic_person_add));

                viewHolder.addFriendButton.setOnClickListener(addListener);
            }
        } else {
            viewHolder.addFriendButton.setOnClickListener(null);
            viewHolder.addFriendButton.setVisibility(View.GONE);
        }

        return r;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public ArrayList<String> getUserNames() {
        return userNames;
    }

    public ArrayList<String> getUuid() {
        return uuid;
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


