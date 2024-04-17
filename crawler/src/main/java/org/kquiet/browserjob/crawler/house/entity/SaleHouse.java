package org.kquiet.browserjob.crawler.house.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * house for sale.
 *
 * @author monkey
 *
 */
@Entity
@Table(name = "sale_house")
public class SaleHouse {
  @Id
  @Column(name = "url")
  private String url;

  @Column(name = "site")
  private String site;

  @Column(name = "imageUrl")
  private String imageUrl;

  @Column(name = "description")
  private String description;

  @Column(name = "price")
  private String price;

  @Column(name = "createuser")
  private String createuser;

  @Column(name = "createdate")
  private LocalDateTime createdate;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getSite() {
    return site;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getCreateuser() {
    return createuser;
  }

  public void setCreateuser(String createuser) {
    this.createuser = createuser;
  }

  public LocalDateTime getCreatedate() {
    return createdate;
  }

  public void setCreatedate(LocalDateTime createdate) {
    this.createdate = createdate;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof SaleHouse temp) {
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
    return String.format(
        "%s[url=%s, site='%s', imageUrl='%s', "
            + "description='%s', price='%s', createuser='%s', createdate='%s']",
        SaleHouse.class.getTypeName(), url, site, imageUrl, description, price, createuser,
        createdate);
  }
}
