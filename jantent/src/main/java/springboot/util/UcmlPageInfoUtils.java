package springboot.util;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;

public class UcmlPageInfoUtils {

	public static PageInfo<Map<String, Object>> initUcmlpageInfo(List<Map<String, Object>> totalList,
			PageInfo<Map<String, Object>> pageInfo,
			int page,
			int limit,
			List<Map<String, Object>> pList){
		
		pageInfo.setList(pList);
		pageInfo.setPageNum(page);
		if(page>1) pageInfo.setHasPreviousPage(true);
		if(page==1) pageInfo.setIsFirstPage(true);
		
		pageInfo.setTotal(totalList.size());
		int pages = totalList.size()/limit + 1;
		if(page==pages) {pageInfo.setIsLastPage(true); pageInfo.setHasNextPage(false);}
		else pageInfo.setHasNextPage(true);
		pageInfo.setPages(totalList.size()/limit + 1);
		int[] navigatepageNums = new int[pages];
		for(int i=1;i<=pages;i++) {
			navigatepageNums[i-1]=i;
		}
		pageInfo.setNavigatepageNums(navigatepageNums);
		return pageInfo;
		
	}
}
