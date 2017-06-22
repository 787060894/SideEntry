package com.panku.sideentry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static com.panku.sideentry.Cheeses.NAMES;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.lv);
        final MyAdapter adapter = new MyAdapter();
        listView.setAdapter(adapter);
        //listView滑动的监听
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                System.out.println("scrollstate = " + scrollState);
                //scrollState==1  说明listView滑动了
                if (scrollState == 1) {
                    SwipeLayout swipeLayout = adapter.getPreSwipeLayout();
                    if (swipeLayout != null) {
                        //如果当前有item打开了  我们就关闭它
                        swipeLayout.close();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

    }


    public class MyAdapter extends BaseAdapter {

        private SwipeLayout preSwipeLayout; //上一个打开的条目

        public SwipeLayout getPreSwipeLayout() {
            return preSwipeLayout;
        }

        @Override
        public int getCount() {
            return NAMES.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_swipe, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(NAMES[position]);
            //点击删除条目
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "删除了", Toast.LENGTH_SHORT).show();
                }
            });
            //侧滑的监听
            holder.layout.setOnSwipeChangeListener(new SwipeLayout.OnSwipeChangeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {
                    //打开的时候记录一下
                    preSwipeLayout = layout;
                }

                @Override
                public void onClose(SwipeLayout layout) {

                }

                @Override
                public void onStartOpen(SwipeLayout layout) {
                    //将要打开的时候调用  关闭上一个打开的
                    if (preSwipeLayout != null) {
                        preSwipeLayout.close();
                    }
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }
            });
            return convertView;
        }
    }

    public class ViewHolder {
        public SwipeLayout layout;
        public TextView tvName;
        public TextView call;
        public TextView delete;

        public ViewHolder(View convertView) {
            layout = (SwipeLayout) convertView.findViewById(R.id.swipelayou);
            tvName = (TextView) convertView.findViewById(R.id.tv_name);
            call = (TextView) convertView.findViewById(R.id.call);
            delete = (TextView) convertView.findViewById(R.id.delete);
        }
    }
}
