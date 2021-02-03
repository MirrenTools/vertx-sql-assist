package io.vertx.ext.sql.assist;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.assist.entity.Student;
import io.vertx.ext.sql.assist.sql.StudentSQL;
import io.vertx.jdbcclient.JDBCPool;

public class JdbcPoolTest extends TestSuite<Student, JDBCPool> {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		JsonObject config = new JsonObject();
		// config.put("url",
		// "jdbc:mysql://10.0.0.79:3306/orion_api_manager?useUnicode=true&useSSL=false&serverTimezone=UTC")
		// .put("driver_class", "com.mysql.cj.jdbc.Driver").put("user",
		// "root").put("password", "root");
		config.put("url", String.format("jdbc:sqlite:%s/data/sqlite.db", System.getProperty("user.dir"))).put("driver_class",
				"org.sqlite.JDBC");
		JDBCPool jdbcPool = JDBCPool.pool(vertx, config);
		StudentSQL sql = new StudentSQL(SQLExecute.createJDBC(jdbcPool));
		JdbcPoolTest test = new JdbcPoolTest(sql, config);
		test.runTest(res -> {
			if (res.succeeded()) {
				System.out.println("succeeded:" + res.result());
			} else {
				System.err.println("failed:" + res.cause());
			}
			System.exit(0);
		});
	}

	public JdbcPoolTest(StudentSQL sql, JsonObject config) {
		super(sql, config);
	}

	@Override
	public Student insertAllData() {
		Student student = new Student();
		student.setId(1);
		student.setCid(1);
		student.setNickname("nickname");
		student.setPwd("pwd");
		return student;
	}

	@Override
	public Student updateAllByIdData() {
		Student student = new Student();
		student.setId(1);
		student.setCid(1);
		student.setNickname("nickname1");
		return student;
	}

	@Override
	public Object selectByIdData() {
		return 1;
	}

	@Override
	public String selectByIdTestKey() {
		return "nickname";
	}

	@Override
	public Object selectByIdTestValue() {
		return "nickname1";
	}

	@Override
	public String selectByIdTestNullKey() {
		return "possword";
	}

	@Override
	public Student insertNonEmptyData() {
		Student student = new Student();
		student.setId(2);
		student.setCid(1);
		return student;
	}

	@Override
	public Object selectByIdResultColumnsData() {
		return 1;
	}

	@Override
	public String selectByIdResultColumnsColumns() {
		return " id,pwd AS p";
	}

	@Override
	public String selectByIdResultColumnsTestKey() {
		return "id";
	}

	@Override
	public Object selectByIdResultColumnsTestValue() {
		return 1;
	}

	@Override
	public String selectByIdResultColumnsTestValueNullKey() {
		return "pwd";
	}

	@Override
	public String selectByIdResultColumnsTestNullKey() {
		return "nickname";
	}

	@Override
	public Student insertNonEmptyGeneratedKeysData() {
		Student student = new Student();
		student.setCid(1);
		return student;
	}

	@Override
	public Student selectSingleByObjData(int id) {
		return new Student().setId(id);
	}

	@Override
	public String selectSingleByObjTestKey() {
		return "cid";
	}

	@Override
	public Object selectSingleByObjTestValue() {
		return 1;
	}

}
