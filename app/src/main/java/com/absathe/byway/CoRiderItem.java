package com.absathe.byway;

import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ABSathe on 01-01-2019.
 */
public class CoRiderItem extends AbstractItem<CoRiderItem, CoRiderItem.ViewHolder> {
    public String name;

    public CoRiderItem() {
        this.name = "This is a person with some name";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    @NonNull
    @Override
    public CoRiderItem.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.corideritem;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_corider;
    }
    protected static class ViewHolder extends FastAdapter.ViewHolder<CoRiderItem> {
        @BindView(R.id.corideritem_name)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bindView(CoRiderItem item, List<Object> payloads) {
            name.setText(item.name);
        }

        @Override
        public void unbindView(CoRiderItem item) {
            name.setText(null);
        }
    }
}
