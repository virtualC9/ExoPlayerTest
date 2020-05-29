package com.z.exoplayertest.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.z.exoplayertest.R;
import com.z.exoplayertest.database.Channel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChannelAdaptr extends RecyclerView.Adapter<ChannelAdaptr.ViewHolder> {

    //数据源
    private List<Channel> mList;
    private OnItemClickListener onItemClickListener;
    private Context context;

    public ChannelAdaptr(Context context,List<Channel> list) {
        mList = list;
        this.context = context;
    }

    //返回item个数
    @Override
    public int getItemCount() {
        if (mList == null || mList.size() == 0) {
            return 0;
        }
        return mList.size();
    }

    //创建ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //填充视图
    @Override
    public void onBindViewHolder(@NonNull final ChannelAdaptr.ViewHolder holder, final int position) {
        holder.mView.setText(mList.get(position).getName());
        if (mList.get(position).getIsLike() == 1) {
            holder.imageView.setImageDrawable(context.getDrawable(R.mipmap.like));
        }else {
            holder.imageView.setImageDrawable(context.getDrawable(R.mipmap.unlike));
        }
        //添加点击监听事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClickItem(position, mList.get(position), false);
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClickItem(position, mList.get(position), true);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.channel_name);
            imageView = itemView.findViewById(R.id.item_like);
        }
    }

    /**
     * 接口
     */
    public interface OnItemClickListener {
        /**
         * 点击标签
         *
         * @param position
         * @param channel
         * @param isClickLike
         */
        void onClickItem(int position, Channel channel, boolean isClickLike);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
