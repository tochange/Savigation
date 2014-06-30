package com.tochange.yang.lib.db;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private String tableName;

    private String[] filels;

    public DatabaseHelper(Context context, String dbName, String tableName,
            String[] fields, CursorFactory factory, int version)
    {
        super(context, dbName, factory, version);
        this.filels = fields;
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(getCreateTableSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
    {
    }

    private String getCreateTableSql()
    {
        String s = "create table " + tableName;
        StringBuffer field = new StringBuffer();
        for (String f : filels)
            field.append(f + " text,");// all string not integer
        s = s + "(" + field.substring(0, field.length() - 1) + ")";
        return s;
    }

//    private void testDb()
//    {
//        net.sqlcipher.database.SQLiteDatabase.loadLibs(this);
//        net.sqlcipher.database.SQLiteDatabase db = new DatabaseHelper(this,
//                "demo.db", Fields.Table.T_N, Fields.Table.F_S, null, 1)
//                .getWritableDatabase("my_key");
//        ContentValues values = new ContentValues();
//        values.put(Fields.Table.F_FIELD1, "达芬奇密码");
//        values.put(Fields.Table.F_FIELD2, "55");
//        db.insert(Fields.Table.T_N, null, values);
//        Cursor cursor = db.query(Fields.Table.T_N, null, null, null, null,
//                null, null);
//        if (cursor != null)
//        {
//            while (cursor.moveToNext())
//            {
//                String name = cursor.getString(cursor
//                        .getColumnIndex(Fields.Table.F_FIELD1));
//                int pages = cursor.getInt(cursor
//                        .getColumnIndex(Fields.Table.F_FIELD2));
//                log.e("book name is " + name);
//            }
//        }
//        cursor.close();
//        db.close();
//    }

}
