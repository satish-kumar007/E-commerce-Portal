package com.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, unique = true, length = 160)
    private String slug; // SEO-friendly identifier e.g. electronics/mobiles

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Category> children = new HashSet<>();

    @Column(nullable = false)
    private int level = 0; // root=0

    @Column(nullable = false)
    private boolean popular = false; // for caching popular categories

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    public Set<Category> getChildren() { return children; }
    public void setChildren(Set<Category> children) { this.children = children; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }
}
