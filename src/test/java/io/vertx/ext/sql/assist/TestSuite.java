package io.vertx.ext.sql.assist;

import java.util.Objects;

import org.mirrentools.sd.ScrewDriver;
import org.mirrentools.sd.ScrewDriverDbUtil;
import org.mirrentools.sd.SdType;
import org.mirrentools.sd.models.SdBean;
import org.mirrentools.sd.models.SdColumn;
import org.mirrentools.sd.models.db.update.SdBasicTableContent;
import org.mirrentools.sd.options.ScrewDriverOptions;
import org.mirrentools.sd.options.SdDatabaseOptions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
/**
 * 测试方法合集
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 * @param <E>
 * @param <C>
 */
public abstract class TestSuite<E, C> {
	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(TestSuite.class);

	private final static String COLUMN_ID = "id";
	private final static String COLUMN_CID = "cid";
	private final static String COLUMN_NICKNAME = "nickname";
	private final static String COLUMN_PWD = "pwd";

	private CommonSQL<E, C> sql;
	/** 数据库连接信息 */
	private JsonObject config;
	public TestSuite(CommonSQL<E, C> sql, JsonObject config) {
		this.sql = sql;
		this.config = config;
	}
	/**
	 * 执行测试
	 * 
	 * @return
	 */
	public void runTest(Handler<AsyncResult<Integer>> handler) {
		try {
			resetDB();
		} catch (Throwable e) {
			handler.handle(Future.failedFuture(e));
		}
		insertAll(handler);
	};

	/**
	 * 重设数据库表
	 * 
	 * @throws Throwable
	 */
	public void resetDB() throws Throwable {
		SdDatabaseOptions dbOptions = new SdDatabaseOptions(config.getString("driver_class"), config.getString("url"));
		if (config.getString("user") != null) {
			dbOptions.setUser(config.getString("user"));
		}
		if (config.getString("user") != null) {
			dbOptions.setUser(config.getString("user"));
		}
		String classesName = "classes";
		String studentName = "student";

		ScrewDriver screwDriver = ScrewDriver.instance(new ScrewDriverOptions(dbOptions));
		ScrewDriverDbUtil dbUtil = ScrewDriverDbUtil.instance(dbOptions);
		// 重置班级表
		SdBasicTableContent classesContent = new SdBasicTableContent() {
		};
		classesContent.setTableName(classesName);
		dbUtil.deleteTable(classesContent);
		SdBean classesbean = new SdBean().setName(classesName);
		classesbean.addColumn(new SdColumn().setName("id").setType(SdType.INTEGER).setPrimary(true))
				.addColumn(new SdColumn().setName("classanme").setType(SdType.STRING).setLength(30));
		screwDriver.createTable(classesbean);

		dbUtil.execute(String.format("insert into %s values(1,'class name')", classesName));

		// 重置学生表
		SdBasicTableContent studentContent = new SdBasicTableContent() {
		};
		studentContent.setTableName(studentName);
		dbUtil.deleteTable(studentContent);
		SdBean bean = new SdBean().setName(studentName);
		bean.addColumn(new SdColumn().setName(COLUMN_ID).setType(SdType.INTEGER).setPrimary(true).setAutoIncrement(true));
		bean.addColumn(
				new SdColumn().setName(COLUMN_CID).setType(SdType.INTEGER).setForeignReferencesTable(classesName).setForeignReferencesColumn("id"));
		bean.addColumn(new SdColumn().setName(COLUMN_NICKNAME).setType(SdType.STRING).setLength(255));
		bean.addColumn(new SdColumn().setName(COLUMN_PWD).setType(SdType.STRING).setLength(255));
		screwDriver.createTable(bean);
	}

