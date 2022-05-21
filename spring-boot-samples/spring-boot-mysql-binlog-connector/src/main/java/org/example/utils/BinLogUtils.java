package org.example.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.example.config.BinLogItem;
import org.example.config.Colum;
import org.example.config.Conf;
import org.example.domain.SearchStoreLogo;
import org.example.enums.CategoryEnum;
import org.example.mapper.SearchStoreLogoExtMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;
import java.sql.*;
import java.util.Date;
import java.util.*;

import static com.github.shyiko.mysql.binlog.event.EventType.*;

/**
 * 监听工具
 *
 * @author yuancetian
 * @since 2021/7/27
 **/
@Slf4j
@Component
public class BinLogUtils {

    private static BinLogUtils binLogUtils;

    @Resource
    private SearchStoreLogoExtMapper searchStoreLogoExtMapper;

    @PostConstruct
    public void init() {
        binLogUtils = this;
        binLogUtils.searchStoreLogoExtMapper = this.searchStoreLogoExtMapper;
    }

    /**
     * 拼接dbTable
     */
    public static String getdbTable(String db, String table) {
        return db + "-" + table;
    }

    /**
     * 获取columns集合
     */
    public static Map<String, Colum> getColMap(Conf conf, String db, String table) throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // 保存当前注册的表的colum信息
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + conf.getHost() + ":" + conf.getPort(), conf.getUsername(), conf.getPasswd());
            // 执行sql
            String preSql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? and TABLE_NAME = ?";
            PreparedStatement ps = connection.prepareStatement(preSql);
            ps.setString(1, db);
            ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            Map<String, Colum> map = new HashMap<>(rs.getRow());
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEMA");
                String tableName = rs.getString("TABLE_NAME");
                String column = rs.getString("COLUMN_NAME");
                int idx = rs.getInt("ORDINAL_POSITION");
                String dataType = rs.getString("DATA_TYPE");
                if (column != null && idx >= 1) {
                    map.put(column, new Colum(schema, tableName, idx - 1, column, dataType)); // sql的位置从1开始
                }
            }
            ps.close();
            rs.close();
            return map;
        } catch (SQLException e) {
            log.error("load db conf error, db_table={}:{} ", db, table, e);
        }
        return null;
    }

    /**
     * 根据table获取code
     *
     * @param table
     * @return java.lang.Integer
     */
    public static Integer getCodeByTable(String table) {
        if (StrUtil.isEmpty(table)) {
            return null;
        }
        return CategoryEnum.getCodeByTab(table);
    }

    public static String getMsgByTab(String table) {
        if (StrUtil.isEmpty(table)) {
            return null;
        }
        return CategoryEnum.getMsgByTab(table);
    }

    /**
     * 根据DBTable获取table
     *
     * @param dbTable
     * @return java.lang.String
     */
    public static String getTable(String dbTable) {
        if (StrUtil.isEmpty(dbTable)) {
            return "";
        }
        String[] split = dbTable.split("-");
        if (split.length == 2) {
            return split[1];
        }
        return "";
    }

    /**
     * 将逗号拼接字符串转List
     *
     * @param str
     * @return
     */
    public static List<String> getListByStr(String str) {
        if (StrUtil.isEmpty(str)) {
            return Lists.newArrayList();
        }

        return Arrays.asList(str.split(","));
    }

    /**
     * 根据操作类型获取对应集合
     *
     * @param binLogItem
     * @return
     */
    public static Map<String, Serializable> getOptMap(BinLogItem binLogItem) {
        // 获取操作类型
        EventType eventType = binLogItem.getEventType();
        if (isWrite(eventType) || isUpdate(eventType)) {
            return binLogItem.getAfter();
        }
        if (isDelete(eventType)) {
            return binLogItem.getBefore();
        }
        return null;
    }

    /**
     * 获取操作类型
     *
     * @param binLogItem
     * @return
     */
    public static Integer getOptType(BinLogItem binLogItem) {
        // 获取操作类型
        EventType eventType = binLogItem.getEventType();
        if (isWrite(eventType)) {
            return 1;
        }
        if (isUpdate(eventType)) {
            return 2;
        }
        if (isDelete(eventType)) {
            return 3;
        }
        return null;
    }


    /**
     * 根据storeId获取imgUrl
     */
    public static String getImgUrl(Long storeId) {

        if (storeId == null) {
            return "";
        }
        //获取url
        SearchStoreLogo searchStoreLogo = new SearchStoreLogo();
        searchStoreLogo.setStoreId(storeId);
        List<SearchStoreLogo> searchStoreLogos = binLogUtils.searchStoreLogoExtMapper.selectList(searchStoreLogo);
        if (CollectionUtil.isNotEmpty(searchStoreLogos)) {
            SearchStoreLogo storeLogo = searchStoreLogos.get(0);
            if (storeLogo != null) {
                return storeLogo.getStoreLogo();
            }
        }
        return "";
    }

    /**
     * 格式化date
     *
     * @param date
     * @return java.util.Date
     */
    public static Date getDateFormat(Date date) {
        if (date == null) {
            return null;
        }
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        String strDate = DateUtil.format(date, dateFormat);
        if (StrUtil.isEmpty(strDate)) {
            return null;
        }

        Date formatDate = DateUtil.parse(strDate, dateFormat);
        return formatDate;
    }
}

