package com.chikli.hudson.plugin.naginator.pipeline;

import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.chikli.hudson.plugin.naginator.NaginatorListener;
import com.chikli.hudson.plugin.naginator.ProgressiveDelay;
import com.chikli.hudson.plugin.naginator.ScheduleDelay;
import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * {@link Step} to mark a build to be rescheduled by {@link NaginatorListener}.
 * Be aware that you have to add this step to the parent build if the build is
 * the child of another build (e.g. multi-configuration projects).
 */
public class RetryExtStep extends Step {

    @DataBoundConstructor
    public RetryExtStep() {
        super();
    }
    
    private int retry;

    public int getRetry() { return this.retry; }

    @DataBoundSetter
    public void setRetry(int retry) {
        this.retry = retry;
    }

    private int maxSchedule;

    public int getMaxSchedule() { return this.maxSchedule; }

    @DataBoundSetter
    public void setMaxSchedule(int maxSchedule) {
        this.maxSchedule = maxSchedule;
    }

    private boolean rerunIfUnstable;

    public boolean isRerunIfUnstable() { return this.rerunIfUnstable; }

    @DataBoundSetter
    public void setRerunIfUnstable(boolean rerunIfUnstable) {
        this.rerunIfUnstable = rerunIfUnstable;
    }

    private boolean checkRegexp;

    public boolean isCheckRegexp() { return this.checkRegexp; }

    @DataBoundSetter
    public void setCheckRegexp(boolean checkRegexp) {
        this.checkRegexp = checkRegexp;
    }

    @CheckForNull
    private ScheduleDelay delay;

    @Nonnull
    public ScheduleDelay getDelay() { return this.delay; }

    @DataBoundSetter
    public void setDelay(ScheduleDelay delay) {
        if (delay == null) {
            delay = new ProgressiveDelay(5 * 60, 3 * 60 * 60);
        } else {
            this.delay = delay;
        }
    }

    @CheckForNull
    private String regexpForRerun;

    @CheckForNull
    public String getRegexpForRerun() {
        return this.regexpForRerun;
    }

    @DataBoundSetter
    public void setRegexpForRerun(@CheckForNull String regexpForRerun) {
        this.regexpForRerun = Util.fixNull(regexpForRerun);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        Jenkins j = Jenkins.getInstanceOrNull();
        if(j != null) {
            return j.getDescriptorByType(DescriptorImpl.class);
        }
        return null;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new RetryExtStepExecution(this, context);
    }

    @Extension
    public final static NaginatorListener LISTENER = new NaginatorListener();

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "retryExt";
        }

        @Override
        public String getDisplayName() {
            return "Retry the body based on configuration";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class);
        }
    }
}
