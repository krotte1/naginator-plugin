package com.chikli.hudson.plugin.naginator.pipeline;

import com.chikli.hudson.plugin.naginator.NaginatorPublisherScheduleAction;
import com.chikli.hudson.plugin.naginator.ScheduleDelay;
import hudson.model.Run;
import javax.inject.Inject;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

/**
 * {@link Step} to mark a build to be rescheduled by {@link NaginatorListener}.
 * Be aware that you have to add this step to the parent build if the build is
 * the child of another build (e.g. multi-configuration projects).
 */
public class RetryExtStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private static final long serialVersionUID = 1L;

    @Inject
    private transient RetryExtStep step;

    protected RetryExtStepExecution(RetryExtStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    public int getRetry() { return step.getRetry(); }
    public int getMaxSchedule() { return step.getMaxSchedule(); }
    public String getRegexpForRerun() { return step.getRegexpForRerun(); }
    public boolean isRerunIfUnstable() { return step.isRerunIfUnstable(); }
    public ScheduleDelay getDelay() { return step.getDelay(); }
    public boolean isCheckRegexp() { return step.isCheckRegexp(); }

    @Override 
    public Void run() throws Exception {
        getContext().get(Run.class).addAction(new NaginatorPublisherScheduleAction(this));
        return null;
    }
}
