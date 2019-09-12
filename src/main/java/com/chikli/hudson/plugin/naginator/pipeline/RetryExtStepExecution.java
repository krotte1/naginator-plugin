package com.chikli.hudson.plugin.naginator.pipeline;

import com.chikli.hudson.plugin.naginator.NoChildStrategy;
import com.chikli.hudson.plugin.naginator.ScheduleDelay;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    public int getRetry() { return step.retry; }
    public int getMaxSchedule() { return step.maxSchedule; }
    public String getRegexpForRerun() { return step.regexpForRerun; }
    public boolean isRerunIfUnstable() { return step.rerunIfUnstable; }
    public ScheduleDelay getDelay() { return step.delay; }
    public boolean isCheckRegexp() { return step.checkRegexp; }

    @Override 
    public Void run() throws Exception {
        build.addAction(new NaginatorPublisherScheduleAction(this));
    }
}
