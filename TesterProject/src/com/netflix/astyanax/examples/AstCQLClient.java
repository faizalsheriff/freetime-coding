package com.netflix.astyanax.examples;

import static com.netflix.astyanax.examples.ModelConstants.*;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fanho.media.movies.beans.Movies;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.connectionpool.impl.SmaLatencyScoreStrategyImpl;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * Example code for demonstrating how to access Cassandra using Astyanax and CQL3.
 * 
 * @author elandau
 * @author Marko Asplund
 */
public class AstCQLClient {
  private static final Logger logger = LoggerFactory.getLogger(AstCQLClient.class);
  
  private AstyanaxContext<Keyspace> context;
  private Keyspace keyspace;
  private ColumnFamily<Integer, String> EMP_CF;
  private ColumnFamily<String, Object> EMP_MOVIES;
  private static final String EMP_CF_NAME = "xeros";
  private static final String INSERT_STATEMENT =
      String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);",
          EMP_CF_NAME, COL_NAME_EMPID, COL_NAME_DEPTID, COL_NAME_FIRST_NAME, COL_NAME_LAST_NAME);
  
  private static final String INSERT_MOVIE_STATEMENT =
	      String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
	          "movies", "moviename","rating", "releaseyear", "synopsis", "criticsconsensus", "runtime", "poster_thumbnail", 
	          "poster_profile", "poster_detailed", "poster_original", "critics_rating", "audience_rating", "critics_score", 
	          "user_score");
  
  
  private static final String CREATE_STATEMENT =
      String.format("CREATE TABLE %s (%s int, %s int, %s varchar, %s varchar, PRIMARY KEY (%s, %s))",
          EMP_CF_NAME, COL_NAME_EMPID, COL_NAME_DEPTID, COL_NAME_FIRST_NAME, COL_NAME_LAST_NAME,
          COL_NAME_EMPID, COL_NAME_DEPTID);

  public void init() {
    logger.debug("init()");
    ConnectionPoolConfigurationImpl poolConfig = new ConnectionPoolConfigurationImpl("MyConnectionPool")
    .setPort(9160)
    .setMaxConnsPerHost(1)
    .setSeeds("127.0.0.1:9960")
    .setLatencyAwareUpdateInterval(10000)  // Will resort hosts per token partition every 10 seconds
    .setLatencyAwareResetInterval(10000) // Will clear the latency every 10 seconds. In practice I set this to 0 which is the default. It's better to be 0.
    .setLatencyAwareBadnessThreshold(2) // Will sort hosts if a host is more than 100% slower than the best and always assign connections to the fastest host, otherwise will use round robin
    .setLatencyAwareWindowSize(100) // Uses last 100 latency samples. These samples are in a FIFO q and will just cycle themselves.
;
       poolConfig.setLatencyScoreStrategy(new SmaLatencyScoreStrategyImpl()); // Enabled SMA.  Omit this to use round robin with a token range


    
       
         
    context =
    		new AstyanaxContext.Builder()
    .forCluster("Test Cluster")
    .forKeyspace("fanho")
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
    
    EMP_CF = ColumnFamily.newColumnFamily(
        EMP_CF_NAME, 
        IntegerSerializer.get(), 
        StringSerializer.get());
  }
  
  public void insert(int empId, int deptId, String firstName, String lastName) {
    try {
      @SuppressWarnings("unused")
      OperationResult<CqlResult<Integer, String>> result = keyspace
          .prepareQuery(EMP_CF)
              .withCql(INSERT_STATEMENT)
          .asPreparedStatement()
              .withIntegerValue(empId)
              .withIntegerValue(deptId)
              .withStringValue(firstName)
              .withStringValue(lastName)
          .execute();
    } catch (ConnectionException e) {
      logger.error("failed to write data to C*", e);
      throw new RuntimeException("failed to write data to C*", e);
    }
    logger.debug("insert ok");
  }
  
  
  
  public void insertDynamicProperties(int id, String[] ... entries) {
    MutationBatch m = keyspace.prepareMutationBatch();

    ColumnListMutation<String> clm = m.withRow(EMP_CF, id);
    for(String[] kv : entries) {
      clm.putColumn(kv[0], kv[1], null);
    }
    
    try {
      @SuppressWarnings("unused")
      OperationResult<Void> result = m.execute();
    } catch (ConnectionException e) {
      logger.error("failed to write data to C*", e);
      throw new RuntimeException("failed to write data to C*", e);
    }
    logger.debug("insert ok");
  }

  
  public void createCF() {
    logger.debug("CQL: "+CREATE_STATEMENT);
    try {
      @SuppressWarnings("unused")
      OperationResult<CqlResult<Integer, String>> result = keyspace
          .prepareQuery(EMP_CF)
          .withCql(CREATE_STATEMENT)
          .execute();
    } catch (ConnectionException e) {
      logger.error("failed to create CF", e);
      throw new RuntimeException("failed to create CF", e);
    }
  }

  public void read(int empId, int deptId) {
    logger.debug("read()");
    try {
      OperationResult<CqlResult<Integer, String>> result
        = keyspace.prepareQuery(EMP_CF)
          .withCql(String.format("SELECT * FROM %s WHERE %s=%d AND %s=%d;", EMP_CF_NAME, COL_NAME_EMPID, empId, COL_NAME_DEPTID, deptId))
          .execute();
      for (Row<Integer, String> row : result.getResult().getRows()) {
        logger.debug("row: "+row.getKey()+","+row); // why is rowKey null?
        
        ColumnList<String> cols = row.getColumns();
        System.out.println("emp");
        System.out.println("- emp id: "+cols.getIntegerValue(COL_NAME_EMPID, null));
        System.out.println("- dept: "+cols.getIntegerValue(COL_NAME_DEPTID, null));
        System.out.println("- firstName: "+cols.getStringValue(COL_NAME_FIRST_NAME, null));
        System.out.println("- lastName: "+cols.getStringValue(COL_NAME_LAST_NAME, null));
      }
    } catch (ConnectionException e) {
      logger.error("failed to read from C*", e);
      throw new RuntimeException("failed to read from C*", e);
    }
  }
  
  public static void main(String[] args) {
    logger.debug("main");
    AstCQLClient c = new AstCQLClient();
    c.init();
    //c.createCF();
    c.insert(224, 333, "Eric4", "Cartman7");
    c.read(224,333);
	  
	 CassandaraEntityManager cm = new CassandaraEntityManager();
	 cm.getKeyspace();
  }

