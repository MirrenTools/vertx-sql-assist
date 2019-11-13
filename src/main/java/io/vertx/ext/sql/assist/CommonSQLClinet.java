package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 通用的数据库操作客户端
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
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
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsListObj(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsListArray(SqlAndParams qp, Handler<AsyncResult<List<JsonArray>>> handler);

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
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	void batch(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler);

	/**
	 * 获得数据总行数
	 * 
	 * @param handler
	 *          返回数据总行数
	 */
	void getCount(Handler<AsyncResult<Long>> handler);

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
	 * @param handler
	 *          结果集
	 */
	void selectAll(Handler<AsyncResult<List<JsonObject>>> handler);

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
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	<S> void selectById(S primaryValue, Handler<AsyncResult<JsonObject>> handler);

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
	<S> void selectById(S primaryValue, String resultColumns, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	<S> void selectById(S primaryValue, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据;
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	<T> void selectSingleByObj(T obj, Handler<AsyncResult<JsonObject>> handler);

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
	<T> void selectSingleByObj(T obj, String resultColumns, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	<T> void selectSingleByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回结果集
	 */
	<T> void selectByObj(T obj, Handler<AsyncResult<List<JsonObject>>> handler);

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
	<T> void selectByObj(T obj, String resultColumns, Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果集
	 */
	<T> void selectByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<List<JsonObject>>> handler);

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
	 * @param handler
	 *          返回操作结果
	 */
	<T> void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 批量添加全部所有字段
	 * 
	 * @param list
	 *          对象
	 * @param handler
	 *          成功返回受影响的行数,如果对象为null或空则返回0
	 */
	<T> void insertBatch(List<T> list, Handler<AsyncResult<Long>> handler);

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
	void insertBatch(List<String> columns, List<JsonArray> params, Handler<AsyncResult<Long>> handler);

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
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler);

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
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	<T> void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler);

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
	 * @param handler
	 *          返回操作结果
	 */
	void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler);

}
