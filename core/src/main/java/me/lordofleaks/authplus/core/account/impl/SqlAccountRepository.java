package me.lordofleaks.authplus.core.account.impl;

import com.zaxxer.hikari.HikariDataSource;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.account.AccountRepositoryException;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SqlAccountRepository implements AccountRepository {

    private DataSource dataSource;

    protected void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract Executor getExecutor();

    @Override
    public void close() {
        if(dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                throw new AccountRepositoryException("Cannot close repository", e);
            }
        }
    }

    protected CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS `authplus_account` (" +
                        "`id_most` BIGINT NOT NULL, " +
                        "`id_least` BIGINT NOT NULL, " +
                        "`name` VARCHAR(16) UNIQUE NOT NULL, " +
                        "`password` BLOB(64) NOT NULL, " +
                        "`salt` BLOB(16) NOT NULL, " +
                        "`premium` TINYINT(1) NOT NULL, " +
                        "PRIMARY KEY(`id_most`, `id_least`)" +
                        ");");
            } catch (Exception e) {
                throw new AccountRepositoryException("Cannot initialize repository", e);
            }
        }, getExecutor());
    }

    @Override
    public CompletableFuture<Account> getAccountByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `authplus_account`" +
                         " WHERE `id_most`=? AND `id_least`=?;")) {
                stmt.setLong(1, uuid.getMostSignificantBits());
                stmt.setLong(2, uuid.getLeastSignificantBits());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Account acc = new Account(uuid);
                        acc.setName(rs.getString("name"));
                        acc.setRegisteredPremium(rs.getBoolean("premium"));
                        acc.setPassword(rs.getBytes("password"));
                        acc.setSalt(rs.getBytes("salt"));
                        return acc;
                    }
                    return null;
                }
            } catch (Exception e) {
                throw new AccountRepositoryException("Cannot get account by uuid", e);
            }
        }, getExecutor());
    }

    @Override
    public CompletableFuture<Void> insertAccount(Account account) {
        String name = account.getName();
        byte[] passwd = account.getPassword().clone();
        byte[] salt = account.getSalt().clone();
        boolean premium = account.isRegisteredPremium();
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO" +
                         " `authplus_account`(`id_most`,`id_least`,`name`,`password`,`salt`,`premium`) VALUES(?,?,?,?,?,?)")) {
                stmt.setLong(1, account.getUniqueId().getMostSignificantBits());
                stmt.setLong(2, account.getUniqueId().getLeastSignificantBits());
                stmt.setString(3, name);
                stmt.setBytes(4, passwd);
                stmt.setBytes(5, salt);
                stmt.setBoolean(6, premium);
                stmt.execute();
            } catch (Exception e) {
                throw new AccountRepositoryException("Cannot get account by uuid", e);
            }
        }, getExecutor());
    }

    @Override
    public CompletableFuture<Void> updateAccount(Account account) {
        byte[] passwd = account.getPassword().clone();
        byte[] salt = account.getSalt().clone();
        boolean premium = account.isRegisteredPremium();
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE `authplus_account`" +
                         " SET `name`=?,`password`=?,`salt`=?,`premium`=? WHERE `id_most`=? AND `id_least`=?")) {
                stmt.setString(1, account.getName());
                stmt.setBytes(2, passwd);
                stmt.setBytes(3, salt);
                stmt.setBoolean(4, premium);
                stmt.setLong(5, account.getUniqueId().getMostSignificantBits());
                stmt.setLong(6, account.getUniqueId().getLeastSignificantBits());
                stmt.execute();
            } catch (Exception e) {
                throw new AccountRepositoryException("Cannot get account by uuid", e);
            }
        }, getExecutor());
    }
}