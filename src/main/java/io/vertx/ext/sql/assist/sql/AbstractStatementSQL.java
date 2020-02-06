package io.vertx.ext.sql.assist.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.assist.SQLStatement;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.SqlWhereCondition;
import io.vertx.ext.sql.assist.Table;
import io.vertx.ext.sql.assist.TableColumn;
import io.vertx.ext.sql.assist.TableId;

/**
 * 抽象数据库操作语句,默认以MySQL标准来编写,如果其他数据库可以基础并重写不兼容的方法<br>
 * 通常不支持limit分页的数据库需要重写{@link #selectAllSQL(SqlAssist)}与{@link #selectByObjSQL(Object, String, String, boolean)}这两个方法
 * 
 * @author <a href="https://mirrentools.org/">Mirren</a>
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
		Table table = entity.getAnnotation(Table.class);
		if (table == null || table.value().isEmpty()) {
			throw new NullPointerException(entity.getName() + " no Table annotation ,you need to set @Table on the class");
		}
		this.sqlTableName = table.value();
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
		if (!hasCol) {
			throw new NullPointerException(entity.getName() + " no TableId annotation ,you need to set @TableId on the field");
		}
		this.sqlResultColumns = column.substring(1);
	}

	/**
	 * 获取表名称
	 * 
	 * @return
	 */
	public String getSqlTableName() {
		return sqlTableName;
	}

	/**
	 * 设置表名称
	 * 
	 * @param sqlTableName
	 * @return
	 */
	public AbstractStatementSQL setSqlTableName(String sqlTableName) {
		this.sqlTableName = sqlTableName;
		return this;
	}

	/**
	 * 获取主键名称
	 * 
	 * @return
	 */
	public String getSqlPrimaryId() {
		return sqlPrimaryId;
	}

	/**
	 * 设置主键名称,当有多个主键时可以重写给方法设置以哪一个主键为主
	 * 
	 * @param sqlPrimaryId
	 * @return
	 */
	public AbstractStatementSQL setSqlPrimaryId(String sqlPrimaryId) {
		this.sqlPrimaryId = sqlPrimaryId;
		return this;
	}

	/**
	 * 获取表返回列
	 * 
	 * @return
	 */
	public String getSqlResultColumns() {
		return sqlResultColumns;
	}

	/**
	 * 设置表返回列
	 * 
	 * @param sqlResultColumns
	 * @return
	 */
	public AbstractStatementSQL setSqlResultColumns(String sqlResultColumns) {
		this.sqlResultColumns = sqlResultColumns;
		return this;
	}

	/**
	 * 表的所有列名与列名对应的值
	 * 
	 * @return
	 * @throws Exception
	 */
	protected <T> List<SqlPropertyValue<?>> getPropertyValue(T obj) throws Exception {
		Field[] fields = obj.getClass().getDeclaredFields();
		List<SqlPropertyValue<?>> result = new ArrayList<>();
		;
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
		StringBuilder sql = new StringBuilder(String.format("select count(*) from %s ", getSqlTableName()));
		JsonArray params = null;
		if (assist != null) {
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				params = new JsonArray();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.add(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.add(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.add(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.add(value);
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
			SqlAndParams result = new SqlAndParams(String.format("select %s from %s ", getSqlResultColumns(), getSqlTableName()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		} else {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? getSqlResultColumns() : assist.getResultColumn();// 表的列名
			// 初始化SQL语句
			StringBuilder sql = new StringBuilder(String.format("select %s %s from %s", distinct, column, getSqlTableName()));
			JsonArray params = null;// 参数
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				params = new JsonArray();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.add(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.add(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.add(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.add(value);
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
					if (params == null) {
						params = new JsonArray();
					}
					params.addAll(assist.getHavingValue());
				}
			}
			if (assist.getOrder() != null) {
				sql.append(assist.getOrder());
			}
			if (assist.getRowSize() != null || assist.getStartRow() != null) {
				if (params == null) {
					params = new JsonArray();
				}
				if (assist.getStartRow() != null) {
					sql.append(" LIMIT ?");
					params.add(assist.getRowSize());
				}
				if (assist.getStartRow() != null) {
					sql.append(" OFFSET ?");
					params.add(assist.getStartRow());
				}
			}
			SqlAndParams result = new SqlAndParams(sql.toString(), params);
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		}
	}

	@Override
	public <S> SqlAndParams selectByIdSQL(S primaryValue, String resultColumns, String joinOrReference) {
		String sql = String.format("select %s from %s %s where %s = ? ", (resultColumns == null ? getSqlResultColumns() : resultColumns), getSqlTableName(),
				(joinOrReference == null ? "" : joinOrReference), getSqlPrimaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByIdSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams selectByObjSQL(T obj, String resultColumns, String joinOrReference, boolean single) {
		StringBuilder sql = new StringBuilder(
				String.format("select %s from %s %s ", (resultColumns == null ? getSqlResultColumns() : resultColumns), getSqlTableName(), (joinOrReference == null ? "" : joinOrReference)));
		JsonArray params = null;
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
					params = new JsonArray();
					sql.append(String.format("where %s = ? ", pv.getName()));
					params.add(pv.getValue());
					isFrist = false;
				} else {
					sql.append(String.format("and %s = ? ", pv.getName()));
					params.add(pv.getValue());
				}
			}
		}
		if (single) {
			sql.append(" LIMIT 1");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams insertAllSQL(T obj) {
		JsonArray params = null;
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
				params = new JsonArray();
			} else {
				tempColumn.append("," + pv.getName());
				tempValues.append(",?");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", getSqlTableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertAllSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams insertNonEmptySQL(T obj) {
		JsonArray params = null;
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
					params = new JsonArray();
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.add(pv.getValue());
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", getSqlTableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql, params);
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
		JsonArray param0 = new JsonArray();
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
			param0.add(pv.getValue());
		}
		List<JsonArray> params = new ArrayList<>();
		params.add(param0);
		for (int i = 1; i < list.size(); i++) {
			JsonArray paramx = new JsonArray();
			List<SqlPropertyValue<?>> propertyValue1;
			try {
				propertyValue1 = getPropertyValue(list.get(i));
			} catch (Exception e) {
				return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
			}
			for (SqlPropertyValue<?> pv : propertyValue1) {
				paramx.add(pv.getValue());
			}
			params.add(paramx);
		}

		String sql = String.format("insert into %s (%s) values (%s) ", getSqlTableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		return qp;
	}

	@Override
	public SqlAndParams insertBatchSQL(List<String> columns, List<JsonArray> params) {
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
		String sql = String.format("insert into %s (%s) values (%s) ", getSqlTableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		return qp;
	}

	@Override
	public <T> SqlAndParams replaceSQL(T obj) {
		JsonArray params = null;
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
					params = new JsonArray();
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.add(pv.getValue());
			}
		}
		String sql = String.format("replace into %s (%s) values (%s) ", getSqlTableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("replaceSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateAllByIdSQL(T obj) {
		if (getSqlPrimaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getName().equals(getSqlPrimaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (tempColumn == null) {
				params = new JsonArray();
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		if (tempIdValue == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		params.add(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", getSqlTableName(), tempColumn, getSqlPrimaryId());
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
		JsonArray params = null;
		StringBuilder tempColumn = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (tempColumn == null) {
				params = new JsonArray();
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", getSqlTableName(), tempColumn, whereStr == null ? "" : whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAllByAssistSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <T> SqlAndParams updateNonEmptyByIdSQL(T obj) {
		if (getSqlPrimaryId() == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (SqlPropertyValue<?> pv : propertyValue) {
			if (pv.getName().equals(getSqlPrimaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					params = new JsonArray();
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.add(pv.getValue());
			}
		}
		if (tempColumn == null || tempIdValue == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no set update value or no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no set update value or no primary key in your SQL statement");
		}
		params.add(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", getSqlTableName(), tempColumn, getSqlPrimaryId());
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
		JsonArray params = null;
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
					params = new JsonArray();
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.add(pv.getValue());
			}
		}
		if (tempColumn == null) {
			return new SqlAndParams(false, "The object has no value");
		}

		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", getSqlTableName(), tempColumn, whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateNonEmptyByAssistSQL : " + result.toString());
		}
		return result;
	}

	@Override
	public <S> SqlAndParams updateSetNullByIdSQL(S primaryValue, List<String> columns) {
		if (getSqlPrimaryId() == null) {
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
		String sql = String.format("update %s set %s where %s = ? ", getSqlTableName(), setStr.toString(), getSqlPrimaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
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
		JsonArray params = new JsonArray();
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", getSqlTableName(), setStr.toString(), whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateSetNullByAssist : " + result.toString());
		}
		return result;
	}

	@Override
	public <S> SqlAndParams deleteByIdSQL(S primaryValue) {
		if (getSqlPrimaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		String sql = String.format("delete from %s where %s = ? ", getSqlTableName(), getSqlPrimaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
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
		JsonArray params = new JsonArray();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		;
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("delete from %s %s", getSqlTableName(), whereStr);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteByAssistSQL : " + result.toString());
		}
		return result;
	}

}
