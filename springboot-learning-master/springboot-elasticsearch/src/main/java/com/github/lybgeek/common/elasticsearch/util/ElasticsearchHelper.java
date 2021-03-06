package com.github.lybgeek.common.elasticsearch.util;

import com.alibaba.fastjson.JSON;
import com.github.lybgeek.common.elasticsearch.annotation.EsDocument;
import com.github.lybgeek.common.elasticsearch.annotation.EsField;
import com.github.lybgeek.common.elasticsearch.annotation.EsId;
import com.github.lybgeek.common.elasticsearch.model.EsEntity;
import com.github.lybgeek.common.elasticsearch.model.EsIndex;
import com.github.lybgeek.common.model.PageResult;
import com.github.lybgeek.common.util.ReflectionUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ElasticsearchHelper {

  @Autowired
  private RestHighLevelClient highLevelClient;

  /**
   * ????????????????????????EsDocument???????????????????????????
   * @param packageName ??????
   */
  public void createIndexs(String packageName){
    Set<Class<?>> clzSet = ReflectionUtil.getClasses(packageName,
        EsDocument.class);

   if(CollectionUtils.isNotEmpty(clzSet)) {
     for (Class<?> clz : clzSet) {
       createIndex(clz);

     }

   }

  }

  public void createIndex(Class<?> clz) {

    CachedClass cachedClass = ReflectionCache.getCachedClass(clz);
    EsIndex esIndex = convertDocumentToIndexEs(clz);
    boolean isIndexExist = this.isIndexExist(esIndex.getIndexName());
    if (!isIndexExist) {
      CachedField[] fields = cachedClass.getFields();
      Map<String, Map<String, Object>> properties = ReflectionUtil
          .covertFieldsIncludeAnnotationValueToMap(fields, EsField.class);
      boolean isSuccess =  this.createIndex(esIndex,properties);
      if(isSuccess){
        log.info("??????{}????????????",esIndex.getIndexName());
      }
    }
  }

  public EsIndex convertDocumentToIndexEs(Class<?> clz) {

    EsDocument document = clz.getAnnotation(EsDocument.class);
    EsIndex esIndex = new EsIndex();
    esIndex.setIndexName(document.indexName());
    esIndex.setType(document.type());
    esIndex.setReplicas(document.replicas());
    esIndex.setShards(document.shards());
    return esIndex;
  }

  public Object getEsId(Object retVal) {

    Object id = null;

    List<Field> fields = FieldUtils.getFieldsListWithAnnotation(retVal.getClass(), EsId.class);

    if(CollectionUtils.isNotEmpty(fields)){
      Field idField = fields.get(0);
      try {
        id = FieldUtils.readDeclaredField(retVal,idField.getName(),true);
      } catch (IllegalAccessException e) {
        log.error(e.getMessage(),e);
      }
    }
    return id;
  }

  /**
   * ????????????
   *
   * @param indexName ????????????
   * @param properties ????????????map???key????????????????????????value????????????????????????????????????????????????????????????????????????
   * ?????????@Field(type = FieldType.Text, analyzer = "ik_smart",searchAnalyzer = "ik_smart", fielddata=true)
   *      private String urlName;
   * ????????????map???urlName->@Field???????????????map???type->FieldType.Text???
   *
   * ?????????????????????https://blog.csdn.net/pyq666/article/details/99639810
   * @return ????????????????????????true????????????????????? fasle
   */
  public boolean createIndex(String indexName, Map<String,Map<String,Object>> properties){

    try {
      XContentBuilder builder = XContentFactory.jsonBuilder();
      builder.startObject()
          .startObject("mappings")
         // .startObject("_doc")
          .field("properties", properties)
          //.endObject()
          .endObject()
          .startObject("settings")
          .field("number_of_shards", 5)
          .field("number_of_replicas", 1)
          .endObject()
          .endObject();

      CreateIndexRequest request = new CreateIndexRequest(indexName).source(builder);
      CreateIndexResponse response = highLevelClient.indices().create(request, RequestOptions.DEFAULT);

      return response.isAcknowledged();
    } catch (IOException e) {
       log.error("createIndex error:"+e.getMessage(),e);
    }

    return false;

  }

  /**
   * ????????????
   * @param esIndex ????????????
   * @param properties
   * @return
   */
  public boolean createIndex(EsIndex esIndex, Map<String,Map<String,Object>> properties){

    try {
      XContentBuilder builder = XContentFactory.jsonBuilder();
      builder.startObject()
          .startObject("mappings")
          //.startObject(esIndex.getType()) es7??????????????????type
          .field("properties", properties)
          //.endObject()
          .endObject()
          .startObject("settings")
          .field("number_of_shards", esIndex.getShards())
          .field("number_of_replicas", esIndex.getReplicas())
          .endObject()
          .endObject();

      CreateIndexRequest request = new CreateIndexRequest(esIndex.getIndexName()).source(builder);
      CreateIndexResponse response = highLevelClient.indices().create(request, RequestOptions.DEFAULT);

      return response.isAcknowledged();
    } catch (IOException e) {
      log.error("createIndex error:"+e.getMessage(),e);
    }

    return false;

  }

  /**
   * ??????????????????????????????
   * @param indexName ?????????
   * @return
   */
  public boolean isIndexExist(String indexName){

    try {
      GetIndexRequest request = new GetIndexRequest(indexName);
      request.local(false);
      request.humanReadable(true);
      request.includeDefaults(false);
      return highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      log.error("isIndexExist error:"+e.getMessage(),e);
    }

    return false;
  }

  /**
   * ??????/??????????????????
   * @param indexName ????????????
   * @param entity
   */
  public boolean saveOrUpdate(String indexName, EsEntity entity) {
    boolean isSuccess = false;
    IndexRequest request = new IndexRequest(indexName);
    request.id(entity.getId());
    request.source(JSON.toJSONString(entity.getData()), XContentType.JSON);
    try {
      IndexResponse response = highLevelClient.index(request, RequestOptions.DEFAULT);
      isSuccess = response.status() == RestStatus.CREATED || response.status() == RestStatus.OK;
    } catch (IOException e) {
      log.error("saveOrUpdate error:"+e.getMessage(),e);
    }

    return isSuccess;
  }

  /**
   *
   * ????????????
   * @param indexName
   * @param list
   */
  public boolean insertBatch(String indexName, List<EsEntity> list) {
    BulkRequest request = new BulkRequest();
    list.forEach(item -> request.add(new IndexRequest(indexName).id(item.getId())
        .source(JSON.toJSONString(item.getData()), XContentType.JSON)));
    try {
      BulkResponse responses = highLevelClient.bulk(request, RequestOptions.DEFAULT);
      return !responses.hasFailures();
    } catch (Exception e) {
      log.error("insertBatch error:"+e.getMessage(),e);
    }
    return false;
  }

  /**
   * ????????????
   * @param indexName
   * @param idList ????????????
   * @param <T>
   */
  public <T> boolean deleteBatch(String indexName, Collection<T> idList) {
    BulkRequest request = new BulkRequest();
    idList.forEach(item -> request.add(new DeleteRequest(indexName, item.toString())));
    try {
      BulkResponse responses = highLevelClient.bulk(request, RequestOptions.DEFAULT);
      return !responses.hasFailures();
    } catch (Exception e) {
      log.error("deleteBatch error:"+e.getMessage(),e);
    }

    return false;
  }

  /**
   * ??????
   *
   * @param indexName ????????????
   * @param builder ????????????
   * @param searchTargetClz ???????????????
   *
   */
  public <T> List<T> search(String indexName, SearchSourceBuilder builder, Class<T> searchTargetClz) {
    SearchRequest request = new SearchRequest(indexName);
    request.source(builder);
    try {
      SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);
      SearchHit[] hits = response.getHits().getHits();
      List<T> res = new ArrayList<>(hits.length);
      for (SearchHit hit : hits) {
        res.add(JSON.parseObject(hit.getSourceAsString(), searchTargetClz));
      }
      return res;
    } catch (Exception e) {
      log.error("search error:"+e.getMessage(),e);
    }

    return null;
  }

  /**
   * ????????????
   *
   * @param indexName ????????????
   * @param builder ????????????
   * @param searchTargetClz ???????????????
   *
   */
  public <T> PageResult <T> pageSearch(String indexName, SearchSourceBuilder builder, Class<T> searchTargetClz){
    SearchRequest request = new SearchRequest(indexName);

    request.source(builder);
    PageResult<T> pageResult = new PageResult<>();
    try {
      SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);
      SearchHit[] hits = response.getHits().getHits();
      if(ArrayUtils.isNotEmpty(hits)){
        List<T> res = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
          res.add(JSON.parseObject(hit.getSourceAsString(), searchTargetClz));
        }
        int pageSize = builder.size();
        int pageNo = builder.from() / pageSize + 1;
        long total = response.getHits().getTotalHits().value;
        Long totalPage =(total + pageSize - 1) / pageSize;
        pageResult.setTotal(total);
        pageResult.setPageNo(pageNo);
        pageResult.setPageSize(pageSize);
        pageResult.setTotalPages(Integer.valueOf(totalPage.toString()));
        pageResult.setList(res);
      }

      return pageResult;
    } catch (Exception e) {
      log.error("pageSearch error:"+e.getMessage(),e);
    }
    return null;
  }

  /**
   * ????????????
   *
   * @param indexName ????????????
   *
   */
  public boolean deleteIndex(String indexName) {
    try {
      AcknowledgedResponse response =  highLevelClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
      return response.isAcknowledged();
    } catch (Exception e) {
      log.error("deleteIndex error:"+e.getMessage(),e);
    }
    return false;
  }

  /**
   * ???????????????????????????
   *
   * @param indexName ????????????
   * @param builder builder ????????????
   *
   */
  public boolean deleteByQuery(String indexName, QueryBuilder builder) {
    DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
    request.setQuery(builder);
    //????????????????????????,?????????10000
    request.setBatchSize(10000);
    request.setConflicts("proceed");
    try {
      BulkByScrollResponse response = highLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
      List<Failure> bulkFailures = response.getBulkFailures();
      return CollectionUtils.isEmpty(bulkFailures);
    } catch (Exception e) {
      log.error("deleteByQuery error:"+e.getMessage(),e);
    }

    return false;
  }



}
