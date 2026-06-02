package com.example.ch1.model;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "obj")
@Data
public class StudentProperties {
    private String sname;
    private int sage;
    private List<String> hobby;
    private Map<String, String> city;

    @Override
    public String toString() {
        return "StudentProperties [sname=" + sname
                + ", sage=" + sage
                + ", hobby0=" + hobby.get(0)
                + ", hobby1=" + hobby.get(1)
                + ", city=" + city + "]";
    }
}
