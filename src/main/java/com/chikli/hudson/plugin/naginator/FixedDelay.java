package com.chikli.hudson.plugin.naginator;

import javax.annotation.Nonnull;

import com.chikli.hudson.plugin.naginator.ScheduleDelay;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Run;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author: <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class FixedDelay extends ScheduleDelay {

    private int delay;

    @DataBoundConstructor
    public FixedDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public int computeScheduleDelay(@SuppressWarnings("rawtypes") Run failedBuild) {
        return delay;
    }
    
    @Extension @Symbol("fixed")
    public static class DescriptorImpl extends ScheduleDelayDescriptor {
        @Override
        public boolean isApplicable(Class<?> clazz) {
            return AbstractItem.class.isAssignableFrom(clazz);
        }
        
        @Override
        @Nonnull
        public String getDisplayName() {
            return "Fixed";
        }
    }
}
