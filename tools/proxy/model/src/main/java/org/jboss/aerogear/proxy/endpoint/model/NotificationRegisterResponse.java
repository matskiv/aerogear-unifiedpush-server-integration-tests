package org.jboss.aerogear.proxy.endpoint.model;

import java.util.List;

import com.google.gson.Gson;

/**
 *
 * @author <a href="mailto:miklosovic@gmail.com">Stefan Miklosovic</a>
 */
public class NotificationRegisterResponse {

    private List<ApnsNotification> apnsNotifications;

    private List<GCMNotification> gcmNotifications;

    public List<ApnsNotification> getApnsNotifications() {
        return apnsNotifications;
    }

    public void setApnsNotifications(List<ApnsNotification> apnsNotifications) {
        this.apnsNotifications = apnsNotifications;
    }

    public List<GCMNotification> getGcmNotifications() {
        return gcmNotifications;
    }

    public void setGcmNotifications(List<GCMNotification> gcmNotifications) {
        this.gcmNotifications = gcmNotifications;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
