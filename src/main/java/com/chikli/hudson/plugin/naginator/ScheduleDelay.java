package com.chikli.hudson.plugin.naginator;

import java.util.ArrayList;
import java.util.List;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.*;
import jenkins.model.Jenkins;


/**
 * Defines schedules policy to trigger a new build after failure
 * @author: <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public abstract class ScheduleDelay extends AbstractDescribableImpl<ScheduleDelay> implements ExtensionPoint {

    public abstract int computeScheduleDelay(@SuppressWarnings("rawtypes") Run failedBuild);

    @Override
    public ScheduleDelayDescriptor getDescriptor() {
        return (ScheduleDelayDescriptor) super.getDescriptor();
    }

    public static abstract class ScheduleDelayDescriptor extends Descriptor<ScheduleDelay> {
        public static DescriptorExtensionList<ScheduleDelay, ScheduleDelayDescriptor> all() {
            Jenkins j = Jenkins.getInstanceOrNull();
            if(j != null) {
                return j.getDescriptorList(ScheduleDelay.class);
            }
            return null;
        }

        public static List<ScheduleDelayDescriptor> getApplicableDescriptors(Class<?> clazz) {
            List<ScheduleDelayDescriptor> result = new ArrayList<>();
            List<ScheduleDelayDescriptor> list = all();
            for (ScheduleDelayDescriptor isd : list) {
                if (isd.isApplicable(clazz)) {
                    result.add(isd);
                }
            }
            return result;
        }
        
        public abstract boolean isApplicable(Class<?> clazz);   
    }
}
