package com.example.consumer.config;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.core.client.ClusterTopologyListener;
import org.apache.activemq.artemis.api.core.client.TopologyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableJms
@Configuration
public class JmsConfig {

  @Value("${artemis.live.host:localhost}") private String liveHost;
  @Value("${artemis.live.port:61616}") private int livePort;
  @Value("${artemis.backup.host:localhost}") private String backupHost;
  @Value("${artemis.backup.port:61617}") private int backupPort;
  @Value("${spring.artemis.user:admin}") private String user;
  @Value("${spring.artemis.password:admin}") private String password;

  @Value("${app.client-id:overflow-consumer-service}")
  private String clientId;

  private TransportConfiguration liveTransport() {
    Map<String,Object> p = new HashMap<>();
    p.put(TransportConstants.HOST_PROP_NAME, liveHost);
    p.put(TransportConstants.PORT_PROP_NAME, livePort);
    return new TransportConfiguration(NettyConnectorFactory.class.getName(), p);
  }

  private TransportConfiguration backupTransport() {
    Map<String,Object> p = new HashMap<>();
    p.put(TransportConstants.HOST_PROP_NAME, backupHost);
    p.put(TransportConstants.PORT_PROP_NAME, backupPort);
    return new TransportConfiguration(NettyConnectorFactory.class.getName(), p);
  }

  @Bean(destroyMethod = "close")
  public ServerLocator serverLocator() throws Exception {
    ServerLocator locator = ActiveMQClient.createServerLocatorWithHA(liveTransport(), backupTransport());
    locator.setReconnectAttempts(-1);
    locator.setInitialConnectAttempts(-1);
    locator.setRetryInterval(500);
    locator.setRetryIntervalMultiplier(1.0);
    locator.addClusterTopologyListener(new ClusterTopologyListener() {
      private final Logger log = LoggerFactory.getLogger("BrokerTopology");
      @Override public void nodeUP(TopologyMember m, boolean last) {
        log.info("NODE UP primary={} backup={} id={}", fmt(m.getPrimary()), fmt(m.getBackup()), m.getNodeId());
      }
      @Override public void nodeDown(long uid, String nodeId) { log.warn("NODE DOWN id={}", nodeId); }
      private String fmt(TransportConfiguration tc) {
        if (tc == null) return "none";
        Map<String,Object> p = tc.getParams();
        return p.get(TransportConstants.HOST_PROP_NAME)+":"+p.get(TransportConstants.PORT_PROP_NAME);
      }
    });
    return locator;
  }

  @Bean(destroyMethod = "close")
  public ActiveMQConnectionFactory connectionFactory(ServerLocator locator) throws Exception {
    ActiveMQConnectionFactory f = new ActiveMQConnectionFactory(locator);
    f.setUser(user);
    f.setPassword(password);
    f.setClientID(clientId);
    f.setEnableSharedClientID(true);
    return f;
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory cf) {
    DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
    f.setConnectionFactory(cf);
    f.setSessionTransacted(true);
    f.setRecoveryInterval(1000L);
    f.setConcurrency("1-1");
    // The broker role we're using cannot auto-create durable queues, and this consumer only needs
    // standard queue semantics. Keep the client ID stable but avoid durable subscriptions so the
    // container doesn't try to CREATE_DURABLE_QUEUE on startup.
//    f.setClientId(clientId);
    f.setSubscriptionDurable(false);
    return f;
  }
}
