/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.pspdfkit.example.instant.api.WebExampleDocumentDescriptor;
import com.pspdfkit.example.instant.api.WebExampleDocumentLayerDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * API for documents database store.
 */
public class WebExampleDocumentsDatabase {

    private static final String LOG_TAG = "DocumentsDatabase";

    private final DatabaseHelper databaseHelper;

    public WebExampleDocumentsDatabase(@NonNull Context context) {
        this(context, null);
    }

    private WebExampleDocumentsDatabase(@NonNull Context context, @Nullable String databaseName) {
        databaseHelper = DatabaseHelper.newInstance(context, databaseName);
    }

    /**
     * Adds list of documents to the database.
     */
    public void addDocuments(@NonNull List<WebExampleDocumentDescriptor> documents) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            addDocumentsInternal(db, documents);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error adding documents to the database.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void addDocumentsInternal(@NonNull SQLiteDatabase db,
                                      @NonNull List<WebExampleDocumentDescriptor> documents) {
        for (WebExampleDocumentDescriptor document : documents) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_DOCUMENT_ID, document.documentId);
            contentValues.put(DatabaseHelper.KEY_DOCUMENT_TITLE, document.title);
            db.insertWithOnConflict(DatabaseHelper.TABLE_DOCUMENTS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            // Put document layers into layers table.
            for (WebExampleDocumentLayerDescriptor layer : document.layers) {
                final ContentValues layerContentValues = new ContentValues();
                layerContentValues.put(DatabaseHelper.KEY_DOCUMENT_ID, document.documentId);
                layerContentValues.put(DatabaseHelper.KEY_LAYER_NAME, layer.layerName);
                layerContentValues.put(DatabaseHelper.KEY_JWT, layer.jwt);
                db.insertWithOnConflict(DatabaseHelper.TABLE_LAYERS, null, layerContentValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /**
     * Replaces documents in the database.
     */
    public void replaceDocuments(@NonNull List<WebExampleDocumentDescriptor> documents) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_DOCUMENTS, null, null);
            db.delete(DatabaseHelper.TABLE_LAYERS, null, null);
            addDocumentsInternal(db, documents);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error replacing documents.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Removes all documents from the database.
     */
    public void removeAllDocuments() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_DOCUMENTS, null, null);
            db.delete(DatabaseHelper.TABLE_LAYERS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deleting all documents.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Returns list of documents stored in the database.
     */
    @NonNull
    public List<WebExampleDocumentDescriptor> getDocuments() {
        final String SELECT_ALL_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_DOCUMENTS;

        final List<WebExampleDocumentDescriptor> documents = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery(SELECT_ALL_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String documentId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DOCUMENT_ID));
                    String documentTitle = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DOCUMENT_TITLE));
                    List<WebExampleDocumentLayerDescriptor> layers = getLayers(documentId);
                    documents.add(new WebExampleDocumentDescriptor(documentId, documentTitle, layers));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving all documents from the database.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return documents;
    }

    @NonNull
    private List<WebExampleDocumentLayerDescriptor> getLayers(@NonNull String documentId) {
        final List<WebExampleDocumentLayerDescriptor> layers = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final Cursor cursor = db.query(
            DatabaseHelper.TABLE_LAYERS,
            null,
            DatabaseHelper.KEY_DOCUMENT_ID + " = ?",
            new String[] {documentId},
            null,
            null,
            null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String authenticationToken = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_JWT));
                    String layerName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_LAYER_NAME));
                    layers.add(new WebExampleDocumentLayerDescriptor(documentId, layerName, authenticationToken));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving document layers from the database.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return layers;
    }

    /**
     * Deletes the whole database.
     */
    public void deleteDatabase(@NonNull Context context) {
        databaseHelper.deleteDatabase(context);
    }
}
