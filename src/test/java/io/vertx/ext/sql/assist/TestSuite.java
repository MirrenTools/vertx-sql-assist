package io.vertx.ext.sql.assist;

import java.util.Arrays;
import java.util.List;
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
import io.vertx.sqlclient.Tuple;
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
	private final static String KEY_PWD = "possword";

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
			failed(e, handler);
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
		if (config.getString("password") != null) {
			dbOptions.setPassword(config.getString("password"));
		}
		String classesName = "classes";
		String studentName = "student";

		ScrewDriver screwDriver = ScrewDriver.instance(new ScrewDriverOptions(dbOptions));
		ScrewDriverDbUtil dbUtil = ScrewDriverDbUtil.instance(dbOptions);
		// 重置学生表
		SdBasicTableContent studentContent = new SdBasicTableContent() {
		};
		studentContent.setTableName(studentName);
		dbUtil.deleteTable(studentContent);

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

		SdBean bean = new SdBean().setName(studentName);
		bean.addColumn(new SdColumn().setName(COLUMN_ID).setType(SdType.INTEGER).setPrimary(true).setAutoIncrement(true));
		bean.addColumn(new SdColumn().setName(COLUMN_CID).setType(SdType.INTEGER).setForeignConstraint("FK_classes_student_id")
				.setForeignReferencesTable(classesName).setForeignReferencesColumn("id"));
		bean.addColumn(new SdColumn().setName(COLUMN_NICKNAME).setType(SdType.STRING).setLength(255));
		bean.addColumn(new SdColumn().setName(COLUMN_PWD).setType(SdType.STRING).setLength(255));
		screwDriver.createTable(bean);
	}
	/**
	 * 测试成功
	 * 
	 * @param handler
	 */
	public void succeeded(Handler<AsyncResult<Integer>> handler) {
		System.out.println("===================================================");
		System.out.println("=====================succeeded=====================");
		System.out.println("===================================================");
		handler.handle(Future.succeededFuture(1));
	}
	/**
	 * 测试失败
	 * 
	 * @param fun
	 *          方法的名称
	 * @param err
	 *          异常的信息
	 * @param handler
	 *          操作
	 */
	public void failed(Throwable err, Handler<AsyncResult<Integer>> handler) {
		err.printStackTrace();
		System.out.println("**********************************************");
		System.out.println("********************failed********************");
		System.out.println(err.getMessage());
		System.out.println("**********************************************");
		handler.handle(Future.failedFuture(err));
	}
	/**
	 * 测试失败
	 * 
	 * @param handler
	 */
	public void failed(String err, Handler<AsyncResult<Integer>> handler) {
		System.out.println("**********************************************");
		System.out.println("********************failed********************");
		System.out.println(err);
		System.out.println("**********************************************");
		handler.handle(Future.failedFuture(err));
	}

	/** 测试新增所有的数据,要求:id=1,cid=1,nickname=nickname,pwd=null */
	public abstract E insertAllData();
	public void insertAll(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.insertAll(insertAllData()).onSuccess(res -> {
			LOG.info("insertAll 执行结果:" + res);
			if (Objects.equals(1, res)) {
				getByIdAndEquals(1, "nickname", null, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("insertAll 测试通过!");
						insertNonEmpty(handler);
					} else {
						LOG.info("insertAll 结果不匹配!");
						failed("insertAll 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("insertAll 结果不匹配!");
				String format = String.format("insertAll 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试新增不为空的数据,要求:id=2,cid=1,nickname=null,pwd=null */
	public abstract E insertNonEmptyData();
	public void insertNonEmpty(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.insertNonEmpty(insertNonEmptyData()).onSuccess(res -> {
			LOG.info("insertNonEmpty 执行结果:" + res);
			if (Objects.equals(1, res)) {
				getByIdAndEquals(2, null, null, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("insertNonEmpty 测试通过!");
						insertNonEmptyGeneratedKeys(handler);
					} else {
						LOG.info("insertNonEmpty 结果不匹配!");
						failed("insertNonEmpty 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("insertNonEmpty 结果不匹配!");
				String format = String.format("insertNonEmpty 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试新增不为空的数据,要求:id=null,cid=1,nickname=nickname,pwd=pwd */
	public abstract E insertNonEmptyGeneratedKeysData();
	public void insertNonEmptyGeneratedKeys(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.insertNonEmptyGeneratedKeys(insertNonEmptyGeneratedKeysData()).onSuccess(res -> {
			LOG.info("insertNonEmptyGeneratedKeys 执行结果:" + res);
			int id = (res == null ? 0 : ((Number) res).intValue());
			if (id >= 1) {
				getByIdAndEquals(id, "nickname", "pwd", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("insertNonEmptyGeneratedKeys 测试通过!");
						insertBatch(handler);
					} else {
						LOG.info("insertNonEmptyGeneratedKeys 结果不匹配!");
						failed("insertNonEmptyGeneratedKeys 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("insertNonEmptyGeneratedKeys 结果不匹配!");
				String format = String.format("insertNonEmptyGeneratedKeys 结果不匹配!\n期望结果:[>=%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试批量新增所有,要求2个数据:id=null,cid=1,nickname=null,pwd=batch */
	public abstract List<E> insertBatchData();
	public void insertBatch(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.insertBatch(insertBatchData()).onSuccess(res -> {
			LOG.info("insertBatch 执行结果:" + res);
			if (res == 2) {
				SqlAssist assist = new SqlAssist();
				assist.and(COLUMN_NICKNAME + " IS NULL").andEq(COLUMN_PWD, "batch");
				getCountAndEquals(assist, 2, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("insertBatch 测试通过!");
						insertBatchCulumns(handler);
					} else {
						LOG.info("insertBatch 结果不匹配!");
						failed("insertBatch 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("insertBatch 结果不匹配!");
				String format = String.format("insertBatch 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 2, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void insertBatchCulumns(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.insertBatch(Arrays.asList("cid", "pwd"), Arrays.asList(Tuple.of(1, "batch2"), Tuple.of(1, "batch2"))).onSuccess(res -> {
			LOG.info("insertBatchCulumns 执行结果:" + res);
			if (res == 2) {
				SqlAssist assist = new SqlAssist();
				assist.and(COLUMN_NICKNAME + " IS NULL").andEq(COLUMN_PWD, "batch2");
				getCountAndEquals(assist, 2, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("insertBatchCulumns 测试通过!");
						replace(handler);
					} else {
						LOG.info("insertBatchCulumns 结果不匹配!");
						failed("insertBatchCulumns 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("insertBatchCulumns 结果不匹配!");
				String format = String.format("insertBatchCulumns 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 2, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试replace要求:id=1,cid=1,nickname=replace,pwd=replace */
	public abstract E replaceData();
	public void replace(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.replace(replaceData()).onSuccess(res -> {
			LOG.info("replace 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, "replace", "replace", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("replace 测试通过!");
						updateAllById(handler);
					} else {
						LOG.info("replace 结果不匹配!");
						failed("replace 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("replace 结果不匹配!");
				String format = String.format("replace 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试根据id修改所有,要求:id=1,cid=1,nickname=update,pwd=null */
	public abstract E updateAllByIdData();
	public void updateAllById(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateAllById(updateAllByIdData()).onSuccess(res -> {
			LOG.info("updateAllById 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, "update", null, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateAllById 测试通过!");
						updateAllByAssist(handler);
					} else {
						LOG.info("updateAllById 结果不匹配!");
						failed("updateAllById 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateAllById 结果不匹配!");
				String format = String.format("updateAllById 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试根据assist修改所有,要求:id=1,cid=1,nickname=null,pwd=updatebyAssist */
	public abstract E updateAllByAssistData();
	public void updateAllByAssist(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateAllByAssist(updateAllByAssistData(), new SqlAssist().andEq(COLUMN_ID, 1)).onSuccess(res -> {
			LOG.info("updateAllByAssist 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, null, "updatebyAssist", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateAllByAssist 测试通过!");
						updateNonEmptyById(handler);
					} else {
						LOG.info("updateAllByAssist 结果不匹配!");
						failed("updateAllByAssist 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateAllByAssist 结果不匹配!");
				String format = String.format("updateAllByAssist 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试根据id修改不为空,要求:id=1,cid=1,nickname=nickname,pwd=null */
	public abstract E updateNonEmptyByIdData();
	public void updateNonEmptyById(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateNonEmptyById(updateNonEmptyByIdData()).onSuccess(res -> {
			LOG.info("updateNonEmptyById 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, "nickname", "updatebyAssist", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateNonEmptyById 测试通过!");
						updateNonEmptyByAssist(handler);
					} else {
						LOG.info("updateNonEmptyById 结果不匹配!");
						failed("updateNonEmptyById 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateNonEmptyById 结果不匹配!");
				String format = String.format("updateNonEmptyById 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/** 测试根据assist修改不为空,要求:pwd=updateNonEmptyByAssist */
	public abstract E updateNonEmptyByAssistData();
	public void updateNonEmptyByAssist(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateNonEmptyByAssist(updateNonEmptyByAssistData(), new SqlAssist().andEq(COLUMN_ID, 1)).onSuccess(res -> {
			LOG.info("updateNonEmptyByAssist 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, "nickname", "updateNonEmptyByAssist", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateNonEmptyByAssist 测试通过!");
						updateSetNullById(handler);
					} else {
						LOG.info("updateNonEmptyByAssist 结果不匹配!");
						failed("updateNonEmptyByAssist 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateNonEmptyByAssist 结果不匹配!");
				String format = String.format("updateNonEmptyByAssist 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void updateSetNullById(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateSetNullById(1, Arrays.asList(COLUMN_NICKNAME)).onSuccess(res -> {
			LOG.info("updateSetNullById 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, null, "updateNonEmptyByAssist", test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateSetNullById 测试通过!");
						updateSetNullByAssist(handler);
					} else {
						LOG.info("updateSetNullById 结果不匹配!");
						failed("updateSetNullById 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateSetNullById 结果不匹配!");
				String format = String.format("updateSetNullById 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void updateSetNullByAssist(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.updateSetNullByAssist(new SqlAssist().andEq(COLUMN_ID, 1), Arrays.asList(COLUMN_PWD)).onSuccess(res -> {
			LOG.info("updateSetNullByAssist 执行结果:" + res);
			if (res == 1) {
				getByIdAndEquals(1, null, null, test -> {
					if (Objects.equals(true, test.result())) {
						LOG.info("updateSetNullByAssist 测试通过!");
						selectAll(handler);
					} else {
						LOG.info("updateSetNullByAssist 结果不匹配!");
						failed("updateSetNullByAssist 结果不匹配!" + test.cause().getMessage(), handler);
					}
				});
			} else {
				LOG.info("updateSetNullByAssist 结果不匹配!");
				String format = String.format("updateSetNullByAssist 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void selectAll(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.selectAll(new SqlAssist().andEq(COLUMN_PWD, "batch")).onSuccess(res -> {
			LOG.info("selectAll 执行结果:" + res);
			if (res.size() == 2) {
				LOG.info("selectAll 测试通过!");
				limitAll(handler);
			} else {
				LOG.info("selectAll 结果不匹配!");
				String format = String.format("selectAll 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 2, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void limitAll(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		sql.limitAll(new SqlAssist().andEq(COLUMN_PWD, "batch")).onSuccess(res -> {
			LOG.info("limitAll 执行结果:" + res);
			Object value = res.getValue(SqlLimitResult.DATA);
			if (value != null) {
				LOG.info("limitAll 测试通过!");
				limitAllNewName(handler);
			} else {
				LOG.info("limitAll 结果不匹配!");
				failed("limitAll 结果不匹配!\n期望结果:[non null]\n实际结果:[null]", handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void limitAllNewName(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		SqlLimitResult.registerResultKey(SqlLimitResult.DATA, "result");
		sql.limitAll(new SqlAssist().andEq(COLUMN_PWD, "batch")).onSuccess(res -> {
			LOG.info("limitAllNewName 执行结果:" + res);
			Object value = res.getValue("result");
			if (value != null) {
				LOG.info("limitAllNewName 测试通过!");
				selectById(handler);
			} else {
				LOG.info("limitAllNewName 结果不匹配!");
				failed("limitAllNewName 结果不匹配!\n期望结果:[non null]\n实际结果:[null]", handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void selectById(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		String cols = "t1.id,t2.classanme";
		String join = "inner join classes t2 on t1.cid=t2.id";
		sql.selectById(1, cols, "t1", join).onSuccess(res -> {
			LOG.info("selectById 执行结果:" + res);
			if (res.getString("classanme") != null) {
				LOG.info("selectById 测试通过!");
				selectSingleByObj(handler);
			} else {
				LOG.info("selectById 结果不匹配!");
				failed("selectById 结果不匹配!\n期望结果:[non null]\n实际结果:[null]", handler);
			}
		}).onFailure(err -> failed(err, handler));
	}
	/** 通过obj查询的数据,要求:cid=1 */
	public abstract E selectByObjData();
	public void selectSingleByObj(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		String cols = "t1.id,t2.classanme";
		String join = "inner join classes t2 on t1.cid=t2.id";
		sql.selectSingleByObj(selectByObjData(), cols, "t1", join).onSuccess(res -> {
			LOG.info("selectSingleByObj 执行结果:" + res);
			if (res.getString("classanme") != null) {
				LOG.info("selectSingleByObj 测试通过!");
				selectByObj(handler);
			} else {
				LOG.info("selectSingleByObj 结果不匹配!");
				failed("selectSingleByObj 结果不匹配!\n期望结果:[non null]\n实际结果:[null]", handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void selectByObj(Handler<AsyncResult<Integer>> handler) {
		System.out.println("=================================================");
		String cols = "t1.id,t2.classanme";
		String join = "inner join classes t2 on t1.cid=t2.id";
		sql.selectByObj(selectByObjData(), cols, "t1", join).onSuccess(res -> {
			LOG.info("selectByObj 执行结果:" + res);
			if (res.size() > 1) {
				LOG.info("selectByObj 测试通过!");
				deleteById(handler);
			} else {
				LOG.info("selectByObj 结果不匹配!");
				String format = String.format("selectByObj 结果不匹配!\n期望结果:[>%d]\n实际结果:[%d]", 1, res.size());
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void deleteById(Handler<AsyncResult<Integer>> handler) {
		sql.deleteById(1).onSuccess(res -> {
			LOG.info("deleteById 执行结果:" + res);
			if (res == 1) {
				LOG.info("deleteById 测试通过!");
				deleteByAssist(handler);
			} else {
				LOG.info("deleteById 结果不匹配!");
				String format = String.format("deleteById 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 1, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	public void deleteByAssist(Handler<AsyncResult<Integer>> handler) {
		sql.deleteByAssist(new SqlAssist().andEq(COLUMN_PWD, "batch")).onSuccess(res -> {
			LOG.info("deleteByAssist 执行结果:" + res);
			if (res == 2) {
				LOG.info("deleteByAssist 测试通过!");
				succeeded(handler);
			} else {
				LOG.info("deleteByAssist 结果不匹配!");
				String format = String.format("deleteByAssist 结果不匹配!\n期望结果:[%d]\n实际结果:[%d]", 2, res);
				failed(format, handler);
			}
		}).onFailure(err -> failed(err, handler));
	}

	/**
	 * 获取id并测试的数据是否匹配
	 * 
	 * @param id
	 *          查询的id
	 * @param nickname
	 *          期望的名称结果
	 * @param password
	 *          期望的密码结果
	 * @param handler
	 */
	public void getByIdAndEquals(Integer id, String nickname, String password, Handler<AsyncResult<Boolean>> handler) {
		sql.selectById(id, COLUMN_NICKNAME + "," + COLUMN_PWD + " AS " + KEY_PWD).onSuccess(res -> {
			LOG.info("getByIdAndEquals 执行结果:" + res + ",期望结果nickname=" + nickname + ",password=" + password);
			if (Objects.equals(nickname, res.getString(COLUMN_NICKNAME)) && Objects.equals(password, res.getString(KEY_PWD))) {
				LOG.info("getByIdAndEquals 测试通过!");
				handler.handle(Future.succeededFuture(true));
			} else {
				String format = String.format("\n期望结果:[nickname=%s,password=%s]\n实际结果:[nickname=%s,password=%s]", nickname, password,
						res.getString(COLUMN_NICKNAME), res.getString(KEY_PWD));
				handler.handle(Future.failedFuture(format));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}
	/**
	 * 获取数据行数并测试行数
	 * 
	 * @param assist
	 * @param value
	 *          期望的值
	 * @param handler
	 */
	public void getCountAndEquals(SqlAssist assist, long value, Handler<AsyncResult<Boolean>> handler) {
		sql.getCount(assist).onSuccess(res -> {
			LOG.info("getCountAndEquals 执行结果:" + res + ",期望结果=" + value);
			if (Objects.equals(value, res)) {
				LOG.info("getCountAndEquals 测试通过!");
				handler.handle(Future.succeededFuture(true));
			} else {
				String format = String.format("\n期望结果:[%d]\n实际结果:[%d]", value, res);
				handler.handle(Future.failedFuture(format));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

}
