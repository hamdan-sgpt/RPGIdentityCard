package com.rpgidentity.model;

import java.util.UUID;

public class IdentityData {

    private final UUID uuid;
    private String idNumber;
    private String nama;
    private int umur;
    private String profesi;
    private Race race;
    private String signatureHash;
    private boolean registered;
    private int customModelData;

    public IdentityData(UUID uuid, String defaultName) {
        this.uuid = uuid;
        this.idNumber = null;
        this.nama = defaultName;
        this.umur = 20;
        this.profesi = "Lumberjack";
        this.race = Race.HUMAN;
        this.signatureHash = null;
        this.registered = false;
        this.customModelData = 0;
    }

    public UUID getUuid() { return uuid; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public int getUmur() { return umur; }
    public void setUmur(int umur) { this.umur = Math.max(1, umur); }

    public String getProfesi() { return profesi; }
    public void setProfesi(String profesi) { this.profesi = profesi; }

    public Race getRace() { return race; }
    public void setRace(Race race) { this.race = race != null ? race : Race.HUMAN; }

    public String getSignatureHash() { return signatureHash; }
    public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }

    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }

    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }
}
