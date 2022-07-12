package org.hango.cloud.gdashboard.api.util;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static <C> void writeBean(HttpServletResponse response, String[] header, List<C> content, String fileName) {
        Class clazz = content.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<Field> realFileds = new ArrayList<>();
        //判断是否是复合字段，避免$jacocoData问题
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isSynthetic()) continue;
            realFileds.add(fields[i]);
        }
        String[] properties = new String[realFileds.size()];
        for (int i = 0; i < realFileds.size(); i++) {
            properties[i] = realFileds.get(i).getName();
        }
        setResponseProperty(response, fileName);
        try (ICsvBeanWriter iCsvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE)) {
            iCsvWriter.writeHeader(header);
            for (C c : content) {
                iCsvWriter.write(c, properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setResponseProperty(HttpServletResponse response, String fileName) {
        response.reset();
        response.setContentType("application/csv;charset=utf-8");
        try {
            response.getWriter().write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setHeader("content-disposition", "attachment; filename=" + fileName + ".csv");
    }
}
