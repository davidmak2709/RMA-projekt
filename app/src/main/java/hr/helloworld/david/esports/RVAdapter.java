package hr.helloworld.david.esports;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder>{

    private List<Event> events;

    RVAdapter(List<Event> events){
        this.events=events;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView eventSport;
        TextView eventOwner;
        TextView eventTime;
        ImageView eventPhoto;

        EventViewHolder(View itemView){
            super(itemView);
            cv=itemView.findViewById(R.id.eventHolder);
            eventSport=itemView.findViewById(R.id.eventSport);
            eventOwner=itemView.findViewById(R.id.eventOwner);
            eventTime=itemView.findViewById(R.id.eventTime);
            //eventPhoto=itemView.findViewById(R.id.eventPhoto);
        }
    }

    @Override
    public int getItemCount(){
        return events.size();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder eventViewHolder, int i){
        eventViewHolder.eventOwner.setText(events.get(i).getNaslov());
        eventViewHolder.eventSport.setText(events.get(i).getSport());
        eventViewHolder.eventTime.setText(events.get(i).getmTime().toString());
        //eventViewHolder.eventPhoto.setImageResource();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
}