public void saveMovie(Movies details) {
	try {
		System.out.println("Saving Movie");
	      
		@SuppressWarnings("unused")
	      OperationResult<CqlResult<String, Object>> result = keyspace
	          .prepareQuery(EMP_MOVIES)
	              .withCql(INSERT_MOVIE_STATEMENT)
	          .asPreparedStatement()
	              .withStringValue(details.getTitle())
	              .withStringValue(details.getMpaa_rating())
	              .withStringValue("12345")
	              .withStringValue(details.getSynopsis())
	              .withStringValue(details.getCritics_consensus())
	              .withStringValue("")
	              .withStringValue(details.getPosters().getThumbnail())
	              .withStringValue(details.getPosters().getProfile())
	              .withStringValue(details.getPosters().getDetailed())
	              .withStringValue(details.getPosters().getOriginal())
	              .withStringValue(details.getRatings().getCritics_rating())
	            .withStringValue(details.getRatings().getAudience_rating())
	              .withStringValue("")
	              .withStringValue("")
	              .execute();
	      
	      System.out.println(" Movie saved");
	    } catch (ConnectionException e) {
	      logger.error("failed to write data to C*", e);
	      e.printStackTrace();
	     // throw new RuntimeException("failed to write data to C*", e);
	    }
	
}

}
