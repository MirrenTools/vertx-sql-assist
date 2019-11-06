package io.vertx.ext.sql.assist.sqlserver;

import java.util.ArrayList;
import java.util.List;

import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.User;
import io.vertx.ext.sql.assist.sql.SqlServer;

public class TestSQL extends SqlServer<User> {
	@Override
	protected String tableName() {
		return "user";
	}

	@Override
	protected String primaryId() {
		return "id";
	}

	@Override
	protected String columns() {
		return "id,name,pwd";
	}

	@Override
	protected List<SqlPropertyValue<?>> propertyValue(User obj) {
		List<SqlPropertyValue<?>> result = new ArrayList<>();
		result.add(new SqlPropertyValue<>("pwd", obj.getPwd()));
		result.add(new SqlPropertyValue<>("name", obj.getName()));
		result.add(new SqlPropertyValue<>("id", obj.getId()));
		return result;
	}

	@Override
	protected JDBCClient jdbcClient() {
		return null;
	}

	@Override
	public SqlAndParams getCountSQL(SqlAssist assist) {
		return super.getCountSQL(assist);
	}

	@Override
	public SqlAndParams selectAllSQL(SqlAssist assist) {
		return super.selectAllSQL(assist);
	}

	@Override
	public <S> SqlAndParams selectByIdSQL(S primaryValue, String resultColumns, String joinOrReference) {
		return super.selectByIdSQL(primaryValue, resultColumns, joinOrReference);
	}

	@Override
	public SqlAndParams selectByObjSQL(User obj, String resultColumns, String joinOrReference, boolean single) {
		return super.selectByObjSQL(obj, resultColumns, joinOrReference, single);
	}

	@Override
	public SqlAndParams insertAllSQL(User obj) {
		return super.insertAllSQL(obj);
	}

	@Override
	public SqlAndParams insertNonEmptySQL(User obj) {
		return super.insertNonEmptySQL(obj);
	}

	@Override
	public SqlAndParams replaceSQL(User obj) {
		return super.replaceSQL(obj);
	}

	@Override
	public SqlAndParams updateAllByIdSQL(User obj) {
		return super.updateAllByIdSQL(obj);
	}

	@Override
	public SqlAndParams updateAllByAssistSQL(User obj, SqlAssist assist) {
		return super.updateAllByAssistSQL(obj, assist);
	}

	@Override
	public SqlAndParams updateNonEmptyByIdSQL(User obj) {
		return super.updateNonEmptyByIdSQL(obj);
	}

	@Override
	public SqlAndParams updateNonEmptyByAssistSQL(User obj, SqlAssist assist) {
		return super.updateNonEmptyByAssistSQL(obj, assist);
	}

	@Override
	public <S> SqlAndParams updateSetNullByIdSQL(S primaryValue, List<String> columns) {
		return super.updateSetNullByIdSQL(primaryValue, columns);
	}

	@Override
	public SqlAndParams updateSetNullByAssistSQL(SqlAssist assist, List<String> columns) {

		return super.updateSetNullByAssistSQL(assist, columns);
	}

	@Override
	public <S> SqlAndParams deleteByIdSQL(S primaryValue) {
		return super.deleteByIdSQL(primaryValue);
	}

	@Override
	public SqlAndParams deleteByAssistSQL(SqlAssist assist) {
		return super.deleteByAssistSQL(assist);
	}

}
