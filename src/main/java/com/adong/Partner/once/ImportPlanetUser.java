package com.adong.Partner.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  导入星球用户到数据库
 */
public class ImportPlanetUser {
    public static void main(String[] args) {
        String fileName = "D:\\deskTop\\Partner_matching\\Partner_Matching_Backend\\src\\main\\resources\\alarm.csv";
        List<PlanetUserInfo> userInfoList = EasyExcel.read(fileName).head(PlanetUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        Map<String, List<PlanetUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getAlarm()))
                        .collect(Collectors.groupingBy(PlanetUserInfo::getAlarm));
        System.out.println("不重复的alarm数 = " + listMap.keySet().size());

    }
}
