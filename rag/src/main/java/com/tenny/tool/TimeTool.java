package com.tenny.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTool {

    // 使用mcp client则注释掉
//    @Tool(description = "获取系统默认时区的当前时间")
//    public String getCurrentTime() {
//        ZonedDateTime now = ZonedDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return now.format(formatter);
//    }

}
