package org.kquiet.browserjob.crawler.bigfun.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * real estate.
 *
 * @author monkey
 *
 */
@Entity
@Table(name = "real_estate")
public class RealEstate {
  @Id
  @Column(name = "url")
  private String url;

  @Column(name = "imageUrl")
  private String imageUrl;

  @Column(name = "title")
  private String title;

  @Column(name = "type")
  private String type;

  @Column(name = "layout")
  private String layout;

  @Column(name = "community")
  private String community;

  @Column(name = "address")
  private String address;

  @Column(name = "floor")
  private String floor;

  @Column(name = "age")
  private BigDecimal age;

  @Column(name = "parking")
  private String parking;

  @Column(name = "priceTotal")
  private BigDecimal priceTotal;

  @Column(name = "priceAveragePing")
  private BigDecimal priceAveragePing;

  @Column(name = "areaLand")
  private BigDecimal areaLand;

  @Column(name = "areaTotal")
  private BigDecimal areaTotal;

  @Column(name = "areaMain")
  private BigDecimal areaMain;

  @Column(name = "site")
  private String site;

  @Column(name = "source")
  private String source;

  @Column(name = "postDate")
  private LocalDateTime postDate;

  @Column(name = "createUser")
  private String createUser;

  @Column(name = "createDate")
  private LocalDateTime createDate;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLayout() {
    return layout;
  }

  public void setLayout(String layout) {
    this.layout = layout;
  }

  public String getCommunity() {
    return community;
  }

  public void setCommunity(String community) {
    this.community = community;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getFloor() {
    return floor;
  }

  public void setFloor(String floor) {
    this.floor = floor;
  }

  public BigDecimal getAge() {
    return age;
  }

  public void setAge(BigDecimal age) {
    this.age = age;
  }

  public String getParking() {
    return parking;
  }

  public void setParking(String parking) {
    this.parking = parking;
  }

  public BigDecimal getPriceTotal() {
    return priceTotal;
  }

  public void setPriceTotal(BigDecimal priceTotal) {
    this.priceTotal = priceTotal;
  }

  public BigDecimal getPriceAveragePing() {
    return priceAveragePing;
  }

  public void setPriceAveragePing(BigDecimal priceAveragePing) {
    this.priceAveragePing = priceAveragePing;
  }

  public BigDecimal getAreaLand() {
    return areaLand;
  }

  public void setAreaLand(BigDecimal areaLand) {
    this.areaLand = areaLand;
  }

  public BigDecimal getAreaTotal() {
    return areaTotal;
  }

  public void setAreaTotal(BigDecimal areaTotal) {
    this.areaTotal = areaTotal;
  }

  public BigDecimal getAreaMain() {
    return areaMain;
  }

  public void setAreaMain(BigDecimal areaMain) {
    this.areaMain = areaMain;
  }

  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public LocalDateTime getPostDate() {
    return postDate;
  }

  public void setPostDate(LocalDateTime postDate) {
    this.postDate = postDate;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public LocalDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof RealEstate temp) {
      return Objects.equals(url, temp.getUrl());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(url);
  }

  @Override
  public String toString() {
    return String.format("%s[url=%s, site='%s', createUser='%s', createDate='%s']",
        RealEstate.class.getTypeName(), url, site, createUser, createDate);
  }
}
