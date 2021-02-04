package io.vertx.ext.sql.assist.sql;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.assist.CommonSQL;
import io.vertx.ext.sql.assist.SQLExecute;
import io.vertx.ext.sql.assist.SQLStatement;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.mysqlclient.MySQLPool;

public class JsonObjectSQL extends CommonSQL<JsonObject, MySQLPool> {
	public JsonObjectSQL(SQLExecute<MySQLPool> execute) {
		super(execute, statement());
	}

	public static SQLStatement statement() {
		AbstractStatementSQL statement = new AbstractStatementSQL(JsonObject.class) {
			@Override
			public String tableName() {
				return "student";
			}

			@Override
			public String primaryId() {
				return "id";
			}

			@Override
			public String resultColumns() {
				return "id,cid,nickname,pwd AS possword";
			}

			@Override
			public <T> List<SqlPropertyValue<?>> getPropertyValue(T obj) throws Exception {
				JsonObject data = (JsonObject) obj;
				List<SqlPropertyValue<?>> result = new ArrayList<>(4);
				result.add(new SqlPropertyValue<>("pwd", data.getString("pwd")));
				result.add(new SqlPropertyValue<>("nickname", data.getString("nickname")));
				result.add(new SqlPropertyValue<>("cid", data.getInteger("cid")));
				result.add(new SqlPropertyValue<>("id", data.getInteger("id")));
				return result;
			}

		};
		return statement;
	}

}
