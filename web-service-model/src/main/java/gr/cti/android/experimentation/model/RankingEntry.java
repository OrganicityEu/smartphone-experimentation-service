package gr.cti.android.experimentation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingEntry implements Serializable {
    private long phoneId;
    private long count;

    public RankingEntry() {
    }

    public RankingEntry(final long phoneId, final long count) {
        this.phoneId = phoneId;
        this.count = count;
    }

    public long getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(long phoneId) {
        this.phoneId = phoneId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}