package edu.oregonstate.mist.metaxe.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import edu.oregonstate.mist.metaxe.Attributes
import edu.oregonstate.mist.metaxe.XEAppDAO

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
            Map<String, Attributes> allAttributes
            try {
                allAttributes = XEAppDAO.getAllAttributes(xeAppsFile)
            } catch (Exception e) {
                return Result.unhealthy(e)
            }

            if (allAttributes.isEmpty()) {
                Result.unhealthy("No data found in xeAppsFile")
            } else {
                Result.healthy()
            }
        } else {
            Result.unhealthy("xeAppsFilePath: ${xeAppsFilePath} does not exist")
        }
    }
}
