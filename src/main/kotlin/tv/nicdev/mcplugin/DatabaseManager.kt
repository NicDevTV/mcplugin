package tv.nicdev.mcplugin

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class DatabaseManager(dataFolder: File) {
    private val folder: File = dataFolder
    private val conn: Connection

    init {
        if (!folder.exists()) folder.mkdirs()
        val dbFile = File(folder, "vanish.db")
        val url = "jdbc:sqlite:${dbFile.absolutePath.replace("\\","/")}" // normalize path
        conn = DriverManager.getConnection(url)
        createTables()
    }

    private fun createTables() {
        val sqlNotify = """
            CREATE TABLE IF NOT EXISTS notify_settings (
                uuid TEXT PRIMARY KEY,
                enabled INTEGER NOT NULL
            );
        """.trimIndent()
        conn.createStatement().use { it.execute(sqlNotify) }

        val sqlVanish = """
            CREATE TABLE IF NOT EXISTS vanish_status (
                uuid TEXT PRIMARY KEY,
                vanished INTEGER NOT NULL
            );
        """.trimIndent()
        conn.createStatement().use { it.execute(sqlVanish) }
    }

    fun setNotify(uuid: UUID, enabled: Boolean) {
        val sql = "REPLACE INTO notify_settings(uuid, enabled) VALUES(?,?)"
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, uuid.toString())
            ps.setInt(2, if (enabled) 1 else 0)
            ps.executeUpdate()
        }
    }

    fun getNotify(uuid: UUID): Boolean? {
        val sql = "SELECT enabled FROM notify_settings WHERE uuid = ?"
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeQuery().use { rs ->
                return if (rs.next()) {
                    rs.getInt("enabled") != 0
                } else null
            }
        }
    }

    fun setVanish(uuid: UUID, vanished: Boolean) {
        val sql = "REPLACE INTO vanish_status(uuid, vanished) VALUES(?,?)"
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, uuid.toString())
            ps.setInt(2, if (vanished) 1 else 0)
            ps.executeUpdate()
        }
    }

    fun getVanish(uuid: UUID): Boolean? {
        val sql = "SELECT vanished FROM vanish_status WHERE uuid = ?"
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeQuery().use { rs ->
                return if (rs.next()) {
                    rs.getInt("vanished") != 0
                } else null
            }
        }
    }

    fun close() {
        try { conn.close() } catch (_: Throwable) {}
    }
}
