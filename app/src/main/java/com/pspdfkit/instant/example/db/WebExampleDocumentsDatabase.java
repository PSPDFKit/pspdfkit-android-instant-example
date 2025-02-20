/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.pspdfkit.instant.example.api.WebExampleDocumentDescriptor;
import com.pspdfkit.instant.example.api.WebExampleDocumentLayerDescriptor;
import java.util.ArrayList;
import java.util.List;

/** API for documents database store. */
public class WebExampleDocumentsDatabase {

    private static final String LOG_TAG = "DocumentsDatabase";

    private final DatabaseHelper databaseHelper;

    public WebExampleDocumentsDatabase(@NonNull final Context context) {
        this(context, null);
    }

    private WebExampleDocumentsDatabase(@NonNull final Context context, @Nullable final String databaseName) {
        databaseHelper = DatabaseHelper.newInstance(context, databaseName);
    }

    /** Adds list of documents to the database. */
    public void addDocuments(@NonNull final List<WebExampleDocumentDescriptor> documents) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            addDocumentsInternal(db, documents);
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Error adding documents to the database.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void addDocumentsInternal(
            @NonNull final SQLiteDatabase db, @NonNull final List<WebExampleDocumentDescriptor> documents) {
        for (final WebExampleDocumentDescriptor document : documents) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_DOCUMENT_ID, document.documentId);
            contentValues.put(DatabaseHelper.KEY_DOCUMENT_TITLE, document.title);
            db.insertWithOnConflict(
                    DatabaseHelper.TABLE_DOCUMENTS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            // Put document layers into layers table.
            for (final WebExampleDocumentLayerDescriptor layer : document.layers) {
                final ContentValues layerContentValues = new ContentValues();
                layerContentValues.put(DatabaseHelper.KEY_DOCUMENT_ID, document.documentId);
                layerContentValues.put(DatabaseHelper.KEY_LAYER_NAME, layer.layerName);
                layerContentValues.put(DatabaseHelper.KEY_JWT, layer.jwt);
                db.insertWithOnConflict(
                        DatabaseHelper.TABLE_LAYERS, null, layerContentValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /** Replaces documents in the database. */
    public void replaceDocuments(@NonNull final List<WebExampleDocumentDescriptor> documents) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_DOCUMENTS, null, null);
            db.delete(DatabaseHelper.TABLE_LAYERS, null, null);
            addDocumentsInternal(db, documents);
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Error replacing documents.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /** Removes all documents from the database. */
    public void removeAllDocuments() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_DOCUMENTS, null, null);
            db.delete(DatabaseHelper.TABLE_LAYERS, null, null);
            db.setTransactionSuccessful();
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Error deleting all documents.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /** Returns list of documents stored in the database. */
    @NonNull
    public List<WebExampleDocumentDescriptor> getDocuments() {
        final String SELECT_ALL_QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_DOCUMENTS;

        final List<WebExampleDocumentDescriptor> documents = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery(SELECT_ALL_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    final int idN = cursor.getColumnIndex(DatabaseHelper.KEY_DOCUMENT_ID);
                    final int titleN = cursor.getColumnIndex(DatabaseHelper.KEY_DOCUMENT_TITLE);
                    if (idN >= 0 && titleN >= 0) {
                        final String documentId = cursor.getString(idN);
                        final String documentTitle = cursor.getString(titleN);
                        final List<WebExampleDocumentLayerDescriptor> layers = getLayers(documentId);
                        documents.add(new WebExampleDocumentDescriptor(documentId, documentTitle, layers));
                    }
                } while (cursor.moveToNext());
            }
        } catch (final Exception e) {
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
    private List<WebExampleDocumentLayerDescriptor> getLayers(@NonNull final String documentId) {
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
                    final int jwtN = cursor.getColumnIndex(DatabaseHelper.KEY_JWT);
                    final int nameN = cursor.getColumnIndex(DatabaseHelper.KEY_LAYER_NAME);
                    if (jwtN >= 0 && nameN >= 0) {
                        final String authenticationToken = cursor.getString(jwtN);
                        final String layerName = cursor.getString(nameN);
                        layers.add(new WebExampleDocumentLayerDescriptor(documentId, layerName, authenticationToken));
                    }
                } while (cursor.moveToNext());
            }
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Error retrieving document layers from the database.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return layers;
    }

    /** Deletes the whole database. */
    public void deleteDatabase(@NonNull final Context context) {
        databaseHelper.deleteDatabase(context);
    }
}
