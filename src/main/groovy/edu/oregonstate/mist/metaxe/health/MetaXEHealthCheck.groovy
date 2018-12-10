package edu.oregonstate.mist.metaxe.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import edu.oregonstate.mist.metaxe.Attributes
import edu.oregonstate.mist.metaxe.XEAppDAO
import edu.oregonstate.mist.metaxe.XeParsingException

class MetaXEHealthCheck extends HealthCheck {
    String xeAppsFilePath

    MetaXEHealthCheck(String xeAppsFilePath) {
        this.xeAppsFilePath = xeAppsFilePath
    }

    @Override
    protected Result check() throws Exception {
        try {
            File xeAppsFile = new File(xeAppsFilePath)
            if (!xeAppsFile.exists()) {
                return Result.unhealthy("xeAppsFilePath: ${xeAppsFilePath} does not exist")
            }
            Map<String, Attributes> allAttributes = XEAppDAO.getAllAttributes(xeAppsFile)
            if (allAttributes.isEmpty()) {
                return Result.unhealthy("No data found in xeAppsFile")
            }
        } catch (NullPointerException ignore) {
            // This shouldn't happen since a null xeAppsFilePath property in the configuration
            // file should prevent the run task from succeeding
            return Result.unhealthy("xeAppsFilePath is null")
        } catch (XeParsingException e) {
            return Result.unhealthy(e)
        } catch (Exception e) {
            return Result.unhealthy(e)
        }
        Result.healthy()
    }
}
