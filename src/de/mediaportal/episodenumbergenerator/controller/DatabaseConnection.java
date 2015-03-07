package de.mediaportal.episodenumbergenerator.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mediaportal.episodenumbergenerator.MPEpisodeNumberGenerator;
import de.mediaportal.episodenumbergenerator.model.Config;

/**
 * The singleton class DatabaseConnection handles every connection to
 * MediaPortal's MySQL database and provides PreparedStatements for SELECT and
 * UPDATE commands
 * 
 * @author Oliver
 *
 */
public class DatabaseConnection {
	/**
	 * Singleton-Instance of the class {@link DatabaseConnection}
	 */
	private static DatabaseConnection instance = null;

	/**
	 * Connection-Object
	 */
	protected Connection connection = null;

	/**
	 * Database name as defined in settings.properties field
	 * <code>mediaportaldbname</code>
	 */
	private String dbName = null;

	/**
	 * Database host as defined in settings.properties field
	 * <code>mediaportaldbhost</code>
	 */
	private String dbHost = null;

	/**
	 * Database user as defined in settings.properties field
	 * <code>mediaportaldbuser</code>
	 */
	private String mediaportaldbuser = null;

	/**
	 * Database password as defined in settings.properties field
	 * <code>mediaportaldbpassword</code>
	 */
	private String mediaportaldbpassword = null;

	/**
	 * Database path as defined in settings.properties field
	 * <code>mysqldatabasepath</code> mysqldatabasepath
	 */
	private String mysqlBinPath = null;

	/**
	 * Backup path as defined in settings.properties field
	 * <code>backuppath</code> mysqldatabasepath
	 */
	private String backupPath = null;

	/**
	 * @return Singleton instance of the class
	 * @throws SQLException
	 *             Thrown if connection to database cannot be established
	 * @throws ClassNotFoundException
	 *             Thrown if MySQL driver is not found
	 */
	public synchronized static DatabaseConnection getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}

	/**
	 * Private constructor of singleton class {@link DatabaseConnection}
	 * 
	 * @throws SQLException
	 *             Thrown if connection to database cannot be established
	 * @throws ClassNotFoundException
	 *             Thrown if MySQL driver is not found
	 */
	private DatabaseConnection() throws ClassNotFoundException, SQLException {
		Config config = MPEpisodeNumberGenerator.getConfig();
		dbHost = config.getProperty(Config.FIELD_MPDB_DBHOST);
		dbName = config.getProperty(Config.FIELD_MPDB_NAME);
		mediaportaldbuser = config.getProperty(Config.FIELD_MPDB_USER);
		mediaportaldbpassword = config.getProperty(Config.FIELD_MPDB_PASSWORD);
		mysqlBinPath = config.getProperty(Config.FIELD_MPDB_DBPATH);
		backupPath = config.getProperty(Config.FIELD_MPDB_BACKUP_PATH);

		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connection = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/" + dbName + "?" + "user=" + mediaportaldbuser + "&password="
				+ mediaportaldbpassword);

	}

	/**
	 * Closes the database connection
	 */
	public void close() {
		try {

			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Creates a {@link PreparedStatement} to find all episodes in mediaportal's
	 * database which have a description starting with the episodeIndicator
	 * 
	 * @param episodeIndicator
	 *            Beginning of description text in EPG table
	 * @return PreparedStatement object
	 * @throws SQLException
	 *             if a database access error occurs or this method is called on
	 *             a closed connection
	 */
	public PreparedStatement getSelectEpgTableStatement(String episodeIndicator) throws SQLException {
		String sql = "SELECT * FROM mptvdb.program WHERE seriesNum = '' AND description LIKE ? AND title NOT LIKE '%Making-of' ORDER BY title";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, episodeIndicator + "%");
		return statement;
	}

	/**
	 * Creates a {@link PreparedStatement} to count all episodes in
	 * mediaportal's database which have a description starting with the
	 * episodeIndicator
	 * 
	 * @param episodeIndicator
	 *            Beginning of description text in EPG table
	 * @return PreparedStatement object
	 * @throws SQLException
	 *             if a database access error occurs or this method is called on
	 *             a closed connection
	 */
	public PreparedStatement getSelectEpgTableCountStatement(String episodeIndicator) throws SQLException {
		String sql = "SELECT COUNT(*) FROM mptvdb.program WHERE seriesNum = '' AND description LIKE ? AND title NOT LIKE '%Making-of' ORDER BY title";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, episodeIndicator + "%");
		return statement;
	}

	/**
	 * Creates a {@link PreparedStatement} to update one line in MediaPortal's
	 * program table
	 * 
	 * @param programId
	 *            Unique ID of the program to be updated
	 * @param seriesNumber
	 *            Series Number
	 * @param episodeNumber
	 *            Episode Number
	 * @return PreparedStatement object
	 * @throws SQLException
	 *             if a database access error occurs or this method is called on
	 *             a closed connection
	 */
	public PreparedStatement updateEpgEpisodeAndSeriesNumber(int programId, String seriesNumber, String episodeNumber) throws SQLException {
		String sql = "UPDATE mptvdb.program SET seriesNum = ?,episodeNum = ? WHERE idProgram = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, seriesNumber);
		statement.setString(2, episodeNumber);
		statement.setInt(3, programId);

		return statement;

	}

	/**
	 * Dumps MediaPortal Database to hard disk to make it possible to restore
	 * data in case of a problem
	 * 
	 * @return completion code of the dump process
	 * @throws IOException
	 *             Is thrown if a problem occours writing to the backup file
	 * @throws InterruptedException
	 *             Is thrown if the wait command to let the application know,
	 *             that the dump is written, is interrupted
	 */
	public int dumpDatabase() throws IOException, InterruptedException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = sdf.format(new Date());
		File backupDirectory = new File("bak/");
		if (!backupDirectory.exists()) {
			backupDirectory.mkdirs();
		}
		String executeCmd = mysqlBinPath + "mysqldump -u " + mediaportaldbuser + " -p" + mediaportaldbpassword + " " + dbName + " -r "
				+ backupPath + timestamp + "_backup.sql";
		Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
		int processComplete = runtimeProcess.waitFor();
		return processComplete;
	}
}
