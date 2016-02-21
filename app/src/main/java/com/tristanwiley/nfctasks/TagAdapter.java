package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class TagAdapter extends BaseAdapter {
    public static final String[] TAG_COLUMNS = new String[] {
            NFContract.TagEntry.TABLE_NAME + "." + NFContract.TagEntry._ID,
            NFContract.TagEntry.COLUMN_NAME
    };

    public static final int NAME_INDEX = 1;

    private Activity mContext;
    private List<Tag> mTags;

    public TagAdapter(Activity context, List<Tag> tags) {
        this.mContext = context;
        this.mTags = tags;
    }

    @Override
    public int getCount() {
        return mTags.size();
    }

    @Override
    public Object getItem(int position) {
        return mTags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TagViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_tag, parent, false);
            viewHolder = new TagViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TagViewHolder) convertView.getTag();
        }

        viewHolder.bindTag(mTags.get(position));

        return convertView;
    }

    public class TagViewHolder implements View.OnClickListener{
        private TextView mTagName;
        private Button mRunTests;

        public TagViewHolder(View view) {
            mTagName = (TextView) view.findViewById(R.id.tag_name);
            mRunTests = (Button) view.findViewById(R.id.run_tests);
            mRunTests.setOnClickListener(this);
        }

        public void bindTag(Tag tag) {
            mTagName.setText(tag.getName());
        }

        @Override
        public void onClick(View v) {
            NFDataSource dataSource = new NFDataSource(mContext);
            dataSource.open();
            // Get tests for name
            List<NestTask> tasks = dataSource.getNestTasks(mContext);
            for(NestTask task : tasks) {
                task.run();
            }
        }
    }
}
