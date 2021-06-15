package me.lordofleaks.authplus.core.account.impl;

import me.lordofleaks.authplus.core.account.AccountRepository;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqliteAccountRepository extends SqlAccountRepository {

    private final ExecutorService exec;

    public SqliteAccountRepository(String file) {
        this.exec = Executors.newSingleThreadExecutor();
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + file);
        setDataSource(ds);
        initialize();
    }

    @Override
    public void close() {
        super.close();
        exec.shutdown();
    }

    @Override
    protected @NotNull Executor getExecutor() {
        return exec;
    }
}