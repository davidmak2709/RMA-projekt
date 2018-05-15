package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListView extends ArrayAdapter<String> {

    private ArrayList<String> urls;
    private ArrayList<String> usernames;
    private Activity context;

    public CustomListView(Activity context, ArrayList<String> urls, ArrayList<String> usernames) {
        super(context, R.layout.listview, usernames);

        this.urls = urls;
        this.usernames = usernames;
        this.context = context;

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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

        Picasso.with(r.getContext())
                .load(urls.get(position))
                .resize(50, 50)
                .centerCrop()
                .into(viewHolder.profilePictureView);

        viewHolder.textView.setText(usernames.get(position));

        return r;
    }

    class ViewHolder {
        TextView textView;
        ImageView profilePictureView;

        ViewHolder(View v) {
            textView = v.findViewById(R.id.usernameTextView);
            profilePictureView = v.findViewById(R.id.profilePicture);
        }
    }
}


