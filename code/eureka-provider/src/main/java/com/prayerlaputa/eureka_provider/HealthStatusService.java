package com.prayerlaputa.eureka_provider;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/6/21
 */
@Service
public class HealthStatusService implements HealthIndicator {

    private Boolean status = true;


    public void setStatus(Boolean status) {
        this.status  = status;
    }


    @Override
    public Health health() {
        if(status) {
            return new Health.Builder().up().build();
        } else {
            return new Health.Builder().down().build();
        }
    }

    public String getStatus() {
        return status.toString();
    }
}
