package com.chikli.hudson.plugin.naginator;

import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link Cause} for builds triggered by this plugin.
 * @author Alan.Harder@sun.com
 */
public class NaginatorCause extends Cause {

    private final String summary;
    private final Integer sourceBuildNumber;
    @SuppressWarnings("rawtypes")
    private transient Job project;

    public NaginatorCause(Run<?, ?> build) {
        this.summary = build.getDisplayName();
        this.sourceBuildNumber = build.getNumber();
    }

    @Override
    public String getShortDescription() {
        return Messages.NaginatorCause_Description(summary);
    }

    @Override
    public void onAddedTo(@SuppressWarnings("rawtypes") @Nonnull Run build) {
        super.onAddedTo(build);
        this.project = build.getParent();
    }

    @Override
    public void onLoad(@Nonnull Run<?,?> build) {
        super.onLoad(build);
        this.project = build.getParent();
    }

    public String getSummary() { return this.summary; }

    @SuppressWarnings("rawtypes")
    public Job getProject() { return this.project; }

    public String getJobUrl() {
        return this.project.getUrl();
    }

    public Integer getSourceBuildNumber() { return this.sourceBuildNumber; }

    /**
     * @return the source build. <code>null</code> when the build is deleted.
     * @since 1.18
     */
    @CheckForNull
    public Run<?, ?> getSourceBuild() {
        if (getSourceBuildNumber() == null) {
            return null;
        }
        return getProject().getBuildByNumber(getSourceBuildNumber());
    }
}
