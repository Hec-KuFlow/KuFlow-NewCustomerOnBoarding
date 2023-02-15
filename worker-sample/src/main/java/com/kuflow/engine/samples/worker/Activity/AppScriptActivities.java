/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.Activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface AppScriptActivities {
    String appScriptRun(String client, String project);
}
