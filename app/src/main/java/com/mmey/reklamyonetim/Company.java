//170201088 M.M.Enes YURTSEVER

package com.mmey.reklamyonetim;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Company implements Parcelable {

    private String companyID;
    private String companyName;
    private String companyCategory;
    private String companyLocationLat;
    private String companyLocationLong;
    private String campaignContent;
    private String campaignExpireDate;

    public Company() {

    }

    public Company(String companyName,String companyCategory, String companyLocationLat, String companyLocationLong, String campaignContent, String campaignExpireDate) {
        this.companyName = companyName;
        this.companyCategory = companyCategory;
        this.companyLocationLat = companyLocationLat;
        this.companyLocationLong = companyLocationLong;
        this.campaignContent = campaignContent;
        this.campaignExpireDate = campaignExpireDate;
    }

    protected Company(Parcel in) {
        companyID = in.readString();
        companyName = in.readString();
        companyCategory = in.readString();
        companyLocationLat = in.readString();
        companyLocationLong = in.readString();
        campaignContent = in.readString();
        campaignExpireDate = in.readString();
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

    @Exclude
    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCategory() {
        return companyCategory;
    }

    public void setCompanyCategory(String companyCategory) {
        this.companyCategory = companyCategory;
    }

    public String getCompanyLocationLat() {
        return companyLocationLat;
    }

    public void setCompanyLocationLat(String companyLocationLat) {
        this.companyLocationLat = companyLocationLat;
    }

    public String getCompanyLocationLong() {
        return companyLocationLong;
    }

    public void setCompanyLocationLong(String companyLocationLong) {
        this.companyLocationLong = companyLocationLong;
    }

    public String getCampaignContent() {
        return campaignContent;
    }

    public void setCampaignContent(String campaignContent) {
        this.campaignContent = campaignContent;
    }

    public String getCampaignExpireDate() {
        return campaignExpireDate;
    }

    public void setCampaignExpireDate(String campaignExpireDate) {
        this.campaignExpireDate = campaignExpireDate;
    }

    @Override
    public String toString() {
        return "Company{" +
                "companyID='" + companyID + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyCategory='" + companyCategory + '\'' +
                ", companyLocationLat='" + companyLocationLat + '\'' +
                ", companyLocationLong='" + companyLocationLong + '\'' +
                ", campaignContent='" + campaignContent + '\'' +
                ", campaignExpireDate='" + campaignExpireDate + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(companyID);
        dest.writeString(companyName);
        dest.writeString(companyCategory);
        dest.writeString(companyLocationLat);
        dest.writeString(companyLocationLong);
        dest.writeString(campaignContent);
        dest.writeString(campaignExpireDate);
    }
}
