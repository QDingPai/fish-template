package com.qdp.fish.template.utils.ftpUtil.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FtpConfig {
    String host;
    Integer port;
    String username;
    String password;
}
