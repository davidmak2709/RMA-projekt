package hr.helloworld.david.esports;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;


public class Event {

    private String mId;       //Naslov



    public String naslov;
    private double mLat;
    private double mLng;
    private float mRadius;
    private long mDuration; //trajanje geofencea
    private int mNumId;        // br eventa

    private int mSize;      //max broj mjesta
    private int mGooing;    //br dolazaka
    private String mSport;
    private String mOwner;
    private Date mTime; //vrijeme eventa

    public Event() {

    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }
    public void setId(String mId) {
        this.mId = mId;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public void setNumId(int mNumId) {
        this.mNumId = mNumId;
    }

    public void setSize(int mSize) {
        this.mSize = mSize;
    }

    public void setGooing(int mGooing) {
        this.mGooing = mGooing;
    }

    public void setSport(String mSport) {
        this.mSport = mSport;
    }

    public void setOwner(String mOwner) {
        this.mOwner = mOwner;
    }

    public void setTime(Date mTime) {
        this.mTime = mTime;
    }


    public String getId() {
        return mId;
    }

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public float getRadius() {
        return mRadius;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getNumId() {
        return mNumId;
    }

    public int getSize() {
        return mSize;
    }

    public int getGooing() {
        return mGooing;
    }

    public String getSport() {
        return mSport;
    }

    public String getOwner() {
        return mOwner;
    }

    public Date getmTime() {
        return mTime;
    }

    public Event(String naslov,String mId, LatLng mLatLng, float mRadius, long mDuration, int mNumId, int mSize, int mGooing, String mSport, String mOwner, Date mTime) {
        this.mId = mId;
        this.mLat = mLatLng.latitude;
        this.mLng = mLatLng.longitude;
        this.mRadius = mRadius;
        this.mDuration = mDuration;
        this.mNumId = mNumId;
        this.mSize = mSize;
        this.mGooing = mGooing;
        this.mSport = mSport;
        this.mOwner = mOwner;
        this.mTime = mTime;
        this.naslov = naslov;
    }

    public Event(String naslov,String mId, LatLng mLatLng, float mRadius, long mDuration, int mSize, int mGooing, String mSport, String mOwner, Date mTime) {
        this.mId = mId;
        this.mLat = mLatLng.latitude;
        this.mLng = mLatLng.longitude;
        this.mRadius = mRadius;
        this.mDuration = mDuration;
        this.mSize = mSize;
        this.mGooing = mGooing;
        this.mSport = mSport;
        this.mOwner = mOwner;
        this.mTime = mTime;
        this.naslov = naslov;
    }

    @Override
    public String toString() {
        return "Event{" +
                "mId='" + mId + '\'' +
                ", naslov='" + naslov + '\'' +
                ", mLat=" + mLat +
                ", mLng=" + mLng +
                ", mRadius=" + mRadius +
                ", mDuration=" + mDuration +
                ", mNumId=" + mNumId +
                ", mSize=" + mSize +
                ", mGooing=" + mGooing +
                ", mSport='" + mSport + '\'' +
                ", mOwner='" + mOwner + '\'' +
                ", mTime=" + mTime +
                '}';
    }
}
