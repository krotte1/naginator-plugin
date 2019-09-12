package com.chikli.hudson.plugin.naginator.pipeline;

import com.chikli.hudson.plugin.naginator.NaginatorPublisherScheduleAction;
import com.chikli.hudson.plugin.naginator.ScheduleDelay;
import com.google.inject.Inject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

/**
 * {@link Step} to mark a build to be rescheduled by {@link NaginatorListener}.
 * Be aware that you have to add this step to the parent build
 * if the build is the child of another build (e.g. multi-configuration projects).
 */
public class RetryExtStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
   
    private static final long serialVersionUID = 1L;

    @StepContextParameter
    private transient Run<?, ?> build;

    @StepContextParameter
    private transient TaskListener taskListener;

    @Inject
    private transient RetryExtStep step;

    public int getRetry() { return step.getRetry(); }
    public int getMaxSchedule() { return step.getMaxSchedule(); }
    public String getRegexpForRerun() { return step.getRegexpForRerun(); }
    public boolean isRerunIfUnstable() { return step.isRerunIfUnstable(); }
    public ScheduleDelay getDelay() { return step.getDelay(); }
    public boolean isCheckRegexp() { return step.isCheckRegexp(); }

    @Override 
    public Void run() throws Exception {
        build.addAction(new NaginatorPublisherScheduleAction(this));
        return null;
    }
}
