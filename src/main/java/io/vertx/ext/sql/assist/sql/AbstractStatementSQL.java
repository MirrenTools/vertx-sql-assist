package io.vertx.ext.sql.assist.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.sql.assist.SQLStatement;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.SqlWhereCondition;
import io.vertx.ext.sql.assist.Table;
import io.vertx.ext.sql.assist.TableColumn;
import io.vertx.ext.sql.assist.TableId;
import io.vertx.sqlclient.Tuple;

/**
 * 抽象数据库操作语句,默认以MySQL标准来编写,如果其他数据库可以基础并重写不兼容的方法<br>
 * 通常不支持limit分页的数据库需要重写{@link #selectAllSQL(SqlAssist)}与{@link #selectByObjSQL(Object, String, String, boolean)}这两个方法
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 * @param <T>
 */
public abstract class AbstractStatementSQL implements SQLStatement {
	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(AbstractStatementSQL.class);
	/** 表的名称 */
	private String sqlTableName;
	/** 主键的名称 */
	private String sqlPrimaryId;
	/** 返回列 */
	private String sqlResultColumns;

	public AbstractStatementSQL(Class<?> entity) {
		super();
		if (tableName() == null) {
			Table table = entity.getAnnotation(Table.class);
			if (table == null || table.value().isEmpty()) {
				throw new NullPointerException(entity.getName() + " no Table annotation ,you need to set @Table on the class");
			}
			this.sqlTableName = table.value();
		}
		if (primaryId() == null || resultColumns() == null) {
			boolean hasId = false;
			boolean hasCol = false;
			Field[] fields = entity.getDeclaredFields();
			StringBuilder column = new StringBuilder();
			for (Field field : fields) {
				field.setAccessible(true);
				TableId tableId = field.getAnnotation(TableId.class);
				TableColumn tableCol = field.getAnnotation(TableColumn.class);
				if (tableId == null && tableCol == null) {
					continue;
				}
				if (tableId != null) {
					if (tableId.value() == null || tableId.value().isEmpty()) {
						continue;
					}
					if (this.sqlPrimaryId != null) {
						this.sqlPrimaryId += tableId.value();
					} else {
						this.sqlPrimaryId = tableId.value();
					}
					hasId = true;
					column.append("," + tableId.value());
					if (tableId.alias() != null && !tableId.alias().isEmpty()) {
						column.append(" AS \"" + tableId.alias() + "\"");
					}
				} else {
					if (tableCol == null) {
						continue;
					}
					column.append("," + tableCol.value());
					if (tableCol.alias() != null && !tableCol.alias().isEmpty()) {
						column.append(" AS \"" + tableCol.alias() + "\"");
					}
					hasCol = true;
				}
			}
			if (!hasId) {
				throw new NullPointerException(entity.getName() + " no TableId annotation ,you need to set @TableId on the field");
			}
			if (!hasCol && !hasId) {
				throw new NullPointerException(entity.getName() + " no TableColumn annotation ,you need to set @TableColumn on the field");
			}
			this.sqlResultColumns = column.substring(1);
		}

	}

	/**
	 * 获取表名称
	 * 
	 * @return
	 */
	public String tableName() {
		return sqlTableName;
	}

	/**
	 * 获取主键名称
	 * 
	 * @return
	 */
	public String primaryId() {
		return sqlPrimaryId;
	}

	/**
	 * 获取表返回列
	 * 
	 * @return
	 */
	public String resultColumns() {
		return sqlResultColumns;
	}

	/**
	 * 表的所有列名与列名对应的值
	 * 
	 * @return
	 * @throws Exception
	 */
	protected <T> List<SqlPropertyValue<?>> getPropertyValue(T obj) throws Exception {
		Field[] fields = obj.getClass().getDeclaredFields();
		List<SqlPropertyValue<?>> result = new ArrayList<>();;
		for (Field field : fields) {
			field.setAccessible(true);
			TableId tableId = field.getAnnotation(TableId.class);
			TableColumn tableCol = field.getAnnotation(TableColumn.class);
			if (tableId == null && tableCol == null) {
				continue;
			}
			if (tableId != null) {
				result.add(0, new SqlPropertyValue<>(tableId.value(), field.get(obj)));
			} else {
				result.add(new SqlPropertyValue<>(tableCol.value(), field.get(obj)));
			}
		}
		return result;
	}

