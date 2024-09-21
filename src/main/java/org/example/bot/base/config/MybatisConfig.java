package org.example.bot.base.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//@MapperScan("cn.tedu.ivos.*.mapper")
@MapperScan("org.example.bot.*.mapper")
public class MybatisConfig {
}