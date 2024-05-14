package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @JsonIgnore // 양방향 연관관계에서 서로 호출하면서 무한 루프가 걸리므로 한곳은 꼭 JsonIgnore 처리 해야한다.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
