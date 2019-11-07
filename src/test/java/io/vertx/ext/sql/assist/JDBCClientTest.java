package io.vertx.ext.sql.assist;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.assist.sql.OracleStatementSQL;

public class JDBCClientTest {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		JsonObject config = new JsonObject().put("url", "jdbc:mysql://localhost:3306/root?useUnicode=true&useSSL=false&serverTimezone=UTC")
				.put("driver_class", "com.mysql.cj.jdbc.Driver").put("password", "root").put("user", "root");
		JDBCClient jdbcClient = JDBCClient.createShared(vertx, config);
		SQLStatement.register(OracleStatementSQL.class);
		JDBCSQL sql = new JDBCSQL(SQLExecute.create(jdbcClient));
		User user = new User();
		user.setName("name");
		user.setPwd("pwd");
		long start = System.currentTimeMillis();
		sql.insertNonEmpty(user, res -> {
			if (res.succeeded()) {
				System.out.println(res.succeeded());
				System.out.println("result: " + res.result());
				System.out.println(System.currentTimeMillis() - start);
			} else {
				System.out.println(res.cause());
			}
		});

		sql.getCount(res -> {
			if (res.succeeded()) {
				System.out.println("result: " + res.result());
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
