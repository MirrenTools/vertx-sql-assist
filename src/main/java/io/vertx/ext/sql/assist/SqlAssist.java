package io.vertx.ext.sql.assist;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * SQL查询帮助类
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class SqlAssist {
	/** 去重 */
	private String distinct;
	/** 分组 */
	private String groupBy;
	/** 分组条件 */
	private String having;
	/** 分组条件值 */
	private JsonArray havingValue;
	/** 排序 */
	private String order;
	/** 数据分页第几页 */
	private Integer page;
	/** 数据分页开始行 */
	private Integer startRow;
	/** 每次取多少行数据 */
	private Integer rowSize;
	/** 设置自定义返回列 */
	private String resultColumn;
	/** 连接语句或多表查询语句 */
	private String joinOrReference;
	/** 条件集 */
	private List<SqlWhereCondition<?>> condition = null;
	/** 自定义属性 */
	private Object custom;

	public SqlAssist() {
		super();
	}

	/**
	 * 该构造方法用于使用SqlAssist的静态条件方法,动态添加条件
	 * 
	 * @param require
	 *          示例:查询等于使用 SqlWhereCondition.andEq("A.ID",10)...
	 *          {@link SqlWhereCondition#andEq(String, Object)}
	 */
	public SqlAssist(SqlWhereCondition<?>... require) {
		this.setConditions(require);
	}

	/**
	 * 将当前对象装换为json字符串
	 * 
	 * @return
	 */
	public String toJsonStr() {
		return toJson().toString();
	}

	/**
	 * 将当前对象装换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (distinct != null) {
			json.put("distinct", distinct);
		}
		if (groupBy != null) {
			json.put("groupBy", groupBy);
		}
		if (having != null) {
			json.put("having", having);
		}
		if (havingValue != null) {
			json.put("havingValue", havingValue);
		}
		if (order != null) {
			json.put("order", order);
		}
		if (page != null) {
			json.put("page", page);
		}
		if (startRow != null) {
			json.put("startRow", startRow);
		}
		if (rowSize != null) {
			json.put("rowSize", rowSize);
		}
		if (resultColumn != null) {
			json.put("resultColumn", resultColumn);
		}
		if (joinOrReference != null) {
			json.put("joinOrReference", joinOrReference);
		}
		if (custom != null) {
			json.put("custom", custom);
		}
		if (condition != null) {
			JsonArray array = new JsonArray();
			condition.forEach(va -> {
				array.add(va.toJson());
			});
			json.put("condition", array);
		}
		return json;
	}

	/**
	 * 将JsonObject对象装换为SqlAssist
	 * 
	 * @param obj
	 * @return
	 */
	public static SqlAssist fromJson(JsonObject obj) {
		if (obj == null || obj.isEmpty()) {
			return null;
		}
		SqlAssist assist = new SqlAssist();
		assist.setPage(obj.getInteger("page"));
		assist.setStartRow(obj.getInteger("startRow"));
		assist.setRowSize(obj.getInteger("rowSize"));
		assist.setDistinct(obj.getString("distinct"));
		assist.setGroupBy(obj.getString("groupBy"));
		assist.setHaving(obj.getString("having"), obj.getJsonArray("havingValue"));
		assist.setOrders(obj.getString("order"));
		assist.setResultColumn(obj.getString("resultColumn"));
		assist.setJoinOrReference(obj.getString("joinOrReference"));
		assist.setCustom(obj.getValue("custom"));
		if (obj.getValue("condition") instanceof JsonArray) {
			List<SqlWhereCondition<?>> list = new ArrayList<>();
			obj.getJsonArray("condition").forEach(va -> {
				list.add(SqlWhereCondition.fromJson((JsonObject) va));
			});
			assist.setCondition(list);
		}
		return assist;
	}

	/**
	 * 添加单个查询条件,参数为{@link SqlWhereCondition} ,推荐使用SqlWhereCondition的静态条件方法添加条件;
	 * 
	 * @param require
	 *          示例:查询等于使用 SqlWhereCondition.andEq("A.ID",10)...
	 *          {@link SqlWhereCondition#andEq(String, Object)}
	 */
	public SqlAssist setConditions(SqlWhereCondition<?> require) {
		if (this.condition == null) {
			this.condition = new ArrayList<SqlWhereCondition<?>>();
			require.setRequire(require.getRequire().replace("and ", " ").replace("or ", " "));
		}
		this.condition.add(require);
		return this;
	}

	/**
	 * 添加多个查询条件,参数为{@ SqlWhereCondition},推荐使用SqlWhereCondition的静态条件方法添加条件;
	 * 
	 * @param require
	 *          示例:查询等于使用 SqlWhereCondition.andEq("A.ID",10)...
	 *          {@link SqlWhereCondition#andEq(String, Object)}
	 */
	public SqlAssist setConditions(SqlWhereCondition<?>... require) {
		if (this.condition == null) {
			this.condition = new ArrayList<SqlWhereCondition<?>>();
		}
		for (int i = 0; i < require.length; i++) {
			if (i == 0 && this.condition.size() == 0) {
				require[i].setRequire(require[i].getRequire().replace("and ", " ").replace("or ", " "));
			}
			this.condition.add(require[i]);
		}
		return this;
	}

	/**
	 * 添加查询条件,该方法一般用于初始化,因为会将现有的条件集清空,既this.condition=conditions
	 * 
	 * @param conditions
	 * @return
	 */
	private SqlAssist setCondition(List<SqlWhereCondition<?>> conditions) {
		this.condition = conditions;
		return this;
	}

	/**
	 * 添加并且条件,列名与表达式<br>
	 * 示例: column = id IS NULL<br>
	 * SQL: [and] id IS NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist and(String column) {
		this.setConditions(SqlWhereCondition.and(column));
		return this;
	}

	/**
	 * 添加或者条件,列名与表达式<br>
	 * 示例: column = id IS NULL<br>
	 * SQL: [or] id IS NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist or(String column) {
		this.setConditions(SqlWhereCondition.or(column));
		return this;
	}

	/**
	 * 添加并且条件,列名与表达式<br>
	 * 示例: column = id<br>
	 * SQL: [and] id IS NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist andIsNull(String column) {
		this.setConditions(SqlWhereCondition.andIsNull(column));
		return this;
	}

	/**
	 * 添加或者条件,列名与表达式<br>
	 * 示例: column = id<br>
	 * SQL: [or] id IS NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist orIsNull(String column) {
		this.setConditions(SqlWhereCondition.orIsNull(column));
		return this;
	}

	/**
	 * 添加并且条件,列名与表达式<br>
	 * 示例: column = id<br>
	 * SQL: [and] id IS NOT NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist andIsNotNull(String column) {
		this.setConditions(SqlWhereCondition.andIsNotNull(column));
		return this;
	}

	/**
	 * 添加或者条件,列名与表达式<br>
	 * 示例: column = id<br>
	 * SQL: [or] id IS NOT NULL
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @return
	 */
	public <T> SqlAssist orIsNotNull(String column) {
		this.setConditions(SqlWhereCondition.orIsNotNull(column));
		return this;
	}

	/**
	 * 添加并且等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id = 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andEq(String column, T value) {
		this.setConditions(SqlWhereCondition.andEq(column, value));
		return this;
	}

	/**
	 * 添加或者等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id = 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orEq(String column, T value) {
		this.setConditions(SqlWhereCondition.orEq(column, value));
		return this;
	}

	/**
	 * 添加并且不等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id <> 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andNeq(String column, T value) {
		this.setConditions(SqlWhereCondition.andNeq(column, value));
		return this;
	}

	/**
	 * 添加或者不等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id <> 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orNeq(String column, T value) {
		this.setConditions(SqlWhereCondition.orNeq(column, value));
		return this;
	}

	/**
	 * 添加并且小于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id < 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andLt(String column, T value) {
		this.setConditions(SqlWhereCondition.andLt(column, value));
		return this;
	}

	/**
	 * 添加或者小于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id < 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orLt(String column, T value) {
		this.setConditions(SqlWhereCondition.orLt(column, value));
		return this;
	}

	/**
	 * 添加并且小于等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id <= 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andLte(String column, T value) {
		this.setConditions(SqlWhereCondition.andLte(column, value));
		return this;
	}

	/**
	 * 添加或者小于等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id <= 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orLte(String column, T value) {
		this.setConditions(SqlWhereCondition.orLte(column, value));
		return this;
	}

	/**
	 * 添加并且大于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id > 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andGt(String column, T value) {
		this.setConditions(SqlWhereCondition.andGt(column, value));
		return this;
	}

	/**
	 * 添加或者大于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id > 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orGt(String column, T value) {
		this.setConditions(SqlWhereCondition.orGt(column, value));
		return this;
	}

	/**
	 * 添加并且大于等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [and] id >= 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andGte(String column, T value) {
		this.setConditions(SqlWhereCondition.andGte(column, value));
		return this;
	}

	/**
	 * 添加或者大于等于条件<br>
	 * 示例: column = id ,value =1<br>
	 * SQL: [or] id >= 1
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orGte(String column, T value) {
		this.setConditions(SqlWhereCondition.orGte(column, value));
		return this;
	}

	/**
	 * 添加并且like条件<br>
	 * 示例: column = id ,value =%1%<br>
	 * SQL: [and] id like %1%
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andLike(String column, T value) {
		this.setConditions(SqlWhereCondition.andLike(column, value));
		return this;
	}

	/**
	 * 添加或者like条件<br>
	 * 示例: column = id ,value =%1%<br>
	 * SQL: [or] id like %1%
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orLike(String column, T value) {
		this.setConditions(SqlWhereCondition.orLike(column, value));
		return this;
	}

	/**
	 * 添加并且like条件<br>
	 * 示例: column = id ,value =%1%<br>
	 * SQL: [and] id not like %1%
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist andNotLike(String column, T value) {
		this.setConditions(SqlWhereCondition.andNotLike(column, value));
		return this;
	}

	/**
	 * 添加或者like条件<br>
	 * 示例: column = id ,value =%1%<br>
	 * SQL: [or] id not like %1%
	 * 
	 * @param column
	 *          列名:如果表中存在相同列名使用表名.列名
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist orNotLike(String column, T value) {
		this.setConditions(SqlWhereCondition.orNotLike(column, value));
		return this;
	}

	/**
	 * 添加自定义查询条件 :<br>
	 * 示例: prefix= and id in(select tid from table where w=?) , value=1<br>
	 * SQL: and id in(select tid from table where w=1)
	 * 
	 * @param prefix
	 *          SQL语句
	 * @param value
	 *          条件值
	 * @return
	 */
	public <T> SqlAssist customCondition(String prefix, T value) {
		this.setConditions(SqlWhereCondition.customCondition(prefix, value));
		return this;
	}

	/**
	 * 自定义查询条件 :<br>
	 * 示例 :prefix=and id in(?,?,?),value=1,2,3<br>
	 * SQL: and id in(1,2,3)
	 * 
	 * @param prefix
	 *          SQL语句
	 * @param value
	 *          条件值
	 * @return
	 */
	public SqlAssist customCondition(String prefix, Object... value) {
		this.setConditions(SqlWhereCondition.customCondition(prefix, value));
		return this;
	}

	/**
	 * 获得一个排序对象,将(列名)参数1 按 参数2排序(true=ASC/false=DESC)<br>
	 * ;如果表中存在相同列名使用表名.列名,如果不存在相同列名可以直接列名<br>
	 * 
	 * @param column
	 *          列名
	 * @param mode
	 *          排序类型,true=asc,false=desc
	 * @return
	 */
	public static String order(String column, boolean mode) {
		if (mode) {
			return column + " asc ";
		} else {
			return column + " desc ";
		}
	}

	/**
	 * 设置排序,通过SqlAssist.order(列名,排序方式)<br>
	 * 示例:assist.setOrder(SqlAssist.order("id",true))//将id正序排序
	 * 
	 * @param order
	 *          {@link #order(String column, boolean mode)}
	 * @return
	 */
	public SqlAssist setOrders(String... order) {
		if (order == null || order.length == 0) {
			this.order = null;
			return this;
		}
		if (this.order == null) {
			this.order = " order by ";
		}
		StringBuffer buffer = new StringBuffer();
		for (String od : order) {
			buffer.append(od);
		}
		this.order += buffer;
		return this;
	}

	/**
	 * 设置一个编辑好的order,该方法一般用于初始化,因为会将现有的order清除,既order=参数order
	 * 
	 * @param order
	 * @return
	 */
	private SqlAssist setOrders(String order) {
		this.order = order;
		return this;
	}

	/**
	 * 获得排序
	 * 
	 * @return
	 */
	public String getOrder() {
		return order;
	}

	/**
	 * 获取分组
	 * 
	 * @return
	 */
	public String getGroupBy() {
		return groupBy;
	}

	/**
	 * 设置分组 示例传入: id,type<br>
	 * SQL: GROUP BY id,type
	 * 
	 * @param groupBy
	 *          要分组的列名比如id,type
	 * @return
	 */
	public SqlAssist setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
	}

	/**
	 * 获取分组条件
	 * 
	 * @return
	 */
	public String getHaving() {
		return having;
	}

	/**
	 * 设置分组条件<br>
	 * 示例: having= id > ? and type=? values=[1,2]<br>
	 * SQL: having id>1 and type=2
	 * 
	 * @param having
	 *          表达式,值以?号占位,示例: id > ? and type=?
	 * @param values
	 *          传入值 new JsonArray().add(1).add(2)
	 * @return
	 */
	public SqlAssist setHaving(String having, JsonArray values) {
		this.having = having;
		this.havingValue = values;
		return this;
	}

	/**
	 * 获取Having的值
	 * 
	 * @return
	 */
	public JsonArray getHavingValue() {
		return havingValue;
	}

	/**
	 * 获得是否去重
	 * 
	 * @return
	 */
	public String getDistinct() {
		return distinct;
	}

	/**
	 * 设置是否去重,true去重,false无效
	 * 
	 * @param distinct
	 */
	public SqlAssist setDistincts(boolean distinct) {
		if (distinct) {
			this.distinct = "distinct";
			return this;
		}
		return this;
	}

	/**
	 * 设置一个现有的去除方案,该方法一般用于初始化,因为会将现有的distinct清除,既distinct=传入的distinct
	 * 
	 * @param distinct
	 * @return
	 */
	private SqlAssist setDistinct(String distinct) {
		this.distinct = distinct;
		return this;
	}

	/**
	 * 获得开始分页行
	 * 
	 * @return
	 */
	public Integer getStartRow() {
		return startRow;
	}

	/**
	 * 获取第几页
	 * 
	 * @return
	 */
	public Integer getPage() {
		return page;
	}

	/**
	 * 设置第几页,该值仅在limitAll方法中有效,最终会被转换为startRow
	 * 
	 * @param page
	 * @return
	 */
	public SqlAssist setPage(Integer page) {
		this.page = page;
		return this;
	}

	/**
	 * 设置从第几行开始取数据
	 * 
	 * @param startRow
	 */
	public SqlAssist setStartRow(Integer startRow) {
		this.startRow = startRow;
		return this;
	}

	/**
	 * 获得每次取多少行数据
	 * 
	 * @return
	 */
	public Integer getRowSize() {
		return rowSize;
	}

	/**
	 * 设置每次取多少行数据
	 * 
	 * @param rowSize
	 */
	public SqlAssist setRowSize(Integer rowSize) {
		this.rowSize = rowSize;
		return this;
	}

	/**
	 * 获得返回指定列
	 * 
	 * @return
	 */
	public String getResultColumn() {
		return resultColumn;
	}

	/**
	 * 设置自定义返回列,多个列以,逗号隔开
	 * 
	 * @return
	 */
	public SqlAssist setResultColumn(String resultColumn) {
		this.resultColumn = resultColumn;
		return this;
	}

	/**
	 * 获取连接查询或多表查询语句
	 * 
	 * @return
	 */
	public String getJoinOrReference() {
		return joinOrReference;
	}

	/**
	 * 设置连接查询或多表查询语句<br>
	 * 多表示例: ,table2,table3<br>
	 * 连接示例: inner join table2 on table1.id=table2.id
	 * 
	 * @param joinOrReference
	 *          连接语句
	 * @return
	 */
	public SqlAssist setJoinOrReference(String joinOrReference) {
		this.joinOrReference = joinOrReference;
		return this;
	}

	/**
	 * 获得条件集
	 * 
	 * @return
	 */
	public List<SqlWhereCondition<?>> getCondition() {
		return condition;
	}

	/**
	 * 获得自定义属性
	 * 
	 * @return
	 */
	public Object getCustom() {
		return custom;
	}

	/**
	 * 设置自定义属性
	 * 
	 * @param custom
	 */
	public void setCustom(Object custom) {
		this.custom = custom;
	}

	@Override
	public String toString() {
		return "SqlAssist [distinct=" + distinct + ", order=" + order + ", startRow=" + startRow + ", rowSize=" + rowSize + ", resultColumn=" + resultColumn + ", condition=" + condition + ", custom="
				+ custom + "]";
	}
}
