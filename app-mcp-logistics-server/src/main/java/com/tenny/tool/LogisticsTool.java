package com.tenny.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class LogisticsTool {

    @Tool(description = "查询内部物流单号的轨迹信息，支持以 LOG 开头后跟 12 位数字的内部运单号")
    public String queryLogistics(
            @ToolParam(description = "内部物流运单号，格式为 LOG + 12 位数字，如 LOG202606290001")
            String trackingNumber) {

        // ================================================================
        // 真实场景：这里通过 Feign / RestTemplate / WebClient 调用物流业务服务
        //   LogisticsResp resp = logisticsFeignClient.query(trackingNumber);
        //   return formatResult(resp);
        // ================================================================

        // 校验内部单号格式
        if (trackingNumber == null || !trackingNumber.matches("LOG\\d{12}")) {
            return "❌ 单号格式不正确，请输入有效的内部运单号（如 LOG202606290001）";
        }

        Random random = new Random(trackingNumber.hashCode());
        int seed = Math.abs(random.nextInt()) % 10;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<String> locations = new ArrayList<>(List.of(
                "杭州市西湖区", "上海市浦东新区转运中心", "深圳市宝安区集散中心",
                "广州市白云区分拨中心", "北京市大兴区转运中心", "南京市江宁区集散中心",
                "武汉市东西湖区中转站", "成都市双流区转运中心"));
        Collections.shuffle(locations, random);
        int checkpointCount = 3 + Math.abs(random.nextInt()) % 4;

        String status, currentLocation, estimateArrival;
        List<String[]> checkpoints = new ArrayList<>();

        if (seed < 2) {
            status = "已签收";
            currentLocation = "杭州市西湖区";
            estimateArrival = "已签收";
            LocalDateTime t = now.minusDays(2);
            for (int i = 0; i < checkpointCount; i++) {
                String act = i == 0 ? "已揽收" : i == checkpointCount - 1 ? "已签收" : "已到达";
                checkpoints.add(new String[]{t.plusHours(i * 4L).format(fmt), locations.get(i), act});
            }
        } else if (seed < 7) {
            status = "运输中";
            currentLocation = locations.get(1);
            estimateArrival = now.plusHours(6 + Math.abs(random.nextInt()) % 24).format(fmt) + " (杭州)";
            LocalDateTime t = now.minusHours(12);
            for (int i = 0; i < checkpointCount; i++) {
                String act = i == 0 ? "已揽收" : (i < checkpointCount - 1 ? "已发出" : "运输中");
                checkpoints.add(new String[]{t.plusHours(i * 3L).format(fmt), locations.get(i), act});
            }
        } else {
            status = "已揽收";
            currentLocation = locations.get(0);
            estimateArrival = now.plusDays(1 + Math.abs(random.nextInt()) % 3).format(fmt) + " (杭州)";
            LocalDateTime t = now.minusHours(2);
            checkpoints.add(new String[]{t.format(fmt), locations.get(0), "已揽收"});
            checkpoints.add(new String[]{t.plusHours(1).format(fmt), locations.get(0), "已装车"});
        }

        checkpoints.sort((a, b) -> b[0].compareTo(a[0]));

        StringBuilder sb = new StringBuilder();
        sb.append("📦 物流查询结果\n");
        sb.append("运单编号：").append(trackingNumber).append("\n");
        sb.append("当前状态：").append(status).append("\n");
        sb.append("当前位置：").append(currentLocation).append("\n");
        sb.append("预计到达：").append(estimateArrival).append("\n");
        sb.append("\n--- 物流轨迹 ---\n");
        for (String[] cp : checkpoints) {
            sb.append(cp[0]).append("  ").append(cp[1]).append("  ").append(cp[2]).append("\n");
        }
        return sb.toString();
    }
}