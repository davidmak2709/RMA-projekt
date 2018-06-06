package hr.helloworld.david.esports;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder> implements Filterable{

    private List<Event> events;
    private List<Event> eventsFiltered;
    private Location userLocation;
    private Location eventLocation=new Location("");
    private SimpleDateFormat eventTimeFormat=new SimpleDateFormat("E d.M\nHH:mm", Locale.getDefault());

    RVAdapter(List<Event> events){
        this.events=events;
        this.eventsFiltered=events;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView eventSport;
        TextView eventOwner;
        TextView eventTime;
        ImageView eventPhoto;
        TextView eventTitle;
        TextView eventGoing;
        TextView eventDistance;
        TextView eventTimeLeft;

        EventViewHolder(View itemView){
            super(itemView);
            cv=itemView.findViewById(R.id.eventHolder);
            eventSport=itemView.findViewById(R.id.eventSport);
            eventOwner=itemView.findViewById(R.id.eventOwner);
            eventTime=itemView.findViewById(R.id.eventTime);
            eventPhoto=itemView.findViewById(R.id.eventPhoto);
            eventTitle=itemView.findViewById(R.id.eventTitle);
            eventGoing=itemView.findViewById(R.id.eventGoing);
            eventDistance=itemView.findViewById(R.id.eventDistance);
            eventTimeLeft=itemView.findViewById(R.id.eventTimeLeft);
        }
    }

    @Override
    public int getItemCount(){
        return eventsFiltered.size();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventViewHolder eventViewHolder, int i){
        Event event=eventsFiltered.get(i);
        eventViewHolder.eventOwner.setText(String.format(Locale.getDefault(), "Organizira: %s", event.getOwner()));
        eventViewHolder.eventSport.setText(event.getSport());
        eventViewHolder.eventTime.setText(eventTimeFormat.format(event.getmTime()));
        eventViewHolder.eventTitle.setText(event.getNaslov());
        eventViewHolder.eventGoing.setText(String.format(Locale.getDefault(), "%d/%d", event.getGooing(), event.getSize()));
        switch(event.getSport().toLowerCase()){
            case "nogomet":
                eventViewHolder.eventPhoto.setImageResource(R.drawable.nogomet);
                break;
            case "košarka":
                eventViewHolder.eventPhoto.setImageResource(R.drawable.kosarka);
                break;
            case "rukomet":
                eventViewHolder.eventPhoto.setImageResource(R.drawable.rukomet);
                break;
            case "tenis":
                eventViewHolder.eventPhoto.setImageResource(R.drawable.tenis);
                break;
            default:
                eventViewHolder.eventPhoto.setImageResource(R.drawable.ostalo);
                break;
        }

        if (userLocation!=null) {
            eventLocation.setLatitude(event.getLat());
            eventLocation.setLongitude(event.getLng());
            float distanceInMeters=userLocation.distanceTo(eventLocation);
            eventViewHolder.eventDistance.setText(String.format(Locale.getDefault(), "Udaljenost do: %.1f km", distanceInMeters/1000));
        }
        else{
            eventViewHolder.eventDistance.setText("Udaljenost nepoznata");
        }

        long timeDiff=event.addMinutesToDate().getTime()-Calendar.getInstance().getTimeInMillis();
        timeDiff=(int) (timeDiff/1000)/60;
        if (timeDiff>0 && timeDiff<60) {
            eventViewHolder.eventTimeLeft.setText(String.format(Locale.getDefault(), "Još %d min", timeDiff));
        } else if(timeDiff>=60 && timeDiff<1440) {
            eventViewHolder.eventTimeLeft.setText(String.format(Locale.getDefault(), "Još %d h", (int) timeDiff/60));
        } else if(timeDiff>=1440 && timeDiff<43800) {
            eventViewHolder.eventTimeLeft.setText(String.format(Locale.getDefault(), "Još %d dana", (int) timeDiff/1440));
        } else if(timeDiff>=43800) {
            eventViewHolder.eventTimeLeft.setText(String.format(Locale.getDefault(), "Još %d mo", (int) timeDiff/43800));
        }

        eventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context=v.getContext();
                Intent intent=new Intent(context, DetailsActivity.class);
                intent.putExtra("id", eventsFiltered.get(eventViewHolder.getAdapterPosition()).getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void clear(){
        events.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Event> list){
        events.addAll(list);
        notifyDataSetChanged();
    }

    public void deleteItem(int index){
        events.remove(index);
        notifyItemRemoved(index);
    }

    public void setUserLocation(Location userLocation){
        this.userLocation=userLocation;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    eventsFiltered=events;
                } else {
                    List<Event> filteredList = new ArrayList<>();
                    for (Event row : events) {
                        if (row.getOwner().toLowerCase().contains(charString.toLowerCase()) || row.getSport().toLowerCase().contains(charString.toLowerCase())
                                || row.getNaslov().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    eventsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = eventsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                eventsFiltered = (ArrayList<Event>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }
}
