package io.vertx.ext.sql.assist;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class JDBCClientTest {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		JsonObject config = new JsonObject().put("url", "jdbc:mysql://localhost:3306/root?useUnicode=true&useSSL=false&serverTimezone=UTC")
				.put("driver_class", "com.mysql.cj.jdbc.Driver").put("password", "root").put("user", "root");
		JDBCClient jdbcClient = JDBCClient.createShared(vertx, config);
		// SQLStatement.register(OracleStatementSQL.class);
		JDBCSQL sql = new JDBCSQL(SQLExecute.createJDBC(jdbcClient));
		User user = new User();
		user.setName("name");
		user.setPwd("pwd");
		sql.insertNonEmpty(user, res -> {
			if (res.succeeded()) {
				System.out.println("insertNonEmpty: " + res.result());
			} else {
				System.out.println(res.cause());
			}
		});
		sql.getCount(res -> {
			if (res.succeeded()) {
				System.out.println("getCount: " + res.result());
			} else {
				System.out.println(res.cause());
			}
		});
		sql.getCount(new SqlAssist().andLte("id", 10), res -> {
			if (res.succeeded()) {
				System.out.println("getCountAssist: " + res.result());
			} else {
				System.out.println(res.cause());
			}
		});

		sql.selectAll(res -> {
			if (res.succeeded()) {
				System.out.println("selectAll " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		sql.selectAll(new SqlAssist().setStartRow(10).setRowSize(5), res -> {
			if (res.succeeded()) {
				System.out.println("selectAllAssist: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		sql.deleteById(1, res -> {
			if (res.succeeded()) {
				System.out.println("deleteById: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});

		sql.deleteByAssist(new SqlAssist().andEq("id", 2), res -> {
			if (res.succeeded()) {
				System.out.println("deleteByAssist: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		List<User> list = new ArrayList<>();
		list.add(user);
		list.add(user);
		sql.insertBatch(list, res -> {
			if (res.succeeded()) {
				System.out.println("insertBatch: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		User user2 = new User();
		user2.setId(3L);
		user2.setName("update name3");
		sql.updateNonEmptyById(user2, res -> {
			if (res.succeeded()) {
				System.out.println("updateNonEmptyById: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});
		User user3 = new User();
		user3.setName("update name4");
		sql.updateNonEmptyByAssist(user3,new SqlAssist().andEq("id", 4L), res -> {
			if (res.succeeded()) {
				System.out.println("updateNonEmptyById: " + res.result());
			} else {
				System.err.println(res.cause());
			}
		});

		vertx.setTimer(3000, tid -> System.exit(0));
	}
}
