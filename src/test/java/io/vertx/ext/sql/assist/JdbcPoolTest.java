package io.vertx.ext.sql.assist;

import java.util.Arrays;
import java.util.List;

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
		test.runTest(res -> System.exit(0));
	}

	public JdbcPoolTest(StudentSQL sql, JsonObject config) {
		super(sql, config);
	}

	@Override
	public Student insertAllData() {
		return new Student().setId(1).setCid(1).setNickname("nickname");
	}
	@Override
	public Student insertNonEmptyData() {
		return new Student().setId(2).setCid(1);
	}

	@Override
	public int insertNonEmptyGeneratedKeysResultPoolType() {
		return 0;
	}

	@Override
	public Student insertNonEmptyGeneratedKeysData() {
		return new Student().setCid(1).setNickname("nickname").setPwd("pwd");
	}

	@Override
	public List<Student> insertBatchData() {
		return Arrays.asList(new Student().setCid(1).setPwd("batch"), new Student().setCid(1).setPwd("batch"));
	}

	@Override
	public Student replaceData() {
		return new Student().setId(1).setCid(1).setNickname("replace").setPwd("replace");
	}

	@Override
	public Student updateAllByIdData() {
		return new Student().setId(1).setCid(1).setNickname("update");
	}

	@Override
	public Student updateAllByAssistData() {
		return new Student().setId(1).setCid(1).setPwd("updatebyAssist");
	}

	@Override
	public Student updateNonEmptyByIdData() {
		return new Student().setId(1).setCid(1).setNickname("nickname");
	}

	@Override
	public Student updateNonEmptyByAssistData() {
		return new Student().setPwd("updateNonEmptyByAssist");
	}

	@Override
	public Student selectByObjData() {
		return new Student().setCid(1);
	}

}
