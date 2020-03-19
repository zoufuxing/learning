package cn.crabapples.spring.entity;

import cn.crabapples.spring.common.BaseEntity;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

/**
 * TODO
 *
 * @author Mr.He
 * 2020/3/18 23:45
 * e-mail crabapples.cn@gmail.com
 * qq 294046317
 * pc-name 29404
 */
@Entity
@Getter
@Setter
public class MenuInfo extends BaseEntity {
    private String listId;
    private String title;
    private String img;
    private String name;
    private String content;
    private Integer count;
    private Double amt;
    private Double sum;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}