	@Override
	public SqlAndParams getCountSQL(SqlAssist assist) {
		StringBuilder sql = new StringBuilder(String.format("select count(*) from %s ", tableName()));
		Tuple params = null;
		if (assist != null) {
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				params = Tuple.tuple();
				List<SqlWhereCondition<?>> where = assist.getCondition();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.addValue(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.addValue(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.addValue(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.addValue(value);
						}
					}
				}
			}
			if (assist.getGroupBy() != null) {
				sql.append(" group by " + assist.getGroupBy() + " ");
			}
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("getCountSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public SqlAndParams selectAllSQL(SqlAssist assist) {
		// 如果Assist为空返回默认默认查询语句,反则根据Assist生成语句sql语句
		if (assist == null) {
			SqlAndParams result = new SqlAndParams(String.format("select %s from %s ", resultColumns(), tableName()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		} else {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? resultColumns() : assist.getResultColumn();// 表的列名
			// 初始化SQL语句
			StringBuilder sql = new StringBuilder(String.format("select %s %s from %s", distinct, column, tableName()));
			Tuple params = Tuple.tuple();// 参数
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.addValue(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.addValue(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.addValue(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.addValue(value);
						}
					}
				}
			}
			if (assist.getGroupBy() != null) {
				sql.append(" group by " + assist.getGroupBy() + " ");
			}
			if (assist.getHaving() != null) {
				sql.append(" having " + assist.getHaving() + " ");
				if (assist.getHavingValue() != null) {
					for (Object value : assist.getHavingValue()) {
						params.addValue(value);
					}
				}
			}
			if (assist.getOrder() != null) {
				sql.append(assist.getOrder());
			}
			if (assist.getRowSize() != null || assist.getStartRow() != null) {
				if (assist.getStartRow() != null) {
					sql.append(" LIMIT ?");
					params.addValue(assist.getRowSize());
				}
				if (assist.getStartRow() != null) {
					sql.append(" OFFSET ?");
					params.addValue(assist.getStartRow());
				}
			}
			SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		}
	}

	@Override
	public <S> SqlAndParams selectByIdSQL(S primaryValue, String resultColumns, String joinOrReference) {
		String sql = String.format("select %s from %s %s where %s = ? ", (resultColumns == null ? resultColumns() : resultColumns), tableName(),
				(joinOrReference == null ? "" : joinOrReference), primaryId());
		SqlAndParams result = new SqlAndParams(sql, Tuple.of(primaryValue));
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByIdSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams selectByObjSQL(T obj, String resultColumns, String joinOrReference, boolean single) {
		StringBuilder sql = new StringBuilder(String.format("select %s from %s %s ", (resultColumns == null ? resultColumns() : resultColumns),
				tableName(), (joinOrReference == null ? "" : joinOrReference)));
		Tuple params = Tuple.tuple();
		boolean isFrist = true;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (int i = propertyValue.size() - 1; i >= 0; i--) {
			SqlPropertyValue<?> pv = propertyValue.get(i);
			if (pv.getValue() != null) {
				if (isFrist) {
					sql.append(String.format("where %s = ? ", pv.getName()));
					isFrist = false;
				} else {
					sql.append(String.format("and %s = ? ", pv.getName()));
				}
				params.addValue(pv.getValue());
			}
		}
		if (single) {
			sql.append(" LIMIT 1");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams insertAllSQL(T obj) {
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName());
				tempValues = new StringBuilder("?");
			} else {
				tempColumn.append("," + pv.getName());
				tempValues.append(",?");
			}
			params.addValue(pv.getValue());
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertAllSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams insertNonEmptySQL(T obj) {
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName());
					tempValues = new StringBuilder("?");
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.addValue(pv.getValue());
			}
		}
		if (tempColumn == null || tempValues == null) {
			return new SqlAndParams(false, "The column or value is null");
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql, (params.size() <= 0 ? null : params));
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertNonEmptySQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams insertBatchSQL(List<T> list) {
		if (list == null || list.isEmpty()) {
			return new SqlAndParams(false, "The param can not be null or empty");
		}
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		Tuple param0 = Tuple.tuple();
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(list.get(0));
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName());
				tempValues = new StringBuilder("?");
			} else {
				tempColumn.append("," + pv.getName());
				tempValues.append(",?");
			}
			param0.addValue(pv.getValue());
		}
		List<Tuple> params = new ArrayList<>();
		params.add(param0);
		for (int i = 1; i < list.size(); i++) {
			Tuple paramx = Tuple.tuple();
			List<SqlPropertyValue<?>> propertyValue1;
			try {
				propertyValue1 = getPropertyValue(list.get(i));
			} catch (Exception e) {
				return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
			}
			for (SqlPropertyValue<?> pv : propertyValue1) {
				paramx.addValue(pv.getValue());
			}
			params.add(paramx);
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		return qp;
	}

