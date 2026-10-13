package com.roomchatapps.Pmishra.models;

import java.util.List;

public class FamilyRankModel {
    private String familyId;
    private String familyName;
    private String familyLogo;
    private int rank;
    private long score;
    private List<String> topMemberAvatars;

    public FamilyRankModel() {
        // Required for Firebase
    }

    public FamilyRankModel(String familyId, String familyName, String familyLogo, int rank, long score, List<String> topMemberAvatars) {
        this.familyId = familyId;
        this.familyName = familyName;
        this.familyLogo = familyLogo;
        this.rank = rank;
        this.score = score;
        this.topMemberAvatars = topMemberAvatars;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyLogo() {
        return familyLogo;
    }

    public void setFamilyLogo(String familyLogo) {
        this.familyLogo = familyLogo;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public List<String> getTopMemberAvatars() {
        return topMemberAvatars;
    }

    public void setTopMemberAvatars(List<String> topMemberAvatars) {
        this.topMemberAvatars = topMemberAvatars;
    }
}
