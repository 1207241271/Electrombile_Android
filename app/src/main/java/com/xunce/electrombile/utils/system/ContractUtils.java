package com.xunce.electrombile.utils.system;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.provider.ContactsContract;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.xunce.electrombile.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by yangxu on 2016/12/26.
 */

public class ContractUtils {

    public static void addContract(String phoneNumber , Context context){
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        long contactId = ContentUris.parseId(resolver.insert(uri, values));

        // 添加姓名
        uri = Uri.parse("content://com.android.contacts/data");
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/name");
        values.put("data2", "小安宝报警");
        resolver.insert(uri, values);

        // 添加电话
        values.clear();
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/phone_v2");
        values.put("data2", "2");
        values.put("data1", phoneNumber);
        resolver.insert(uri, values);

        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,contactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.img_findcar);
        b.compress(Bitmap.CompressFormat.JPEG, 100, array);
        values.put(ContactsContract.Contacts.Photo.PHOTO, array.toByteArray());
        resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
    }

    public static void deleteContract(Context context){
        String deleteName = "小安宝报警";

        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        try {
            Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts._ID}, "display_name=?", new String[]{deleteName}, null);
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                resolver.delete(uri, "display_name=?", new String[]{deleteName});
                uri = Uri.parse("content://com.android.contacts/data");
                resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
