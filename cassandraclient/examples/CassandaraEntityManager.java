package com.netflix.astyanax.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.connectionpool.impl.SmaLatencyScoreStrategyImpl;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * Example code for demonstrating how to access Cassandra using Astyanax and CQL3.
 * 
 * @author fsheriff
 * 
 */
public class CassandaraEntityManager {
  private static final Logger logger = LoggerFactory.getLogger(CassandaraEntityManager.class);
  
  private AstyanaxContext<Keyspace> context;
  private Keyspace keyspace;

  private String keySpaceName;
 
  
  public Keyspace getKeyspace() {
	return keyspace;
}



public String getKeySpaceName() {
	return keySpaceName;
}

public void setKeySpaceName(String keySpaceName) {
	this.keySpaceName = keySpaceName;
}

public CassandaraEntityManager(){
	  init();
  }

  private void init() {
    logger.debug("init()");
    ConnectionPoolConfigurationImpl poolConfig = new ConnectionPoolConfigurationImpl("MyConnectionPool")
    .setPort(9160)
    .setMaxConnsPerHost(1)
    .setSeeds("127.0.0.1:9160")
    .setLatencyAwareUpdateInterval(10000)  // Will resort hosts per token partition every 10 seconds
    .setLatencyAwareResetInterval(10000) // Will clear the latency every 10 seconds. In practice I set this to 0 which is the default. It's better to be 0.
    .setLatencyAwareBadnessThreshold(2) // Will sort hosts if a host is more than 100% slower than the best and always assign connections to the fastest host, otherwise will use round robin
    .setLatencyAwareWindowSize(100) // Uses last 100 latency samples. These samples are in a FIFO q and will just cycle themselves.
;
       poolConfig.setLatencyScoreStrategy(new SmaLatencyScoreStrategyImpl()); // Enabled SMA.  Omit this to use round robin with a token range


    
    context = new AstyanaxContext.Builder()
    .forCluster("Test Cluster")
    .forKeyspace(keySpaceName)
    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()      
        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE).setConnectionPoolType(ConnectionPoolType.TOKEN_AWARE)
    )
    .withConnectionPoolConfiguration(poolConfig/*new ConnectionPoolConfigurationImpl("MyConnectionPool")
        .setPort(9160)
        .setMaxConnsPerHost(1)
        .setSeeds("127.0.0.1:9160")*/
    )
    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()      
        .setCqlVersion("3.0.0")
        .setTargetCassandraVersion("1.2"))
    .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
    .buildKeyspace(ThriftFamilyFactory.getInstance());

    context.start();
    keyspace = context.getEntity();
    
   
  }
  
  



}
