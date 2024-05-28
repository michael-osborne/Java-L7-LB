package com.utopiarealized.bugbounty.proxy.model;

import java.util.Date;

public class SpyContext {

    private String bountyName;
    private String directory;
    private Date startupTime;

    public SpyContext(final String bountyName, final String directory) {
        this.bountyName = bountyName;
        this.directory = directory;
        this.startupTime = new Date();
    }

    public String getBountyName() {
        return bountyName;
    }

    public void setBountyName(String bountyName) {
        this.bountyName = bountyName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Date getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(Date startupTime) {
        this.startupTime = startupTime;
    }
}
