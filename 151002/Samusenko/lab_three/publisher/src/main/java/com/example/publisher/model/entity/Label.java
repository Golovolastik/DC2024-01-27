package com.example.publisher.model.entity;

import com.example.publisher.model.AEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder

public class Label extends  AEntity{
    private String name;
}
