package com.gougou.fanimgpickerlibrary.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class LGCompressServiceParam implements Parcelable {

    private int outWidth;
    private int outHeight;
    private int maxFileSize;
    private String srcImageUri;

    public LGCompressServiceParam() {
    }

    protected LGCompressServiceParam(Parcel in) {
        outWidth = in.readInt();
        outHeight = in.readInt();
        maxFileSize = in.readInt();
        srcImageUri = in.readString();
    }

    public static final Creator<LGCompressServiceParam> CREATOR = new Creator<LGCompressServiceParam>() {
        @Override
        public LGCompressServiceParam createFromParcel(Parcel in) {
            return new LGCompressServiceParam(in);
        }

        @Override
        public LGCompressServiceParam[] newArray(int size) {
            return new LGCompressServiceParam[size];
        }
    };

    public int getOutWidth() {
        return outWidth;
    }

    public void setOutWidth(int outWidth) {
        this.outWidth = outWidth;
    }

    public int getOutHeight() {
        return outHeight;
    }

    public void setOutHeight(int outHeight) {
        this.outHeight = outHeight;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getSrcImageUri() {
        return srcImageUri;
    }

    public void setSrcImageUri(String srcImageUri) {
        this.srcImageUri = srcImageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(outWidth);
        dest.writeInt(outHeight);
        dest.writeInt(maxFileSize);
        dest.writeString(srcImageUri);
    }
}