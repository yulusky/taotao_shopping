package com.taotao.search.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.taotao.common.pojo.SearchItem;
import com.taotao.common.pojo.SearchResult;

/**
 * 从索引库中搜索商品的dao 

 */
@Repository
public class SearchDao {
	@Autowired
	private SolrServer solrServer;
	/**
	 * 根据查询的条件查询商品的结果集
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SearchResult search(SolrQuery query) throws Exception{
		
		SearchResult searchResult = new SearchResult();
		//1.创建solrserver对象 由spring管理 注入
		//2.直接执行查询
		QueryResponse response = solrServer.query(query);
		//3.获取结果集
		SolrDocumentList results = response.getResults();
		//设置searchresult的总记录数
		searchResult.setRecordCount(results.getNumFound());
		//4.遍历结果集
		//定义一个集合
		List<SearchItem> itemlist = new ArrayList<>();
		
		//取高亮
		Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
		
		for (SolrDocument solrDocument : results) {
			//讲solrdocument中的属性 一个个的设置到 searchItem中
			SearchItem item = new SearchItem();
			item.setCategory_name(solrDocument.get("item_category_name").toString());
			item.setId(Long.parseLong(solrDocument.get("id").toString()));
			item.setImage(solrDocument.get("item_image").toString());
			//item.setItem_desc(item_desc);
			item.setPrice((Long)solrDocument.get("item_price"));
			item.setSell_point(solrDocument.get("item_sell_point").toString());
			//取高亮
			List<String> list = highlighting.get(solrDocument.get("id")).get("item_title");
			//判断list是否为空
			String gaoliangstr = "";
			if(list!=null && list.size()>0){
				//有高亮
				gaoliangstr=list.get(0);
			}else{
				gaoliangstr = solrDocument.get("item_title").toString();
			}
			
			item.setTitle(gaoliangstr);
			//searchItem  封装到SearchResult中的itemList 属性中
			itemlist.add(item);
		}
		//5.设置SearchResult 的属性
		searchResult.setItemList(itemlist);
		return searchResult;
	}
}
