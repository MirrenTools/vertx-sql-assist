# vertx-sql-assist
其他语言版本: [English](./README.md), [简体中文](./README.zh.md).

vertx-sql-assist是 [Vert.x](https://vertx.io/) 的SQL操作帮助工具,它提供了增删改查、连接、分页等支持,配合SqlAssist帮助类自己基本不用写一行SQL代码。

我们推荐你使用 [ScrewDriver](https://github.com/MirrenTools/screw-driver) 来生成代码,这样你会发现世界非常美好!
## 添加依赖
``` XML
<dependency>
  <groupId>org.mirrentools</groupId>
  <artifactId>vertx-sql-assist</artifactId>
  <version>RELEASE</version>
</dependency>
```
## SQL类方法说明
* **getCount** 获取数据总行数
* **selectAll** 查询多行数据
* **limitAll** 分页查询
* **selectById** 通过id查询数据
* **selectByObj** 通过对象中不为空的属性查询数据
* **selectSingleByObj** 通过对象中不为空的属性查询数据只取返回的第一行数据
* **insertBatch** 批量添加插入对象
* **insertAll** 插入一个对象包括属性值为null的值
* **insertNonEmpty** 插入一个对象,只插入对象中值不为null的属性
* **replace** 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
* **updateAllById** 更新一个对象中所有的属性包括null值,条件为对象中的主键值
* **updateAllByAssist** 更新一个对象中所有的属性包括null值,条件为SqlAssist帮助类
* **updateNonEmptyById** 更新一个对象中属性不为null值,条件为对象中的主键值
* **updateNonEmptyByAssist** 更新一个对象中属性不为null值,条件为SqlAssist帮助类
* **updateSetNullById** 通过主键值设置指定的列为空
* **updateSetNullByAssist** 通过Assist作为条件设置指定的列为空
* **deleteById** 通过主键值删除对应的数据行
* **deleteByAssist** 通过SqlAssist条件集删除对应的数据行
* **queryAsObj** 执行查询结果为JsonObject
* **queryAsListObj** 执行查询结果为JsonArray
* **queryAsListArray** 执行查询结果为ResultSet
* **update** 执行更新等操作得到受影响的行数
* **batch** 批量执行

## SqlAssist方法说明
* **setOrders** 设置排序,通过SqlAssist.order(列名,排序方式)
* **setGroupBy** 设置分组
* **setHaving** 设置分组条件
* **setDistincts** 设置是否去重,true去重
* **setPage** 设置第几页,该值仅在limitAll方法中有效,最终会被转换为startRow
* **setStartRow** 设置从第几行开始取数据
* **setRowSize** 设置每次取多少行数据
* **setResultColumn** 设置自定义返回列,多个列以,逗号隔开
* **setJoinOrReference** 设置连接查询或多表查询语句
* **and** 添加并且条件
* **or** 添加或者条件
* **andEq** 添加并且等于条件
* **orEq** 添加或者等于条件
* **andNeq** 添加并且不等于条件
* **orNeq** 添加或者不等于条件
* **andLt** 添加并且小于条件
* **orLt** 添加或者小于条件
* **andLte** 添加并且小于等于条件
* **orLte** 添加或者小于等于条件
* **andGt** 添加并且大于条件
* **orGt** 添加或者大于条件
* **andGte** 添加并且大于等于条件
* **orGte** 添加或者大于等于条件
* **andLike** 添加并且like条件
* **orLike** 添加或者like条件
* **andNotLike** 添加并且not like条件
* **orNotLike** 添加或者not like条件
* **andisNull** 添加并且is null条件
* **orisNull** 添加或者is null条件
* **andIsNotNull** 添加并且is not null条件
* **orIsNotNull** 添加或者is not null条件
* **setConditions** 添加查询条件
* **customCondition** 添加自定义查询条件

## SqlAssist使用示例
[示例项目](https://github.com/shenzhenMirren/vertx-sql-assist-examples)


``` java
// (1)创建Assist帮助类
SqlAssist assist = new SqlAssist();
// (2)添加条件type=1或3,相当于SQL: where type=1 or type=3
assist.orEq("type", 1).orEq("type", 3);
// (3)去掉重复的数据,相当于SQL: select distinct ...
assist.setDistincts(true);
// (4)自定义只返回id,type,name,seq列
assist.setResultColumn("id,type,name,seq");
// (5)通过seq类倒序排序,相当于SQL: order by seq desc
assist.setOrders(SqlAssist.order("seq", false));
// (6)获取数据库中第20-35行的数据,你也可以使用setPage(第几页)的方式进行分页获取,相当于SQL: limit 20,15
assist.setStartRow(20).setRowSize(15);
// (7)执行获取数据
Future future=Promise.promise().future();
future.setHandler(//处理结果);
itemsSQL.selectAll(assist,future);
```
具体使用方式可以查看SqlAssist类的方法注释,如果不清楚的可以在ScrewDriver群里咨询


## 使用方法

**示例**

1.创建实体类

``` java
//添加表注释
@Table("表的名称")
public class User {
  @TableId("主键id")
  private Long id;
  @TableColumn("列的名称")
  private String name;
  @TableColumn(value = "列的名称", alias = "列的别名,不是必须")
  private Integer type;
  //其他必须的
}  
```
2.创建SQL类并继承CommonSQL

``` java
public class UserSQL extends CommonSQL<User,JDBCClient> {//(1)
	public UserSQL(SQLExecute<JDBCClient> execute) {
		super(execute);
	}
  // (1)
  // User 必须是有@Table, @TableId,@TableColumn注解的实体类
  // JDBCClient 可以是别的数据库客户端
  //实现其他的方法
}  
```
3.执行

``` java
public static void main(String[] args) {
  // 其他已省略的变量
  UserSQL userSQL = new UserSQL(SQLExecute.createJDBC(jdbcClient));
  // 查询示例
  // 创建帮助类
  SqlAssist assist = new SqlAssist();
  assist.setStartRow(0).setRowSize(15);
  assist.andEq("type", 1);
  assist.setOrders(SqlAssist.order("id", true));
  // 执行查询
  userSQL.selectAll(assist,res->{
    if (res.succeeded()) {
      System.out.println(res.result());
    }else {
      System.err.println(res.cause());
    }
  });
  //保存示例
  User user =new User();
  user.setId(1001L);
  user.setName("org.mirrentools");
  user.setType(1);
  userSQL.insertNonEmpty(user,res->{//Processed results});
}
```
## 通用设置
**设置不同数据库SQL语句** 默认使用MySQL标准的SQL语句,你可以通过SQLStatement设置为不同的数据库SQL语句,支持MySQL、PostgreSQL、Oracle、DB2、SQL Server、SQLite,比如设置为Oracle你可以这样:
``` java
SQLStatement.register(OracleStatementSQL.class);
```
**设置分页返回结果名称** 分页获取数据返回的名称默认为:totals=数据总行数,pages=数据总页数 ,page=当前是第几页,size=每页显示多少行数据,data=数据,如果你要将名称改为其他的你可以这样:
``` java
SqlLimitResult.registerResultKey("totals", "counts");
```