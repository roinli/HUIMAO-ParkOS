package com.huimao.job.core.alarm;

import com.huimao.job.domain.XxlJobInfo;
import com.huimao.job.domain.XxlJobLog;

public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
