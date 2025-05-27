package com.werhoz.mapzebraprinter.data.repository;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.UserEntity;
import com.werhoz.mapzebraprinter.network.ApiService;

import java.util.List;
import java.util.concurrent.Executors;

public class DataRepository {
    private final ApiService apiService;
    private final AppDatabase db;

    public interface SyncCallback {
        void onProgress(String message);
    }

    public DataRepository(ApiService apiService, AppDatabase db) {
        this.apiService = apiService;
        this.db = db;
    }

    public void syncAllTables(SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                callback.onProgress("Syncing users...");
                List<UserEntity> users = apiService.getUsers().execute().body();
                db.userDao().insertAll(users);
                callback.onProgress("Users saved.");

//                callback.onProgress("Syncing posts...");
//                List<PostEntity> posts = apiService.getPosts().execute().body();
//                db.postDao().insertAll(posts);
//                callback.onProgress("Posts saved.");
//
//                callback.onProgress("Syncing comments...");
//                List<CommentEntity> comments = apiService.getComments().execute().body();
//                db.commentDao().insertAll(comments);
//                callback.onProgress("Comments saved.");

                callback.onProgress("✅ All tables synced successfully!");
            } catch (Exception e) {
                callback.onProgress("❌ Sync failed: " + e.getMessage());
            }
        });
    }
}

