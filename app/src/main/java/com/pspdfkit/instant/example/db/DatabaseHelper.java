/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Helper for documents database operations. */
class DatabaseHelper extends SQLiteOpenHelper {

    // Tables.
    public static final String TABLE_DOCUMENTS = "documents";
    public static final String TABLE_LAYERS = "layers";
    // Table columns.
    public static final String KEY_DOCUMENT_ID = "document_id";
    public static final String KEY_JWT = "authentication_token";
    public static final String KEY_DOCUMENT_TITLE = "title";
    public static final String KEY_LAYER_NAME = "layer_name";
    // Database info.
    private static final String DB_NAME = "web_example_db";
    private static final int DB_VERSION = 2;

    @Nullable
    private static DatabaseHelper instance;

    @NonNull
    public static synchronized DatabaseHelper newInstance(
            @NonNull final Context context, @Nullable final String databaseName) {
        final String dbName = databaseName == null ? DB_NAME : databaseName;

        if (instance != null && instance.databaseName.equals(databaseName)) {
            return instance;
        } else {
            return new DatabaseHelper(context.getApplicationContext(), dbName);
        }
    }

    @NonNull
    private final String databaseName;

    private DatabaseHelper(@NonNull final Context context, @NonNull final String databaseName) {
        super(context, databaseName, null, DB_VERSION);
        this.databaseName = databaseName;
    }

    private static void removeInstanceWithName(@NonNull final String databaseName) {
        if (instance != null && instance.databaseName.equals(databaseName)) {
            instance = null;
        }
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        createDocumentsTable(db);
        createLayersTable(db);
    }

    private void createDocumentsTable(@NonNull final SQLiteDatabase db) {
        final String CREATE_DOCUMENTS_TABLE = "CREATE TABLE "
                + TABLE_DOCUMENTS
                + "("
                + KEY_DOCUMENT_ID
                + " TEXT NOT NULL PRIMARY KEY,"
                + KEY_DOCUMENT_TITLE
                + " TEXT NOT NULL )";
        db.execSQL(CREATE_DOCUMENTS_TABLE);
    }

    private void createLayersTable(@NonNull final SQLiteDatabase db) {
        final String CREATE_LAYERS_TABLE = "CREATE TABLE "
                + TABLE_LAYERS
                + "("
                + KEY_DOCUMENT_ID
                + " TEXT NOT NULL,"
                + KEY_LAYER_NAME
                + " TEXT,"
                + KEY_JWT
                + " TEXT NOT NULL )";
        db.execSQL(CREATE_LAYERS_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
            createDocumentsTable(db);
            createLayersTable(db);
        }
    }

    public void deleteDatabase(@NonNull final Context context) {
        context.deleteDatabase(databaseName);
        removeInstanceWithName(databaseName);
    }
}
