package io.vertx.ext.sql.assist;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;

public class JdbcPoolTest extends TestSuite<User, JDBCPool> {
	public JdbcPoolTest(UserJdbcSQL sql, JsonObject config) {
		super(sql, config);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		JsonObject config = new JsonObject();
		// config.put("url",
		// "jdbc:mysql://10.0.0.79:3306/orion_api_manager?useUnicode=true&useSSL=false&serverTimezone=UTC")
		// .put("driver_class", "com.mysql.cj.jdbc.Driver").put("user",
		// "root").put("password", "root");
		config.put("url", String.format("jdbc:sqlite:%s/data/sqlite.db", System.getProperty("user.dir"))).put("driver_class",
				"org.sqlite.JDBC");
		JDBCPool jdbcPool = JDBCPool.pool(vertx, config);
		SQLExecute<JDBCPool> execute = SQLExecute.createJDBC(jdbcPool);
		UserJdbcSQL sql = new UserJdbcSQL(execute);
		JdbcPoolTest test = new JdbcPoolTest(sql, config);
		test.runTest(res -> {
			if (res.succeeded()) {
				System.out.println("succeeded:" + res.result());
			} else {
				System.err.println("failed:" + res.cause());
			}
			System.exit(0);
		});
	}
}
