package io.vertx.ext.sql.assist;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MySQLClientTest {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		MySQLConnectOptions options = new MySQLConnectOptions();
		options.setPort(3306).setHost("localhost").setDatabase("root").setUser("root").setPassword("root");
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		MySQLPool client = MySQLPool.pool(vertx, options, poolOptions);

		MySQLSQL sql = new MySQLSQL(SQLExecute.create(client));
		User user = new User();
		user.setName("name");
		user.setPwd("pwd");
		sql.insertNonEmpty(user, res -> {
			if (res.succeeded()) {
				System.out.println("insert: " + res.result());
			} else {
				System.out.println(res.cause());
			}
		});

		sql.getCount(res -> {
			if (res.succeeded()) {
				System.out.println("count: " + res.result());
			} else {
				System.out.println(res.cause());
			}
		});
		sql.selectAll(new SqlAssist().setStartRow(10).setRowSize(5), res -> {
			if (res.succeeded()) {
				System.out.println(res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		vertx.setTimer(3000, tid -> System.exit(0));
	}
}