	public abstract E insertAllData();
	public void insertAll(Handler<AsyncResult<Integer>> handler) {
		sql.insertAll(insertAllData()).onSuccess(res -> {
			LOG.info("insertAll 执行结果:" + res);
			if (Objects.equals(1, res)) {
				LOG.info("insertAll 测试通过!");
				// 测试获取数量
				getCount(handler);
			} else {
				handler.handle(Future.failedFuture("insertAll 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	public void getCount(Handler<AsyncResult<Integer>> handler) {
		sql.getCount().onSuccess(res -> {
			LOG.info("getCount 执行结果:" + res);
			if (Objects.equals(1L, res)) {
				LOG.info("getCount 测试通过!");
				// 测试根据id更新所有的数据
				updateAllById(handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("getCount 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}
	
	public abstract E updateAllByIdData();
	public void updateAllById(Handler<AsyncResult<Integer>> handler) {
		sql.updateAllById(updateAllByIdData()).onSuccess(res -> {
			LOG.info("updateAllById 执行结果:" + res);
			if (Objects.equals(1, res)) {
				LOG.info("updateAllById 测试通过!");
				selectById(handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("updateAllById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}
	
	public abstract Object selectByIdData();
	public abstract String selectByIdTestKey();
	public abstract Object selectByIdTestValue();
	public abstract String selectByIdTestNullKey();
	public void selectById(Handler<AsyncResult<Integer>> handler) {
		sql.selectById(selectByIdData()).onSuccess(res -> {
			LOG.info("selectById 执行结果:" + res);
			if (Objects.equals(selectByIdTestValue(), res.getValue(selectByIdTestKey()))
					&& Objects.isNull(res.getValue(selectByIdTestNullKey()))) {
				LOG.info("selectById 测试通过!");
				// 执行测试新增不为空的
				insertNonEmpty(handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("selectById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	public abstract E insertNonEmptyData();
	public void insertNonEmpty(Handler<AsyncResult<Integer>> handler) {
		sql.insertNonEmpty(insertNonEmptyData()).onSuccess(res -> {
			LOG.info("insertNonEmpty 执行结果:" + res);
			if (Objects.equals(1, res)) {
				LOG.info("insertNonEmpty 测试通过!");
				// 通过id查询,查询返回指定列
				selectByIdResultColumns(handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("insertNonEmpty 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	public abstract Object selectByIdResultColumnsData();
	public abstract String selectByIdResultColumnsColumns();
	public abstract String selectByIdResultColumnsTestKey();
	public abstract Object selectByIdResultColumnsTestValue();
	public abstract String selectByIdResultColumnsTestValueNullKey();
	public abstract String selectByIdResultColumnsTestNullKey();
	public void selectByIdResultColumns(Handler<AsyncResult<Integer>> handler) {
		sql.selectById(selectByIdResultColumnsData(), selectByIdResultColumnsColumns()).onSuccess(res -> {
			LOG.info("selectByIdResultColumns 执行结果:" + res);
			if (Objects.equals(selectByIdResultColumnsTestValue(), res.getValue(selectByIdResultColumnsTestKey()))
					&& Objects.isNull(res.getValue(selectByIdResultColumnsTestValueNullKey()))
					&& Objects.isNull(res.getValue(selectByIdResultColumnsTestNullKey()))
					) {
				LOG.info("selectByIdResultColumns 测试通过!");
				insertNonEmptyGeneratedKeys(handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("selectById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}
	
	public abstract E insertNonEmptyGeneratedKeysData();
	public void insertNonEmptyGeneratedKeys(Handler<AsyncResult<Integer>> handler) {
		sql.insertNonEmptyGeneratedKeys(insertNonEmptyGeneratedKeysData()).onSuccess(res -> {
			LOG.info("insertNonEmptyGeneratedKeys 执行结果:" + res);
			int id = ((Number) res).intValue();
			if (id >= 1) {
				LOG.info("insertNonEmptyGeneratedKeys 测试通过!");
				selectSingleByObj(id, handler);
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("insertNonEmptyGeneratedKeys 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	public abstract E selectSingleByObjData(int id);
	public abstract String selectSingleByObjTestKey();
	public abstract Object selectSingleByObjTestValue();
	public void selectSingleByObj(int id, Handler<AsyncResult<Integer>> handler) {
		sql.selectSingleByObj(selectSingleByObjData(id)).onSuccess(res -> {
			LOG.info("selectSingleByObj 执行结果:" + res);
			if (Objects.equals(selectSingleByObjTestValue(), res.getValue(selectSingleByObjTestKey()))) {
				LOG.info("selectSingleByObj 测试通过!");
				handler.handle(Future.succeededFuture(1));
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("selectById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	
}
