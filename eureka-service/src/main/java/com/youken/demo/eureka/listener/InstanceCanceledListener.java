package com.youken.demo.eureka.listener;

import com.netflix.appinfo.InstanceInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author 杨剑
 * @date 2018-12-25
 */
@Configuration
@Slf4j
public class InstanceCanceledListener implements ApplicationListener<EurekaInstanceRegisteredEvent> {

	@Override
	public void onApplicationEvent(@NonNull EurekaInstanceRegisteredEvent event) {
		InstanceInfo info = event.getInstanceInfo();
		log.info("服务取消：{}/{}:{}", info.getAppName(), info.getIPAddr(), info.getPort());
	}
}
