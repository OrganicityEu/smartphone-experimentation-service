package gr.cti.android.experimentation.model;


public class RankingEntry {
    private long phoneId;
    private long count;

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