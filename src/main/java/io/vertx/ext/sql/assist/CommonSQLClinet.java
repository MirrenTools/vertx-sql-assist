package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

/**
 * 通用的数据库操作客户端
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 * @param <C>
 *          SQL执行器的客户端类型,比如JDBCClient
 */
public interface CommonSQLClinet<C> {
	/**
	 * 获取客户端
	 * 
	 * @return
	 */
	C getDbClient();

	/**
	 * 执行SQL
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	default Future<RowSet<Row>> execute(SqlAndParams qp) {
		Promise<RowSet<Row>> promise = Promise.promise();
		execute(qp, promise);
		return promise.future();
	}

	/**
	 * 执行SQL
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void execute(SqlAndParams qp, Handler<AsyncResult<RowSet<Row>>> handler);

	/**
	 * 执行查询结果为单个对象
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	default Future<JsonObject> queryAsObj(SqlAndParams qp) {
		Promise<JsonObject> promise = Promise.promise();
		queryAsObj(qp, promise);
		return promise.future();
	}

	/**
	 * 执行查询结果为单个对象
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 执行查询多个对象
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @return 返回结果
	 */
	default Future<List<JsonObject>> queryAsList(SqlAndParams qp) {
		Promise<List<JsonObject>> promise = Promise.promise();
		queryAsList(qp, promise);
		return promise.future();
	}

	/**
	 * 执行查询多个对象
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsList(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 执行更新等操作得到受影响的行数
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @return 受影响的行数
	 */
	default Future<Integer> update(SqlAndParams qp) {
		Promise<Integer> promise = Promise.promise();
		update(qp, promise);
		return promise.future();
	}

	/**
	 * 执行更新等操作得到受影响的行数
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	void update(SqlAndParams qp, Handler<AsyncResult<Integer>> handler);

	/**
	 * 执行更新并操作结果,比如返回GENERATED_KEYS等:<br>
	 * 获取GENERATED_KEYS方法:property传入JDBCPool.GENERATED_KEYS得到结果后判断row!=null就执行row.get对应类型数据
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	default Future<Row> updateResult(SqlAndParams qp, PropertyKind<Row> property) {
		Promise<Row> promise = Promise.promise();
		updateResult(qp, property, promise);
		return promise.future();
	}

	/**
	 * 执行更新并操作结果,比如返回GENERATED_KEYS等:<br>
	 * 获取GENERATED_KEYS方法:property传入JDBCPool.GENERATED_KEYS得到结果后判断row!=null就执行row.get对应类型数据
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	void updateResult(SqlAndParams qp, PropertyKind<Row> property, Handler<AsyncResult<Row>> handler);

	/**
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @return 返回结果
	 */
	default Future<Integer> batch(SqlAndParams qp) {
		Promise<Integer> promise = Promise.promise();
		batch(qp, promise);
		return promise.future();
	}

	/**
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	void batch(SqlAndParams qp, Handler<AsyncResult<Integer>> handler);

	/**
	 * 获得数据总行数
	 * 
	 */
	default Future<Long> getCount() {
		Promise<Long> promise = Promise.promise();
		getCount(null, promise);
		return promise.future();
	}

	/**
	 * 获得数据总行数
	 * 
	 * @param handler
	 *          返回数据总行数
	 */
	default void getCount(Handler<AsyncResult<Long>> handler) {
		getCount(null, handler);
	}

	/**
	 * 获取数据总行数
	 * 
	 * @param assist
	 *          查询工具,如果没有可以为null
	 * @return
	 */
	default Future<Long> getCount(SqlAssist assist) {
		Promise<Long> promise = Promise.promise();
		getCount(assist, promise);
		return promise.future();
	}

	/**
	 * 获取数据总行数
	 * 
	 * @param assist
	 *          查询工具,如果没有可以为null
	 * @param handler
	 *          返回数据总行数
	 */
	void getCount(SqlAssist assist, Handler<AsyncResult<Long>> handler);

	/**
	 * 查询所有数据
	 * 
	 * @return
	 */
	default Future<List<JsonObject>> selectAll() {
		Promise<List<JsonObject>> promise = Promise.promise();
		selectAll(null, promise);
		return promise.future();
	}

