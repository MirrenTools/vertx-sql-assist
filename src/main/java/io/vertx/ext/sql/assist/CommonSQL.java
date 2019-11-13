package io.vertx.ext.sql.assist;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
/**
 * 通用的数据库操作客户端的默认实现,
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 * @param <E> 实体类的类型
 * @param <C> SQL执行器的客户端类型,比如JDBCClient
 */
public abstract class CommonSQL<E, C> implements CommonSQLClinet<C> {
	/** SQL 执行器 */
	private SQLExecute<C> execute;
	/** SQL 命令 */
	private SQLCommand command;

	/**
	 * 使用以注册或默认的{@link SQLStatement}
	 * 
	 * @param entityClz
	 *          实体类,类必须包含{@link Table} {@link TableId} {@link TableColumn}注解
	 * @param execute
	 *          执行器
	 */
	public CommonSQL(SQLExecute<C> execute) {
		Class<?> entityClz = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		SQLStatement statement = SQLStatement.create(entityClz);
		this.command = new SQLCommandImpl(statement, execute);
	}

	/**
	 * 使用自定义的{@link SQLStatement}
	 * 
	 * @param execute
	 *          SQL执行器
	 * @param statement
	 *          SQL执行语句
	 */
	public CommonSQL(SQLExecute<E> execute, SQLStatement statement) {
		this.command = new SQLCommandImpl(statement, execute);
	}

	/**
	 * 获取客户端
	 * 
	 * @return
	 * 
	 */
	@Override
	public C getDbClient() {
		return execute.getClient();
	}

	@Override
	public void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler) {
		execute.queryAsObj(qp, handler);
	}

	@Override
	public void queryAsListObj(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler) {
		execute.queryAsListObj(qp, handler);
	}

	@Override
	public void queryAsListArray(SqlAndParams qp, Handler<AsyncResult<List<JsonArray>>> handler) {
		execute.queryAsListArray(qp, handler);
	}

	@Override
	public void update(SqlAndParams qp, Handler<AsyncResult<Integer>> handler) {
		execute.update(qp, handler);
	}

	@Override
	public void batch(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler) {
		execute.batch(qp, handler);
	}

	@Override
	public void getCount(Handler<AsyncResult<Long>> handler) {
		command.getCount(handler);
	}

	@Override
	public void getCount(SqlAssist assist, Handler<AsyncResult<Long>> handler) {
		command.getCount(assist, handler);
	}

	@Override
	public void selectAll(Handler<AsyncResult<List<JsonObject>>> handler) {
		command.selectAll(handler);
	}

	@Override
	public void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler) {
		command.selectAll(assist, handler);
	}

	@Override
	public void limitAll(SqlAssist assist, Handler<AsyncResult<JsonObject>> handler) {
		command.limitAll(assist, handler);
	}

	@Override
	public <S> void selectById(S primaryValue, Handler<AsyncResult<JsonObject>> handler) {
		command.selectById(primaryValue, handler);
	}

	@Override
	public <S> void selectById(S primaryValue, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		command.selectById(primaryValue, resultColumns, handler);
	}

	@Override
	public <S> void selectById(S primaryValue, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		command.selectById(primaryValue, resultColumns, joinOrReference, handler);
	}

	@Override
	public <T> void selectSingleByObj(T obj, Handler<AsyncResult<JsonObject>> handler) {
		command.selectSingleByObj(obj, handler);
	}

	@Override
	public <T> void selectSingleByObj(T obj, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		command.selectSingleByObj(obj, resultColumns, handler);
	}

	@Override
	public <T> void selectSingleByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		command.selectSingleByObj(obj, resultColumns, joinOrReference, handler);
	}

	@Override
	public <T> void selectByObj(T obj, Handler<AsyncResult<List<JsonObject>>> handler) {
		command.selectByObj(obj, handler);
	}

	@Override
	public <T> void selectByObj(T obj, String resultColumns, Handler<AsyncResult<List<JsonObject>>> handler) {
		command.selectByObj(obj, resultColumns, handler);
	}

	@Override
	public <T> void selectByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<List<JsonObject>>> handler) {
		command.selectByObj(obj, resultColumns, joinOrReference, handler);
	}

	@Override
	public <T> void insertAll(T obj, Handler<AsyncResult<Integer>> handler) {
		command.insertAll(obj, handler);
	}

	@Override
	public <T> void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler) {
		command.insertNonEmpty(obj, handler);
	}

	@Override
	public <T> void insertBatch(List<T> list, Handler<AsyncResult<Long>> handler) {
		command.insertBatch(list, handler);
	}

	@Override
	public void insertBatch(List<String> columns, List<JsonArray> params, Handler<AsyncResult<Long>> handler) {
		command.insertBatch(columns, params, handler);
	}

	@Override
	public <T> void replace(T obj, Handler<AsyncResult<Integer>> handler) {
		command.replace(obj, handler);
	}

	@Override
	public <T> void updateAllById(T obj, Handler<AsyncResult<Integer>> handler) {
		command.updateAllById(obj, handler);
	}

	@Override
	public <T> void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		command.updateAllByAssist(obj, assist, handler);
	}

	@Override
	public <T> void updateNonEmptyById(T obj, Handler<AsyncResult<Integer>> handler) {
		command.updateNonEmptyById(obj, handler);
	}

	@Override
	public <T> void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		command.updateNonEmptyByAssist(obj, assist, handler);
	}

	@Override
	public <S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		command.updateSetNullById(primaryValue, columns, handler);
	}

	@Override
	public <T> void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		command.updateSetNullByAssist(assist, columns, handler);
	}

	@Override
	public <S> void deleteById(S primaryValue, Handler<AsyncResult<Integer>> handler) {
		command.deleteById(primaryValue, handler);
	}

	@Override
	public void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		command.deleteByAssist(assist, handler);
	}

}
