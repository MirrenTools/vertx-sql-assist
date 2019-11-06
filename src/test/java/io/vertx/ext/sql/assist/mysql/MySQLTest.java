package io.vertx.ext.sql.assist.mysql;

import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.User;

public class MySQLTest {
	public static void main(String[] args) {
		UserSQL sql = new UserSQL();
		SqlAssist assist = new SqlAssist().andEq("id", 1L);
		assist.setOrders(SqlAssist.order("id", true));
		assist.setGroupBy("name");
		User user = new User();
		user.setId(1L);
		user.setName("name");
		user.setPwd("pwd");
		SqlAndParams countSQL = sql.getCountSQL(assist);
		System.out.println(countSQL);
		SqlAndParams selectAllSQL = sql.selectAllSQL(assist);
		System.out.println(selectAllSQL);
		SqlAndParams selectByIdSQL = sql.selectByIdSQL(1L, "test", "as u inner join table t u.id=t.id");
		System.out.println(selectByIdSQL);
		SqlAndParams selectByObjSQL = sql.selectByObjSQL(user, null, null, true);
		System.out.println(selectByObjSQL);
		SqlAndParams insertNonEmptySQL = sql.insertNonEmptySQL(user);
		System.out.println(insertNonEmptySQL);
		SqlAndParams updateNonEmptyByIdSQL = sql.updateNonEmptyByIdSQL(user);
		System.out.println(updateNonEmptyByIdSQL);
		SqlAndParams deleteByIdSQL = sql.deleteByIdSQL(1L);
		System.out.println(deleteByIdSQL);
	}
}
