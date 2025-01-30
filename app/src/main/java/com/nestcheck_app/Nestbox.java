package com.nestcheck_app;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Nestbox implements Parcelable {
    private int nestboxNumber;
    private String nestProgress = "default";

    private int eggsOrChics = 0;
    private boolean sampled = false;
    private String eggCoordinateCharacter = "";
    private String eggCoordinateNumber = "";

    private String spottedBird ="";

    public String getSpottedBird() {
        return spottedBird;
    }

    public void setSpottedBird(String spottedBird) {
        this.spottedBird = spottedBird;
    }

    public Nestbox(int nestboxNumber, String nestProgress, int eggsOrChics, boolean sampled, String eggCoordinateCharacter, String eggCoordinateNumber) {
        this.nestboxNumber = nestboxNumber;
        this.nestProgress = nestProgress;
        this.eggsOrChics = eggsOrChics;
        this.sampled = sampled;
        this.eggCoordinateCharacter = eggCoordinateCharacter;
        this.eggCoordinateNumber = eggCoordinateNumber;
    }
    public void setEggCoordinateCharacter(String eggCoordinateCharacter){ this.eggCoordinateCharacter = eggCoordinateCharacter; }

    public String getEggCoordinateCharacter() {
        return eggCoordinateCharacter;
    }

    public void setEggCoordinateNumber(String eggCoordinateNumber) {
        this.eggCoordinateNumber = eggCoordinateNumber;
    }

    public String getEggCoordinateNumber() {
        return eggCoordinateNumber;
    }

    public Nestbox(int nestboxNumber) {
        this.nestboxNumber = nestboxNumber;
    }

    public int getNestboxNumber() {
        return nestboxNumber;
    }

    public void setNestboxNumber(int nestboxNumber) {
        this.nestboxNumber = nestboxNumber;
    }

    public String getNestProgress() {
        return nestProgress;
    }

    public void setNestProgress(String nestProgress) {
        this.nestProgress = nestProgress;
    }

    public int getEggsOrChics() {
        return eggsOrChics;
    }

    public void setEggsOrChics(int eggsOrChics) {
        this.eggsOrChics = eggsOrChics;
    }

    public boolean isSampled() {
        return sampled;
    }

    public void setSampled(boolean sampled) {
        this.sampled = sampled;
    }

    public Nestbox(){
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(nestboxNumber);
        dest.writeString(nestProgress);
        dest.writeInt(eggsOrChics);
        dest.writeByte((byte) (sampled ? 1:0));
        dest.writeString(eggCoordinateCharacter);
        dest.writeString(eggCoordinateNumber);
    }

    protected Nestbox (Parcel in){
        nestboxNumber = in.readInt();
        nestProgress = in.readString();
        eggsOrChics = in.readInt();
        sampled = in.readByte() != 0;
        eggCoordinateCharacter = in.readString();
        eggCoordinateNumber = in.readString();
    }

    public static final Creator<Nestbox> CREATOR = new Creator<Nestbox>() {
        @Override
        public Nestbox createFromParcel(Parcel in) {
            return new Nestbox(in);
        }

        @Override
        public Nestbox[] newArray(int size) {
            return new Nestbox[size];
        }
    };
}