	@Override
	public SqlAndParams insertBatchSQL(List<String> columns, List<Tuple> params) {
		if ((columns == null || columns.isEmpty()) || (params == null || params.isEmpty())) {
			return new SqlAndParams(false, "The columns and params can not be null or empty");
		}
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		for (String column : columns) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(column);
				tempValues = new StringBuilder("?");
			} else {
				tempColumn.append("," + column);
				tempValues.append(",?");
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		return qp;
	}

	@Override
	public <T> SqlAndParams replaceSQL(T obj) {
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName());
					tempValues = new StringBuilder("?");
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.addValue(pv.getValue());
			}
		}
		if (tempColumn == null || tempValues == null) {
			return new SqlAndParams(false, "The column or value is null");
		}
		String sql = String.format("replace into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
		if (LOG.isDebugEnabled()) {
			LOG.debug("replaceSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateAllByIdSQL(T obj) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getName().equals(primaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			params.addValue(pv.getValue());
		}
		if (tempIdValue == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		params.addValue(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", tableName(), tempColumn, primaryId());
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAllByIdSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateAllByAssistSQL(T obj, SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			params.addValue(pv.getValue());
		}
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.addValue(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.addValue(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.addValue(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.addValue(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), tempColumn, whereStr == null ? "" : whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAllByAssistSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateNonEmptyByIdSQL(T obj) {
		if (primaryId() == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getName().equals(primaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.addValue(pv.getValue());
			}
		}
		if (tempColumn == null || tempIdValue == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no set update value or no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no set update value or no primary key in your SQL statement");
		}
		params.addValue(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", tableName(), tempColumn, primaryId());
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateNonEmptyByIdSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateNonEmptyByAssistSQL(T obj, SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		Tuple params = Tuple.tuple();
		StringBuilder tempColumn = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.addValue(pv.getValue());
			}
		}

		if (tempColumn == null) {
			return new SqlAndParams(false, "The object has no value");
		}

		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.addValue(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.addValue(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.addValue(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.addValue(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), tempColumn, whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateNonEmptyByAssistSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <S> SqlAndParams updateSetNullByIdSQL(S primaryValue, List<String> columns) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}

		if (columns == null || columns.size() == 0) {
			return new SqlAndParams(false, "Columns cannot be null or empty");
		}
		StringBuilder setStr = new StringBuilder();
		setStr.append(" " + columns.get(0) + " = null ");
		for (int i = 1; i < columns.size(); i++) {
			setStr.append(", " + columns.get(i) + " = null ");
		}
		String sql = String.format("update %s set %s where %s = ? ", tableName(), setStr.toString(), primaryId());
		SqlAndParams result = new SqlAndParams(sql, Tuple.of(primaryValue));
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateSetNullById : " + result.toString());
		}
		return result;
	}

	@Override
	public <S> SqlAndParams updateSetNullByAssistSQL(SqlAssist assist, List<String> columns) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		if (columns == null || columns.size() == 0) {
			return new SqlAndParams(false, "Columns cannot be null or empty");
		}
		StringBuilder setStr = new StringBuilder();
		setStr.append(" " + columns.get(0) + " = null ");
		for (int i = 1; i < columns.size(); i++) {
			setStr.append(", " + columns.get(i) + " = null ");
		}
		Tuple params = Tuple.tuple();
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.addValue(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.addValue(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.addValue(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.addValue(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), setStr.toString(), whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateSetNullByAssist : " + result.toString());
		}
		return result;
	}

	@Override
	public <S> SqlAndParams deleteByIdSQL(S primaryValue) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		String sql = String.format("delete from %s where %s = ? ", tableName(), primaryId());
		SqlAndParams result = new SqlAndParams(sql, Tuple.of(primaryValue));
		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteByIdSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public SqlAndParams deleteByAssistSQL(SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		List<SqlWhereCondition<?>> where = assist.getCondition();
		Tuple params = Tuple.tuple();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.addValue(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.addValue(value);
			}
		} ;
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.addValue(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.addValue(value);
				}
			}
		}
		String sql = String.format("delete from %s %s", tableName(), whereStr);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteByAssistSQL : " + result.toString());
		}
		return result;
	}

}
