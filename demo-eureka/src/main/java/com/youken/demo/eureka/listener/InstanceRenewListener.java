package com.youken.demo.eureka.listener;

import com.netflix.appinfo.InstanceInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author 杨剑
 * @date 2018-12-25
 */
@Configuration
@Slf4j
public class InstanceRenewListener implements ApplicationListener<EurekaInstanceRenewedEvent> {

	@Override
	public void onApplicationEvent(@NonNull EurekaInstanceRenewedEvent event) {
		InstanceInfo info = event.getInstanceInfo();
		log.info("心跳检测：{}/{}:{}", info.getAppName(), info.getIPAddr(), info.getPort());
	}
}
