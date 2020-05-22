package io.github.spigotjs.libraries;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class MySQL {

	public static MySQLConnection createConnection(String url, String user, String password) {
		return new MySQLConnection(url, user, password);
	}
	

	public static class MySQLConnection {
		
		private String host;
		private String user;
		private String password;
		
		public MySQLConnection(String host, String user, String password) {
			super();
			this.host = host;
			this.user = user;
			this.password = password;
		}

		private Connection connection;
		
		/**
		 * Connect
		 */
		public void connect(Consumer<SQLException> consumer) {
			try {
				this.connection = DriverManager.getConnection(host, user, password);
				consumer.accept(null);
			} catch (SQLException e) {
				e.printStackTrace();
				consumer.accept(e);
			}
		}
		
		/**
		 * Update
		 * @param query
		 * @param done
		 */
		public void update(String update, Consumer<SQLException> done) {
			try {
				connection.prepareStatement(update).execute();
				done.accept(null);
			} catch (SQLException e) {
				e.printStackTrace();
				done.accept(e);
			}
		}
		
		/**
		 * Query
		 * @param query
		 * @param done
		 */
		public void query(String query, Consumer<ResultSet> done) {
			try {
				ResultSet rs = connection.prepareStatement(query).executeQuery();
				done.accept(rs);
			} catch (SQLException e) {
				e.printStackTrace();
				done.accept(null);
			}
		}
		
		/**
		 * Disconnect
		 */
		public void end(Consumer<SQLException> done) {
			try {
				connection.close();
				done.accept(null);
			} catch (SQLException e) {
				e.printStackTrace();
				done.accept(e);
			}
		}
	}

}
