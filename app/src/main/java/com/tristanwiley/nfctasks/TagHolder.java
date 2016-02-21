package com.tristanwiley.nfctasks;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class TagHolder extends RecyclerView.ViewHolder {
    private TextView tagName;

    public TagHolder(View view) {
        super(view);
        tagName = (TextView) view.findViewById(R.id.tag_name);
    }

    public void bindTag(Tag tag) {
        tagName.setText(tag.getName());
    }
}
