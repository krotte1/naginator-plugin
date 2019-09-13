package com.chikli.hudson.plugin.naginator.pipeline;

import java.util.Set;

import com.chikli.hudson.plugin.naginator.NaginatorListener;
import com.chikli.hudson.plugin.naginator.ProgressiveDelay;
import com.chikli.hudson.plugin.naginator.ScheduleDelay;
import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * {@link Step} to mark a build to be rescheduled by {@link NaginatorListener}.
 * Be aware that you have to add this step to the parent build if the build is
 * the child of another build (e.g. multi-configuration projects).
 */
public class RetryExtStep extends Step {

    @DataBoundSetter
    public int retry;

    @DataBoundSetter
    public int maxSchedule;

    @DataBoundSetter
    public String regexpForRerun;

    @DataBoundSetter
    public boolean rerunIfUnstable;

    @DataBoundSetter
    public boolean checkRegexp;

    private ScheduleDelay delay;

    public ScheduleDelay getDelay() {
        return this.delay;
    }

    @DataBoundSetter
    public void setDelay(ScheduleDelay delay) {
        if (delay == null) {
            delay = new ProgressiveDelay(5 * 60, 3 * 60 * 60);
        } else {
            this.delay = delay;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return Jenkins.get().getDescriptorByType(DescriptorImpl.class);
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

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new RetryExtStepExecution(this, context);
    }
}