	/**
	 * 查询所有数据
	 * 
	 * @param handler
	 *          结果集
	 */
	default void selectAll(Handler<AsyncResult<List<JsonObject>>> handler) {
		selectAll(null, handler);
	}

	/**
	 * 通过查询工具查询所有数据
	 * 
	 * @param assist
	 *          查询工具帮助类
	 * @return
	 */
	default Future<List<JsonObject>> selectAll(SqlAssist assist) {
		Promise<List<JsonObject>> promise = Promise.promise();
		selectAll(assist, promise);
		return promise.future();
	}

	/**
	 * 通过查询工具查询所有数据
	 * 
	 * @param assist
	 *          查询工具帮助类
	 * @param handler
	 *          结果集
	 */
	void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 页查询,默认page=1,rowSize=15(取第一页,每页取15行数据)
	 * 
	 * @param assist
	 *          查询工具(注意:startRow在该方法中无效,最后会有page转换为startRow)
	 * @return 返回结果为(JsonObject)格式为: {@link SqlLimitResult#toJson()}
	 */
	default Future<JsonObject> limitAll(final SqlAssist assist) {
		Promise<JsonObject> promise = Promise.promise();
		limitAll(assist, promise);
		return promise.future();
	}

	/**
	 * 分页查询,默认page=1,rowSize=15(取第一页,每页取15行数据)
	 * 
	 * @param assist
	 *          查询工具(注意:startRow在该方法中无效,最后会有page转换为startRow)
	 * @param handler
	 *          返回结果为(JsonObject)格式为: {@link SqlLimitResult#toJson()}
	 */
	void limitAll(final SqlAssist assist, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @return 返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	default <S> Future<JsonObject> selectById(S primaryValue) {
		Promise<JsonObject> promise = Promise.promise();
		selectById(primaryValue, null, null, null, promise);
		return promise.future();
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	default <S> void selectById(S primaryValue, Handler<AsyncResult<JsonObject>> handler) {
		selectById(primaryValue, null, null, null, handler);
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @return 返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	default <S> Future<JsonObject> selectById(S primaryValue, String resultColumns) {
		Promise<JsonObject> promise = Promise.promise();
		selectById(primaryValue, resultColumns, null, null, promise);
		return promise.future();
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	default <S> void selectById(S primaryValue, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		selectById(primaryValue, resultColumns, null, null, handler);
	}

	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @return 返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	default <S> Future<JsonObject> selectById(S primaryValue, String resultColumns, String tableAlias, String joinOrReference) {
		Promise<JsonObject> promise = Promise.promise();
		selectById(primaryValue, resultColumns, tableAlias, joinOrReference, promise);
		return promise.future();
	}
	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	<S> void selectById(S primaryValue, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据;
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @return 结果:如果存在返回JsonObject,不存在返回null
	 */
	default <T> Future<JsonObject> selectSingleByObj(T obj) {
		Promise<JsonObject> promise = Promise.promise();
		selectSingleByObj(obj, null, null, null, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据;
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	default <T> void selectSingleByObj(T obj, Handler<AsyncResult<JsonObject>> handler) {
		selectSingleByObj(obj, null, null, null, handler);
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @return 结果:如果存在返回JsonObject,不存在返回null
	 */
	default <T> Future<JsonObject> selectSingleByObj(T obj, String resultColumns) {
		Promise<JsonObject> promise = Promise.promise();
		selectSingleByObj(obj, resultColumns, null, null, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	default <T> void selectSingleByObj(T obj, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		selectSingleByObj(obj, resultColumns, null, null, handler);
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @return 结果:如果存在返回JsonObject,不存在返回null
	 */
	default <T> Future<JsonObject> selectSingleByObj(T obj, String resultColumns, String tableAlias, String joinOrReference) {
		Promise<JsonObject> promise = Promise.promise();
		selectSingleByObj(obj, resultColumns, tableAlias, joinOrReference, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	<T> void selectSingleByObj(T obj, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @return 返回结果集
	 */
	default <T> Future<List<JsonObject>> selectByObj(T obj) {
		Promise<List<JsonObject>> promise = Promise.promise();
		selectByObj(obj, null, null, null, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回结果集
	 */
	default <T> void selectByObj(T obj, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectByObj(obj, null, null, null, handler);
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @return 返回结果集
	 */
	default <T> Future<List<JsonObject>> selectByObj(T obj, String resultColumns) {
		Promise<List<JsonObject>> promise = Promise.promise();
		selectByObj(obj, resultColumns, null, null, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @param handler
	 *          返回结果集
	 */
	default <T> void selectByObj(T obj, String resultColumns, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectByObj(obj, resultColumns, null, null, handler);
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @return 返回结果集
	 */
	default <T> Future<List<JsonObject>> selectByObj(T obj, String resultColumns, String tableAlias, String joinOrReference) {
		Promise<List<JsonObject>> promise = Promise.promise();
		selectByObj(obj, resultColumns, tableAlias, joinOrReference, promise);
		return promise.future();
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果集
	 */
	<T> void selectByObj(T obj, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 插入一个对象包括属性值为null的值
	 * 
	 * @param obj
	 *          对象
	 * @return 返回操作结果受影响的行数
	 */
	default <T> Future<Integer> insertAll(T obj) {
		Promise<Integer> promise = Promise.promise();
		insertAll(obj, promise);
		return promise.future();
	}

	/**
	 * 插入一个对象包括属性值为null的值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void insertAll(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @return 返回操作结果
	 */
	default <T> Future<Integer> insertNonEmpty(T obj) {
		Promise<Integer> promise = Promise.promise();
		insertNonEmpty(obj, promise);
		return promise.future();
	}

	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,只插入对象中值不为null的属性,并返回自增的结果
	 * 
	 * @param obj
	 *          对象
	 * @return 返回操作结果
	 */
	/**
	 * 
	 * @param <T>
	 * @param obj
	 *          对象
	 * @param property
	 *          获取属性的方法,比如JDBCPool使用传入JDBCPool.GENERATED_KEYS,MySQLPool使用PropertyKind.create("last-inserted-id",数据类型)
	 * @return
	 */
	default <T, R> Future<R> insertNonEmptyGeneratedKeys(T obj, PropertyKind<R> property) {
		Promise<R> promise = Promise.promise();
		insertNonEmptyGeneratedKeys(obj, property, promise);
		return promise.future();
	}

	/**
	 * 插入一个对象,只插入对象中值不为null的属性,并返回自增的结果
	 * 
	 * @param obj
	 *          对象
	 * @param property
	 *          获取属性的方法,比如JDBCPool使用传入JDBCPool.GENERATED_KEYS,MySQLPool使用PropertyKind.create("last-inserted-id",数据类型)
	 * @param handler
	 *          返回操作结果
	 */
	<T, R> void insertNonEmptyGeneratedKeys(T obj, PropertyKind<R> property, Handler<AsyncResult<R>> handler);

	/**
	 * 批量添加全部所有字段
	 * 
	 * @param list
	 *          对象
	 * @return 成功返回受影响的行数,如果对象为null或空则返回0
	 */
	default <T> Future<Integer> insertBatch(List<T> list) {
		Promise<Integer> promise = Promise.promise();
		insertBatch(list, promise);
		return promise.future();
	}

	/**
	 * 批量添加全部所有字段
	 * 
	 * @param list
	 *          对象
	 * @param handler
	 *          成功返回受影响的行数,如果对象为null或空则返回0
	 */
	<T> void insertBatch(List<T> list, Handler<AsyncResult<Integer>> handler);
	/**
	 * 批量添加自定字段
	 * 
	 * @param column
	 *          字段的名称示例:["id","name",...]
	 * @param params
	 *          字段对应的参数示例:[["id","name"],["id","name"]...]
	 * @return 成功返回受影响的行数,如果字段或字段参数为null或空则返回0
	 */
	default Future<Integer> insertBatch(List<String> columns, List<Tuple> params) {
		Promise<Integer> promise = Promise.promise();
		insertBatch(columns, params, promise);
		return promise.future();
	}

	/**
	 * 批量添加自定字段
	 * 
	 * @param column
	 *          字段的名称示例:["id","name",...]
	 * @param params
	 *          字段对应的参数示例:[["id","name"],["id","name"]...]
	 * @param handler
	 *          成功返回受影响的行数,如果字段或字段参数为null或空则返回0
	 */
	void insertBatch(List<String> columns, List<Tuple> params, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @return 结果集受影响的行数
	 */
	default <T> Future<Integer> replace(T obj) {
		Promise<Integer> promise = Promise.promise();
		replace(obj, promise);
		return promise.future();
	}

	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果集受影响的行数
	 */
	<T> void replace(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @return 返回操作结果
	 */
	default <T> Future<Integer> updateAllById(T obj) {
		Promise<Integer> promise = Promise.promise();
		updateAllById(obj, promise);
		return promise.future();
	}

	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateAllById(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 更新一个对象中所有的属性包括null值,条件为SqlAssist条件集<br>
	 * 
	 * @param obj
	 *          对象
	 * @param SqlAssist
	 *          sql帮助工具
	 * 
	 * @return 返回操作结果
	 */
	default <T> Future<Integer> updateAllByAssist(T obj, SqlAssist assist) {
		Promise<Integer> promise = Promise.promise();
		updateAllByAssist(obj, assist, promise);
		return promise.future();
	}

	/**
	 * 更新一个对象中所有的属性包括null值,条件为SqlAssist条件集<br>
	 * 
	 * @param obj
	 *          对象
	 * @param SqlAssist
	 *          sql帮助工具
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler);

	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @return 返回操作结果
	 */
	default <T> Future<Integer> updateNonEmptyById(T obj) {
		Promise<Integer> promise = Promise.promise();
		updateNonEmptyById(obj, promise);
		return promise.future();
	}
	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateNonEmptyById(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 更新一个对象中属性不为null值,条件为SqlAssist条件集
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          查询工具
	 * @return 返回操作结果
	 */
	default <T> Future<Integer> updateNonEmptyByAssist(T obj, SqlAssist assist) {
		Promise<Integer> promise = Promise.promise();
		updateNonEmptyByAssist(obj, assist, promise);
		return promise.future();
	}

	/**
	 * 更新一个对象中属性不为null值,条件为SqlAssist条件集
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          查询工具
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler);

	/**
	 * 通过主键值设置指定的列为空
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          主键
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @return 返回操作结果
	 */
	default <S> Future<Integer> updateSetNullById(S primaryValue, List<String> columns) {
		Promise<Integer> promise = Promise.promise();
		updateSetNullById(primaryValue, columns, promise);
		return promise.future();
	}

	/**
	 * 通过主键值设置指定的列为空
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          主键
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	<S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<Integer>> handler);

	/**
	 * 通过Assist作为条件设置指定的列为空
	 * 
	 * @param assist
	 *          sql帮助工具
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	default <T> Future<Integer> updateSetNullByAssist(SqlAssist assist, List<String> columns) {
		Promise<Integer> promise = Promise.promise();
		updateSetNullByAssist(assist, columns, promise);
		return promise.future();
	}

	/**
	 * 通过Assist作为条件设置指定的列为空
	 * 
	 * @param assist
	 *          sql帮助工具
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @return handler 返回操作结果
	 */
	<T> void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler);

	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param obj
	 *          主键值
	 * @return 返回操作结果
	 */
	default <S> Future<Integer> deleteById(S primaryValue) {
		Promise<Integer> promise = Promise.promise();
		deleteById(primaryValue, promise);
		return promise.future();
	}

	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param obj
	 *          主键值
	 * @param handler
	 *          返回操作结果
	 */
	<S> void deleteById(S primaryValue, Handler<AsyncResult<Integer>> handler);

	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          条件集
	 * 
	 * @return 返回操作结果
	 */
	default Future<Integer> deleteByAssist(SqlAssist assist) {
		Promise<Integer> promise = Promise.promise();
		deleteByAssist(assist, promise);
		return promise.future();
	}

	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          条件集
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler);

}
