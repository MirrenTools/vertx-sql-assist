package io.vertx.ext.sql.assist;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.assist.sql.JsonObjectSQL;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MySQLPoolTest extends TestSuite<JsonObject, MySQLPool> {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		JsonObject config = new JsonObject();
		config.put("url", "jdbc:mysql://10.0.0.79:3306/test_db?useUnicode=true&useSSL=false&serverTimezone=UTC")
				.put("driver_class", "com.mysql.cj.jdbc.Driver").put("user", "root").put("password", "root");

		MySQLConnectOptions connectOptions = new MySQLConnectOptions()
				.setPort(3306)
				.setHost("10.0.0.79")
				.setDatabase("test_db")
				.setUser("root")
				.setPassword("root")
				.setUseAffectedRows(true)
				;
		
		MySQLPool pool = MySQLPool.pool(vertx, connectOptions, new PoolOptions());
		JsonObjectSQL sql = new JsonObjectSQL(SQLExecute.createMySQL(pool));
		MySQLPoolTest test = new MySQLPoolTest(sql, config);
		test.runTest(res -> System.exit(0));
	}

	public MySQLPoolTest(JsonObjectSQL sql, JsonObject config) {
		super(sql, config);
	}

	@Override
	public JsonObject insertAllData() {
		return new JsonObject().put("id", 1).put("cid", 1).put("nickname", "nickname");
	}
	@Override
	public JsonObject insertNonEmptyData() {
		return new JsonObject().put("id", 2).put("cid", 1);
	}

	@Override
	public int insertNonEmptyGeneratedKeysResultPoolType() {
		return 1;
	}

	@Override
	public JsonObject insertNonEmptyGeneratedKeysData() {
		return new JsonObject().put("cid", 1).put("nickname", "nickname").put("pwd", "pwd");
	}

	@Override
	public List<JsonObject> insertBatchData() {
		return Arrays.asList(new JsonObject().put("cid", 1).put("pwd", "batch"), new JsonObject().put("cid", 1).put("pwd", "batch"));
	}

	@Override
	public JsonObject replaceData() {
		return new JsonObject().put("id", 1).put("cid", 1).put("nickname", "replace").put("pwd", "replace");
	}

	@Override
	public JsonObject updateAllByIdData() {
		return new JsonObject().put("id", 1).put("cid", 1).put("nickname", "update");
	}

	@Override
	public JsonObject updateAllByAssistData() {
		return new JsonObject().put("id", 1).put("cid", 1).put("pwd", "updatebyAssist");
	}

	@Override
	public JsonObject updateNonEmptyByIdData() {
		return new JsonObject().put("id", 1).put("cid", 1).put("nickname", "nickname");
	}

	@Override
	public JsonObject updateNonEmptyByAssistData() {
		return new JsonObject().put("pwd", "updateNonEmptyByAssist");
	}

	@Override
	public JsonObject selectByObjData() {
		return new JsonObject().put("cid", 1);
	}

}
