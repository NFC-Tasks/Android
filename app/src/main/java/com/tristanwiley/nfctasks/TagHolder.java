package com.tristanwiley.nfctasks;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class TagHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private TextView tagName;

    public TagHolder(View view) {
        super(view);
        tagName = (TextView) view.findViewById(R.id.tag_name);

        view.setOnClickListener(this);
    }

    public void bindTag(Tag tag) {
        tagName.setText(tag.getName());
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        Intent tagRead = new Intent(context, TagReadActivity.class);
        tagRead.putExtra(TagReadActivity.ARG_TAG, tagName.getText().toString());
        context.startActivity(tagRead);
    }
}
