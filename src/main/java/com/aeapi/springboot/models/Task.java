package com.aeapi.springboot.models;

import java.util.List;

import lombok.Data;

@Data
public class Task {
    List<String> images;

    public String toString() {
        return String.join("\t", images);
    }
}
