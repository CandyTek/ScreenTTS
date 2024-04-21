package com.xiaoming.screentts;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainAppAdapter extends BaseAdapter implements Filterable {
	private final Context context;
	public List<AppInfo> displayApps;
	private SearchFilter filter;            // 筛选相关
	private final ArrayList<String> listMoveNull = new ArrayList<>();    // RemoveAll 移除空白 专用
	public List<AppInfo> originalInfo;    // 保留一份原始的数据，给筛选用

	public MainAppAdapter(Context context,List<AppInfo> apps) {

		this.context = context;
		this.originalInfo = apps;
		listMoveNull.add(null);
		listMoveNull.add("");
		displayApps = new ArrayList<>(originalInfo);
	}

	@Override
	public int getCount() {
		return displayApps.size();
	}

	@Override
	public Object getItem(int position) {
		return displayApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return displayApps.get(position).id;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_app,parent,false);
			holder = new ViewHolder();
			holder.tvAppLabel = convertView.findViewById(R.id.title);
			holder.tvPkgName = convertView.findViewById(R.id.subTitle);
			holder.linRoot = convertView.findViewById(R.id.linRoot);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//名字，包名
		AppInfo appInfo = displayApps.get(position);
		holder.tvAppLabel.setText(appInfo.label);
		holder.tvPkgName.setText(appInfo.pkgName);
		holder.linRoot.setBackgroundColor(appInfo.isSelected ? Color.GRAY : Color.TRANSPARENT);

		return convertView;
	}

	private static class ViewHolder {
		// View_Holder
		TextView tvAppLabel;
		TextView tvPkgName;
		LinearLayout linRoot;
	}

	public class SearchFilter extends Filter {
		//██ 发布搜索_结果 ██
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,FilterResults results) {
			displayApps = (List<AppInfo>) results.values;
			if (results.count > 0) notifyDataSetChanged();
			else notifyDataSetInvalidated();
		}

		private final Object mLock = new Object();// 互锁相关
		private String keyWord = "";            // 当前搜索的 字符串

		public String getKeyWord() {
			return keyWord;
		}

		//██ 主要搜索方法 ██
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults filterResults = new FilterResults();

			List<AppInfo> values;
			synchronized (mLock) {
				values = new ArrayList<>(originalInfo);
				keyWord = String.valueOf(prefix);
			}

			if (prefix == null || prefix.length() == 0) {
				filterResults.values = values;
				filterResults.count = values.size();
				//keyWord = "";
			} else {
				String prefixString = prefix.toString().toLowerCase();

				final int count = values.size();
				final List<AppInfo> newValues = new ArrayList<>();
				//██████████ 这里是搜索匹配 对应的结果
				if (prefixString.contains(" ")) { // 判断关键字里是否有空格，有的话就进行 多关键字匹配模式
					String[] str = prefixString.split("\\s+");
					ArrayList<String> arr = new ArrayList<>(Arrays.asList(str).subList(0,str.length));
					arr.removeAll(listMoveNull); // 这里去移除掉 所有的 空字符串，和null
					int arrSizeTemp = arr.size();// 有人测试过这种遍历效率最高
					// 多关键字匹配模式 方法
					for (int i = 0;i < count;i++) {
						// 第一匹配
						final AppInfo value = values.get(i);
						boolean addDoneTemp = true;
						for (int ii = 0;ii < arrSizeTemp;ii++) {
							if (!(value.label.toLowerCase().contains(arr.get(ii)))) {
								addDoneTemp = false;
								break;
							}
						}
						if (addDoneTemp) {
							newValues.add(value);
							continue;
						}
						// 第三匹配
						addDoneTemp = true;
						for (int ii = 0;ii < arrSizeTemp;ii++) {
							if (!(value.pkgName.toLowerCase().contains(arr.get(ii)))) {
								addDoneTemp = false;
								break;
							}
						}
						if (addDoneTemp) newValues.add(value);
					}
				} else {
					for (int i = 0;i < count;i++) {
						final AppInfo value = values.get(i);
						if (value.label.toLowerCase().contains(prefixString)
								|| value.pkgName.toLowerCase().contains(prefixString))
							newValues.add(value);
					}
				}
				filterResults.values = newValues;
				filterResults.count = newValues.size();
			}

			return filterResults;
		}

	}

	/** ██ 搜索filter初始化 ██ */
	@Override
	public SearchFilter getFilter() {
		if (filter == null) {
			filter = new SearchFilter();
		}
		return filter;
	}

	/** ██ 仅更新单个view的背景 ██ */
	public static void updateSingleItemBackground(View convertView,boolean isSelected) {
		if (convertView == null) return;
		((ViewHolder) convertView.getTag()).linRoot.setBackgroundColor(isSelected ? Color.GRAY : Color.TRANSPARENT);
	}

}
