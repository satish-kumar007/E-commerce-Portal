package com.product.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoryNodeDto {
    private Long id;
    private String name;
    private String slug;
    private int level;
    private List<CategoryNodeDto> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public List<CategoryNodeDto> getChildren() { return children; }
    public void setChildren(List<CategoryNodeDto> children) { this.children = children; }
}
