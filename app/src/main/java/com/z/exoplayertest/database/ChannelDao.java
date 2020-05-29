package com.z.exoplayertest.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChannelDao {
    private SqliteHelper helper;
    private static ChannelDao instance = null;
    private SQLiteDatabase db;

    public ChannelDao(Context context) {
        helper = new SqliteHelper(context);
        db = helper.getWritableDatabase();
    }

    public static ChannelDao getInstance(Context context) {
        if (instance == null) {
            instance = new ChannelDao(context);
        }
        return instance;
    }

    /**
     * 插入数据
     *
     * @param channel
     */
    public void add(Channel channel) {
        String sql = "Insert into Channel(name,url,isLike) values(?,?,?)";
        db.execSQL(sql, new Object[]{channel.getName(), channel.getUrl(), channel.getIsLike()
                }
        );
    }

    /**
     * 插入数据
     *
     * @param list
     */
    public void addList(List<Channel> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        String sql = "Insert into Channel(name,url,isLike) values(?,?,?)";
        for (Channel channel : list) {
            db.execSQL(sql, new Object[]{channel.getName(), channel.getUrl(), channel.getIsLike()});
        }
    }

    public void deleteAll(){
        db.execSQL("delete from Channel");
    }
    /**
     * 修改数据
     */
    public void update(String[] values) {
        db.execSQL("update Channel set isLike=? where id=? ", values);
    }


    /**
     * 获取所有数据
     *
     * @return
     */
    public List<Channel> getAllData() {
        List<Channel> channelList = new ArrayList<Channel>();
        String sql = "select * from Channel";
        Cursor cursor = db.rawQuery(sql, new String[]{

        });
        while (cursor.moveToNext()) {
            channelList.add(new Channel(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("url")),
                            cursor.getInt(cursor.getColumnIndex("isLike"))
                    )
            );
        }
        return channelList;
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public Channel findById(int id) {
        String sql = "select id,name,url,isLike from Channel where id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(id)
        });
        if (cursor.moveToNext()) {
            return new Channel(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("url")),
                    cursor.getInt(cursor.getColumnIndex("isLike"))
            );
        }
        return null;
    }

    /**
     * 获取用户收藏
     *
     * @return
     */
    public List<Channel> queryUserLike() {
        List<Channel> channelList = new ArrayList<Channel>();

        String sql = "select * from Channel where isLike = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"1"});
        while (cursor.moveToNext()) {
            channelList.add(new Channel(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("url")),
                            cursor.getInt(cursor.getColumnIndex("isLike"))
                    )
            );
        }
        return channelList;
    }

    /**
     * 根据名称模糊查询用户收藏
     *
     * @return
     */
    public List<Channel> queryByNameAndLike(String value) {
        List<Channel> channelList = new ArrayList<Channel>();

        String sql = "select * from Channel where name like ? and isLike = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + value + "%","1"});
        while (cursor.moveToNext()) {
            channelList.add(new Channel(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("url")),
                            cursor.getInt(cursor.getColumnIndex("isLike"))
                    )
            );
        }
        return channelList;
    }

    /**
     * 根据名称模糊查询
     *
     * @return
     */
    public List<Channel> queryByName(String value) {
        List<Channel> channelList = new ArrayList<Channel>();

        String sql = "select * from Channel where name like ?";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + value + "%"});
        while (cursor.moveToNext()) {
            channelList.add(new Channel(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("url")),
                            cursor.getInt(cursor.getColumnIndex("isLike"))
                    )
            );
        }
        return channelList;
    }
}
