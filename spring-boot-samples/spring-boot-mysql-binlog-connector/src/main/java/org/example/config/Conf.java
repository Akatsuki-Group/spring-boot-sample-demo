package org.example.config;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 数据库配置
 *
 * @author yuancetian
 * @since 2021/7/27
 **/
@Data
@AllArgsConstructor
public class Conf {
    private String host;
    private int port;
    private String username;
    private String passwd;
}

