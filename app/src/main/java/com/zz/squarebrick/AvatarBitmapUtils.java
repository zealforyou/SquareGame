package com.zz.squarebrick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.util.LruCache;

import com.vise.common_utils.utils.view.ActivityUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhuo.zhang on 2018/11/2.
 */

public class AvatarBitmapUtils {
    private static AvatarBitmapUtils avatarUtils;
    private LruCache<Integer, Bitmap> bitmapCaches;
    private BitmapRegionDecoder bitmapDecoder;
    private int width, height;
    private BitmapFactory.Options options;
    private int cols = 14, rows = 16;
    private int colWidth = 60, space = 1;

    public static AvatarBitmapUtils getAvatarUtils() {
        if (avatarUtils == null) {
            synchronized (ActivityUtil.class) {
                if (avatarUtils == null) {
                    avatarUtils = new AvatarBitmapUtils();
                }
            }
        }
        return avatarUtils;
    }

    public AvatarBitmapUtils() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            release();
        }
    }

    private void init() throws IOException {
        GameApplication app = GameApplication.getApp();
        int identifier = app.getResources().getIdentifier("avatar_list", "mipmap", app.getPackageName());
        InputStream inputStream = app.getResources().openRawResource(identifier);
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        width = options.outWidth;
        height = options.outHeight;
        bitmapDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        initCache();

    }


    private void initCache() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        bitmapCaches = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    // 把Bitmap对象加入到缓存中
    private void addBitmapToMemory(Integer key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapCaches.put(key, bitmap);
        }
    }

    // 从缓存中得到Bitmap对象
    private Bitmap getBitmapFromMemCache(Integer key) {
        return bitmapCaches.get(key);
    }

    // 从缓存中删除指定的Bitmap
    private void removeBitmapFromMemory(Integer key) {
        bitmapCaches.remove(key);
    }

    public Bitmap getAvartar(Integer index) {
        Bitmap bitmap = getBitmapFromMemCache(index);
        if (bitmap == null) {
            int row = index / cols;
            int col = index % cols;
            Rect rect = new Rect();
            rect.left = col * (colWidth + space);
            rect.top = row * (colWidth + space);
            rect.right = rect.left + colWidth + space;
            rect.bottom = rect.top + colWidth + space;
            bitmap = bitmapDecoder.decodeRegion(rect, options);
            addBitmapToMemory(index, bitmap);
        }
        return bitmap;


    }

    public void release() {
        if (bitmapCaches != null)
            bitmapCaches.evictAll();
        bitmapCaches = null;
        if (bitmapDecoder != null)
            bitmapDecoder.recycle();
        bitmapDecoder = null;
        avatarUtils = null;
    }
}
