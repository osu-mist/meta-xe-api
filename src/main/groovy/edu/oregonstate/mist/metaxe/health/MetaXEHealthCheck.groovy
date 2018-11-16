package edu.oregonstate.mist.metaxe.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

class MetaXEHealthCheck extends HealthCheck {
    String xeAppsFilePath

    MetaXEHealthCheck(String xeAppsFilePath) {
        this.xeAppsFilePath = xeAppsFilePath
    }

    @Override
    protected Result check() throws Exception {
        File xeAppsFile
        try {
            xeAppsFile = new File(xeAppsFilePath)
        } catch (NullPointerException ignore) {
            // This shouldn't happen since a null xeAppsFilePath property in the configuration
            // file should prevent the run task from succeeding
            return Result.unhealthy("xeAppsFilePath is null")
        }
        if (xeAppsFile.exists()) {
            Result.healthy()
        } else {
            Result.unhealthy("xeAppsFilePath: ${xeAppsFilePath} does not exist")
        }
    }
}
